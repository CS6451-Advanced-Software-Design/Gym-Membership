// The report's layout: front cover, the blank marking scheme, the table of contents, then the sections that
// build.sh converts from report/sections. Build it with build.sh rather than compiling this file directly.

#let details = yaml("report.yaml")

#set document(title: details.title, author: details.members.map(member => member.name))
// Every page after the cover and the marking scheme carries the module and university at the top
#set page(
  paper: "a4",
  margin: (x: 2.4cm, y: 2.5cm),
  header: {
    set text(size: 9pt, style: "italic")
    grid(columns: (1fr, auto), details.module, details.university)
    v(-6pt)
    line(length: 100%, stroke: 0.4pt + luma(150))
  },
)
#set text(font: "Libertinus Serif", size: 11pt, lang: "en", region: "ie")
#set par(justify: true, leading: 0.62em, spacing: 1.1em)
#show raw: set text(font: "DejaVu Sans Mono", size: 8.5pt)
#show raw.where(block: true): block.with(fill: luma(246), inset: 8pt, radius: 2pt, width: 100%)
#show link: it => if type(it.dest) == str { underline(it) } else { it }
#show figure.caption: set text(size: 9.5pt)
#set table(stroke: 0.5pt + luma(170), inset: 5pt)
#show table: set text(size: 9pt)
#show table: set par(justify: false)
// A long table must be able to break across pages
#show figure.where(kind: table): set block(breakable: true)
// A table with a caption (a "Table: ..." line under it in Markdown) is numbered, with the caption above it.
// One without a caption isn't counted, so the numbers have no gaps
#show figure.where(kind: table): set figure.caption(position: top)
#show figure.where(kind: table, caption: none): it => {
  counter(figure.where(kind: table)).update(count => count - 1)
  align(center, it.body)
}

// Pandoc's Typst output uses this for a --- line
#let horizontalrule = line(length: 100%, stroke: 0.5pt + luma(170))

// Front cover, with no page number or header
#page(numbering: none, header: none)[
  #set align(center)
  #set par(justify: false)
  #v(1.5cm)
  #image("ul-logo.svg", width: 4.2cm)
  #v(1.2cm)
  #text(size: 13pt, details.module) \
  #text(size: 13pt, details.assignment)
  #v(1.6cm)
  #text(size: 26pt, weight: "bold", hyphenate: false, details.title)
  #v(1.6cm)
  #table(
    columns: (auto, auto),
    align: (left, left),
    stroke: none,
    inset: (x: 10pt, y: 6pt),
    table.header([*Name*], [*Student ID*]),
    ..details.members.map(member => (member.name, member.id)).flatten(),
  )
  #v(1fr)
  #details.group · Lecturer: #details.lecturer \
  #details.university · #details.semester \
  #link(details.repository)
  #v(1cm)
]

// The marking scheme as issued, blank, with only our names and IDs written in on its first page
#set page(numbering: "i")
#counter(page).update(1)
#for number in (1, 2) {
  page(margin: 0pt, numbering: none, header: none)[
    #image("marking-scheme.pdf", page: number, width: 100%)
    #if number == 1 {
      for (row, member) in details.members.enumerate() {
        place(top + left, dx: 70pt, dy: 98.5pt + row * 13.8pt, text(size: 10pt, font: "Libertinus Serif", member.name))
        place(top + left, dx: 282pt, dy: 98.5pt + row * 13.8pt, text(size: 10pt, font: "Libertinus Serif", member.id))
      }
    }
  ]
}

#outline(title: [Contents], depth: 2, indent: auto)

// Each numbered section starts on a new page. build.sh reads the <section> metadata to count each section's pages
#set heading(numbering: "1.1")
#show heading: set text(font: "Libertinus Serif")
#show heading.where(level: 1): it => {
  pagebreak(weak: true)
  context [#metadata((number: counter(heading).get().first(), page: here().page())) <section>]
  set text(size: 18pt)
  block(above: 0pt, below: 1.2em, it)
}
#show heading.where(level: 2): set text(size: 13.5pt)
#show heading.where(level: 3): set text(size: 11.5pt)

#set page(numbering: "1")
#counter(page).update(1)

#include "out/body.typ"

#context [#metadata((page: here().page())) <end>]
