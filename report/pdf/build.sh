#!/usr/bin/env bash
# Builds report/pdf/out/report.pdf from the section files in report/sections, in file-name order, then lists how
# many pages each section takes and fails if a section is over the spec's page limit. Needs pandoc and typst.
set -euo pipefail

cd "$(git rev-parse --show-toplevel)"
here=report/pdf
out=$here/out
rm -rf "$out"
mkdir -p "$out"

: > "$out/body.typ"
for section in report/sections/[0-9]*.md; do
  pandoc "$section" --from markdown --to typst --lua-filter "$here/report.lua" --wrap none --columns 100000 >> "$out/body.typ"
  printf '\n' >> "$out/body.typ"
done

typst compile --root . --ignore-system-fonts "$here/report.typ" "$out/report.pdf"

# The spec's page limits, by section number
limit() {
  case $1 in
    1 | 11) echo 1 ;;
    2 | 5) echo 2 ;;
  esac
}

starts=$(typst eval --root . --ignore-system-fonts --in "$here/report.typ" 'query(<section>).map(it => it.value)')
end=$(typst eval --root . --ignore-system-fonts --in "$here/report.typ" 'query(<end>).first().value.page')
summary=$(jq -r --argjson end "$end" '
  [.[] | {number, page}] as $sections
  | range(0; $sections | length) as $index
  | $sections[$index] as $section
  | (if $index + 1 < ($sections | length) then $sections[$index + 1].page else $end + 1 end) as $next
  | "\($section.number) \($next - $section.page)"' <<< "$starts")

over=0
echo "| Section | Pages | Limit |"
echo "| --- | --- | --- |"
while read -r number pages; do
  limit=$(limit "$number")
  flag=''
  if [[ -n $limit && $pages -gt $limit ]]; then
    flag=' (over)'
    over=1
  fi
  echo "| §$number | $pages | ${limit:-none}$flag |"
done <<< "$summary"
echo
echo "Total: $end pages, $out/report.pdf"

missing=$({ grep -B1 "id: ''" "$here/report.yaml" || true; } | sed -n 's/.*name: //p')
if [[ -n $missing ]]; then
  echo "Student IDs still missing in $here/report.yaml for: $(paste -sd, - <<< "$missing")"
fi

exit $over
