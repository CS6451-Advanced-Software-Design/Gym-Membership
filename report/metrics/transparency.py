"""Builds the §7 tables from cloc (lines of code) and git blame (authorship). Run from the repo root."""
import collections, csv, io, subprocess, sys

CLOC = sys.argv[1]
OUT = sys.argv[2]
MEMBERS = {
    'Chirag Aggarwal': 'Chirag', 'BhΔ∇Δt': 'Bharat', 'dhruv punj': 'Dhruv', 'Dhruv Punj': 'Dhruv',
    'sanjuD1603': 'DineshReddy', 'Mogili Dinesh Reddy': 'DineshReddy',
}
ORDER = ['Chirag', 'Bharat', 'DineshReddy', 'Dhruv']

def run(*args):
    return subprocess.run(args, capture_output=True, text=True, encoding='utf8', check=True).stdout

files = [f for f in run('git', 'ls-files', 'code/src').splitlines() if f.endswith('.java')]

# cloc: code lines per file (blank and comment lines left out)
report = run(CLOC, '--by-file', '--csv', '--quiet', '--include-lang=Java', *files)
code_lines = {}
for row in csv.reader(io.StringIO(report)):
    if len(row) >= 5 and row[0] == 'Java':
        code_lines[row[1].replace('\\', '/')] = int(row[4])

def blame(path):
    """Non-blank lines per member, ignoring whitespace changes and following moved lines within a file."""
    counts = collections.Counter()
    author = None
    for line in run('git', 'blame', '-w', '-M', '--line-porcelain', 'HEAD', '--', path).splitlines():
        if line.startswith('author '):
            author = MEMBERS.get(line[7:], line[7:])
        elif line.startswith('\t') and line[1:].strip():
            counts[author] += 1
    return counts

rows = []
for path in files:
    parts = path.split('/')
    kind = parts[2]                                   # main or test
    package = '.'.join(parts[5:-1]) or '(root)'       # under code/src/<kind>/java/gym/
    counts = blame(path)
    total = sum(counts.values())
    owner, lines = counts.most_common(1)[0]
    rows.append(dict(path=path, kind=kind, package=package, name=parts[-1][:-5], loc=code_lines[path],
                     owner=owner, share=round(100 * lines / total), counts=counts))

out = []
w = out.append

# Table 1: classes per package
w('| Package | Types | Lines of code |\n| --- | --- | --- |')
by_package = collections.OrderedDict()
for r in sorted((r for r in rows if r['kind'] == 'main'), key=lambda r: r['package']):
    p = by_package.setdefault(r['package'], [0, 0])
    p[0] += 1
    p[1] += r['loc']
for package, (types, loc) in by_package.items():
    w(f'| `gym.{package}` | {types} | {loc} |' if package != '(root)' else f'| `gym` | {types} | {loc} |')
main = [r for r in rows if r['kind'] == 'main']
test = [r for r in rows if r['kind'] == 'test']
w(f'| **Main total** | **{len(main)}** | **{sum(r["loc"] for r in main)}** |')
w(f'| Tests (`code/src/test`) | {len(test)} | {sum(r["loc"] for r in test)} |')
w(f'| **All** | **{len(rows)}** | **{sum(r["loc"] for r in rows)}** |')
w('')

# Table 2: every class with its author and LOC
w('| Package | Class | Main author (share of lines) | Lines of code |\n| --- | --- | --- | --- |')
for r in sorted(rows, key=lambda r: (r['kind'] != 'main', r['package'], r['name'])):
    package = ('test: ' if r['kind'] == 'test' else '') + (r['package'] if r['package'] != '(root)' else 'gym')
    w(f'| {package} | {r["name"]} | {r["owner"]} ({r["share"]}%) | {r["loc"]} |')
w('')

# Table 3: lines per member
totals = {kind: collections.Counter() for kind in ('main', 'test')}
for r in rows:
    totals[r['kind']].update(r['counts'])
everyone = totals['main'] + totals['test']
all_lines = sum(everyone.values())
w('| Member | Main code lines | Test code lines | Total | Share |\n| --- | --- | --- | --- | --- |')
for m in ORDER + sorted(set(everyone) - set(ORDER)):
    w(f'| {m} | {totals["main"][m]} | {totals["test"][m]} | {everyone[m]} | {round(100 * everyone[m] / all_lines, 1)}% |')
w(f'| **All** | **{sum(totals["main"].values())}** | **{sum(totals["test"].values())}** | **{all_lines}** | **100%** |')

open(OUT, 'w', encoding='utf8', newline='\n').write('\n'.join(out) + '\n')
print(run('git', 'rev-parse', '--short', 'HEAD').strip(), len(rows), 'files')
