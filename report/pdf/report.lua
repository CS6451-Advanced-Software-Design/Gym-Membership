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
-- Grids: a div such as ::: {.grid columns=2} ::: lays its figures out side by side, `columns` to a row, so a page
-- holds more of them. Give each image a width (e.g. {width=100%}), which is then a share of its column.
-- Landscape: a div such as ::: landscape ::: gets a page of its own turned sideways, for a diagram too wide to read
-- at portrait width.

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

-- Keeps only the first count columns of a table
local function keep(table, count)
  while #table.colspecs > count do
    table.colspecs:remove()
  end
  local function trim(rows)
    for _, row in ipairs(rows) do
      while #row.cells > count do
        row.cells:remove()
      end
    end
  end
  trim(table.head.rows)
  for _, body in ipairs(table.bodies) do
    trim(body.body)
  end
end

-- Merges consecutive rows of a prompt log with the same date and member into one row, numbered first–last, with
-- each prompt on its own line
local function group(table)
  local text = pandoc.utils.stringify
  for _, body in ipairs(table.bodies) do
    local rows = pandoc.List()
    local first
    for _, row in ipairs(body.body) do
      local last = rows[#rows]
      local cells = row.cells
      if last and text(last.cells[2].contents) == text(cells[2].contents)
          and text(last.cells[3].contents) == text(cells[3].contents) then
        last.cells[1].contents = { pandoc.Plain({ pandoc.Str(first .. '–' .. text(cells[1].contents)) }) }
        for index = 4, #cells do
          local inlines = last.cells[index].contents[1].content
          inlines:insert(pandoc.LineBreak())
          inlines:extend(cells[index].contents[1].content)
        end
      else
        first = text(cells[1].contents)
        rows:insert(row)
      end
    end
    body.body = rows
  end
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
      if div.attributes.columns then
        keep(table, tonumber(div.attributes.columns))
      end
      if div.attributes.group then
        group(table)
      end
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

local function grid(div)
  local columns = tonumber(div.attributes.columns or '2')
  local blocks = pandoc.Blocks({
    pandoc.RawBlock('typst', '#grid(columns: ' .. columns .. ', column-gutter: 12pt, row-gutter: 6pt, align: bottom,'),
  })
  for _, block in ipairs(div.content) do
    blocks:insert(pandoc.RawBlock('typst', '['))
    blocks:insert(block)
    blocks:insert(pandoc.RawBlock('typst', '],'))
  end
  blocks:insert(pandoc.RawBlock('typst', ')'))
  return blocks
end

local function landscape(div)
  local blocks = pandoc.Blocks({ pandoc.RawBlock('typst', '#page(flipped: true)[') })
  blocks:extend(div.content)
  blocks:insert(pandoc.RawBlock('typst', ']'))
  return blocks
end

-- Keeps a paragraph that introduces a code block on the same page as it, so a heading and its intro are not left
-- alone at the bottom of a page when the (unbreakable) code moves to the next one
local function introduce(blocks)
  local result = pandoc.Blocks({})
  for index, block in ipairs(blocks) do
    local next = blocks[index + 1]
    if block.t == 'Para' and next and next.t == 'CodeBlock' then
      result:insert(pandoc.RawBlock('typst', '#block(sticky: true)['))
      result:insert(block)
      result:insert(pandoc.RawBlock('typst', ']'))
    else
      result:insert(block)
    end
  end
  return result
end

function Pandoc(document)
  local source = PANDOC_STATE.input_files[1]
  local base = directory(source)
  local blocks = document.blocks:walk({
    Div = function(div)
      if div.attributes.include then
        return include(div, base)
      end
      if div.classes:includes('grid') then
        return grid(div)
      end
      if div.classes:includes('landscape') then
        return landscape(div)
      end
    end,
  })
  document.blocks = introduce(rewrite(blocks, base))
  return document
end
