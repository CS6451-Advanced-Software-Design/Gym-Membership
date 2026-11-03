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
-- `merge` turns a file of one table per heading (the diary's weeks) into one table with a row per heading.
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

-- Drops the GenAI prompt and pull request numbers from a diary entry: §13 and the repository history already
-- carry them, and in the merged table they only add length
local function strip(inlines)
  local markdown = pandoc.write(pandoc.Pandoc({ pandoc.Plain(inlines) }), 'markdown-smart', { wrap_text = 'none' })
  markdown = markdown:gsub('\\#', '#')
    :gsub('%s*%(PRs? #[^)]*%)', '')
    :gsub('%s*%(#%d+[^)]*%)', '')
    :gsub('[.;,]?%s*GenAI #[%d#–%-, ]*%d', '')
    :gsub('%s*%(GenAI[^)]*%)', '')
    :gsub('\n', ' ')
  local blocks = pandoc.read(markdown, 'markdown-smart').blocks
  return blocks[1] and blocks[1].content or pandoc.List()
end

-- One table from a heading-and-table-per-section file such as the diary: one row per heading, labelled with the
-- heading's first two words ("Week 4"). Each later column lists every first-column entry (the member) that has
-- something in it, with members who wrote the same thing combined, and a non-empty bold-led paragraph after the
-- table (e.g. **Notes:** ...) is added to the second-to-last column
local function merge(blocks)
  local text = pandoc.utils.stringify
  local result, header
  local function lines(entries)
    local inlines = pandoc.List()
    for index, entry in ipairs(entries) do
      if index > 1 then
        inlines:insert(pandoc.LineBreak())
      end
      inlines:insert(pandoc.Strong({ pandoc.Str(table.concat(entry.names, ', ') .. ':') }))
      inlines:insert(pandoc.Space())
      inlines:extend(entry.content)
    end
    return pandoc.Cell({ pandoc.Plain(inlines) })
  end
  local rows = pandoc.List()
  local label, columns
  local function finish()
    if not columns then
      return
    end
    local cells = pandoc.List({ pandoc.Cell({ pandoc.Plain({ pandoc.Str(label) }) }) })
    for _, entries in ipairs(columns) do
      cells:insert(lines(entries))
    end
    rows:insert(pandoc.Row(cells))
    columns = nil
  end
  for _, block in ipairs(blocks) do
    if block.t == 'Header' then
      finish()
      local words = {}
      for word in text(block):gmatch('%S+') do
        words[#words + 1] = word
      end
      label = (words[1] or '') .. ' ' .. (words[2] or '')
    elseif block.t == 'Table' then
      if not header then
        header = block
      end
      columns = {}
      for index = 2, #block.colspecs do
        columns[index - 1] = {}
      end
      for _, body in ipairs(block.bodies) do
        for _, row in ipairs(body.body) do
          local name = text(row.cells[1].contents)
          for index = 2, #row.cells do
            local content = strip(row.cells[index].contents[1] and row.cells[index].contents[1].content or pandoc.List())
            local value = text(row.cells[index].contents)
            if value ~= '' then
              local entries = columns[index - 1]
              local found
              for _, entry in ipairs(entries) do
                if entry.value == value then
                  found = entry
                end
              end
              if found then
                table.insert(found.names, name)
              else
                table.insert(entries, { names = { name }, value = value, content = pandoc.List(content) })
              end
            end
          end
        end
      end
    elseif block.t == 'Para' and columns then
      local name, rest = text(block):match('^(%w+):%s*(.*)$')
      if name and rest ~= '' then
        local content = pandoc.List(block.content)
        while #content > 0 and (content[1].t == 'Strong' or content[1].t == 'Space') do
          content:remove(1)
        end
        table.insert(columns[#columns - 1], { names = { name }, value = rest, content = content })
      end
    end
  end
  finish()
  if not header then
    return blocks
  end
  header.colspecs[1] = { pandoc.AlignDefault, nil }
  header.head.rows[1].cells[1] = pandoc.Cell({ pandoc.Plain({ pandoc.Str('Week') }) })
  header.bodies = { header.bodies[1] }
  header.bodies[1].body = rows
  return pandoc.Blocks({ header })
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
