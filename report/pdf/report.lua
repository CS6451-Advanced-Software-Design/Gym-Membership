-- Pandoc filter for the report PDF.
--
-- Images: paths are relative to the Markdown file, but Typst resolves them from the repository root, so each
-- one is rewritten to start with /.
-- Links: a link to another file in the repository can't be followed in a PDF, so it points at GitHub instead.
-- Tables: every column takes the width its content needs, rather than widths guessed from the dashes under the header,
-- and a column with no alignment of its own is left-aligned rather than inheriting the centring around the table.
-- Includes: a div such as ::: {include="../diary.md"} ::: is replaced by that file, without its title and
-- introduction, with its headings moved down a level (or by `shift`). A long table can be fitted to the page with
-- `widths` (each column's share of the page width, e.g. "0.1 0.9") and `size` (its text size, e.g. "8pt").

local REPOSITORY = 'https://github.com/CS6451-Advanced-Software-Design/Gym-Membership/blob/main/'

local function directory(path)
  return path:match('^(.*)/[^/]*$') or '.'
end

-- Joins a relative path onto a directory and resolves its . and .. parts
local function resolve(base, path)
  local parts = {}
  for part in (base .. '/' .. path):gmatch('[^/]+') do
    if part == '..' then
      table.remove(parts)
    elseif part ~= '.' then
      table.insert(parts, part)
    end
  end
  return table.concat(parts, '/')
end

local function external(target)
  return target:match('^%a[%w+.-]*:') ~= nil or target:sub(1, 1) == '#'
end

local function rewrite(blocks, base)
  return pandoc.Blocks(blocks):walk({
    Table = function(table)
      local widths = {}
      for width in (table.attr.attributes.widths or ''):gmatch('%S+') do
        widths[#widths + 1] = tonumber(width)
      end
      for index, column in ipairs(table.colspecs) do
        local alignment = column[1] == 'AlignDefault' and 'AlignLeft' or column[1]
        table.colspecs[index] = { alignment, widths[index] }
      end
      return table
    end,
    Image = function(image)
      if not external(image.src) then
        image.src = '/' .. resolve(base, pandoc.path.normalize(image.src))
      end
      return image
    end,
    Link = function(link)
      if not external(link.target) then
        local path, anchor = link.target:match('^([^#]*)(#?.*)$')
        link.target = REPOSITORY .. resolve(base, path) .. anchor
      end
      return link
    end,
  })
end

-- The included file's content from its first second-level heading or table, skipping its title and introduction
local function body(blocks)
  local kept = pandoc.Blocks({})
  local started = false
  for _, block in ipairs(blocks) do
    if not started and ((block.t == 'Header' and block.level > 1) or block.t == 'Table') then
      started = true
    end
    if started then
      kept:insert(block)
    end
  end
  return kept
end

local function include(div, base)
  local relative = div.attributes.include
  local path = resolve(base, relative)
  local file = io.open(path, 'r')
  if not file then
    error('Cannot include ' .. path)
  end
  local text = file:read('a')
  file:close()
  local shift = tonumber(div.attributes.shift or '1')
  local blocks = body(pandoc.read(text, 'markdown').blocks):walk({
    Header = function(header)
      header.level = header.level + shift
      return header
    end,
    Table = function(table)
      table.attr.attributes.widths = div.attributes.widths
      return table
    end,
  })
  blocks = rewrite(blocks, directory(path))
  if div.attributes.size then
    blocks:insert(1, pandoc.RawBlock('typst', '#[\n#show table: set text(size: ' .. div.attributes.size .. ')\n#show raw: set text(size: 7pt)\n#set table(inset: 3pt)'))
    blocks:insert(pandoc.RawBlock('typst', ']'))
  end
  return blocks
end

function Pandoc(document)
  local source = PANDOC_STATE.input_files[1]
  local base = directory(source)
  local blocks = document.blocks:walk({
    Div = function(div)
      if div.attributes.include then
        return include(div, base)
      end
    end,
  })
  document.blocks = rewrite(blocks, base)
  return document
end
