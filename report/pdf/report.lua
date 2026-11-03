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
-- `merge` turns a file of one table per heading (the diary's weeks) into one table, with the heading's first two
-- words in a new first column and each non-empty bold-led paragraph (e.g. **Notes:**) as a row of its own.
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

-- One table from a heading-and-table-per-section file: each heading's first two words ("Week 4") go in a new first
-- column on its first row, and a non-empty paragraph after a table (e.g. **Notes:** ...) becomes a row of its own
local function merge(blocks)
  local merged, label
  local function cell(inlines)
    return pandoc.Cell({ pandoc.Plain(inlines or {}) })
  end
  for _, block in ipairs(blocks) do
    if block.t == 'Header' then
      local words = {}
      for word in pandoc.utils.stringify(block):gmatch('%S+') do
        words[#words + 1] = word
      end
      label = (words[1] or '') .. ' ' .. (words[2] or '')
    elseif block.t == 'Table' then
      local rows = pandoc.List()
      for _, body in ipairs(block.bodies) do
        rows:extend(body.body)
      end
      if not merged then
        merged = block
        merged.colspecs:insert(1, { pandoc.AlignDefault, nil })
        merged.head.rows[1].cells:insert(1, cell({ pandoc.Str('Week') }))
        merged.bodies = { merged.bodies[1] }
        merged.bodies[1].body = pandoc.List()
      end
      for index, row in ipairs(rows) do
        row.cells:insert(1, cell(index == 1 and { pandoc.Str(label) } or nil))
        merged.bodies[1].body:insert(row)
      end
    elseif block.t == 'Para' and merged then
      local text = pandoc.utils.stringify(block)
      local name, rest = text:match('^(%w+):%s*(.*)$')
      if name and rest ~= '' then
        local inlines = pandoc.List(block.content)
        -- drop the leading **Name:** and the space after it
        while #inlines > 0 and (inlines[1].t == 'Strong' or inlines[1].t == 'Space') do
          inlines:remove(1)
        end
        local row = pandoc.Row({ cell(), cell({ pandoc.Str(name) }), cell(inlines), cell(), cell() })
        merged.bodies[1].body:insert(row)
      end
    end
  end
  return merged and pandoc.Blocks({ merged }) or blocks
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
  if div.attributes.merge then
    blocks = merge(blocks)
  end
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
  document.blocks = rewrite(blocks, base)
  return document
end
