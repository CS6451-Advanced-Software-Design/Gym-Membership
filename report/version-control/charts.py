# /// script
# requires-python = ">=3.12"
# dependencies = ["matplotlib==3.10.7"]
# ///
"""
Draws the two version control charts in §8 from the git history of main: commits per week (commits.png) and pull
requests merged into main over time (pulls.png), with the day main started to require a pull request and passing tests.

Run from the repository root: uv run report/version-control/charts.py
"""
import re
import subprocess
from collections import Counter
from datetime import date, datetime, timedelta
from pathlib import Path

import matplotlib

matplotlib.use('Agg')
import matplotlib.dates as dates
import matplotlib.pyplot as pyplot

HERE = Path(__file__).parent
COLOUR = '#2a6fdb'

# The day the ruleset on main started requiring a pull request and the three test jobs
PROTECTED = datetime.fromisoformat('2026-10-25T07:35:17+00:00')


def git(*arguments: str) -> str:
    return subprocess.run(['git', *arguments], check=True, capture_output=True, text=True).stdout


def monday(day: date) -> date:
    return day - timedelta(days=day.weekday())


def commits() -> None:
    days = [date.fromisoformat(day) for day in git('log', 'origin/main', '--no-merges', '--format=%as').split()]
    first = monday(min(days))
    last = monday(max(days))
    weeks = [first + timedelta(weeks=index) for index in range((last - first).days // 7 + 1)]
    counts = Counter(monday(day) for day in days)

    figure, axes = pyplot.subplots(figsize=(8, 3))
    axes.bar(weeks, [counts[week] for week in weeks], width=5, color=COLOUR)
    axes.set_ylabel('Commits')
    axes.set_title(f'Commits to main per week ({len(days)} in all, merge commits left out)', fontsize=10)
    axes.set_xticks(weeks)
    axes.xaxis.set_major_formatter(dates.DateFormatter('%d %b'))
    axes.spines[['top', 'right']].set_visible(False)
    figure.tight_layout()
    figure.savefig(HERE / 'commits.png', dpi=200)


def pulls() -> None:
    merged = []
    for line in git('log', 'origin/main', '--first-parent', '--reverse', '--format=%cI %P|%s').splitlines():
        head, subject = line.split('|', 1)
        moment, *parents = head.split()
        # A merge commit names its pull request; a squash merge ends in its number, e.g. (#27)
        if subject.startswith('Merge pull request #') or (len(parents) == 1 and re.search(r'\(#\d+\)$', subject)):
            merged.append(datetime.fromisoformat(moment))

    figure, axes = pyplot.subplots(figsize=(8, 3))
    axes.step(merged, range(1, len(merged) + 1), where='post', color=COLOUR, linewidth=1.8)
    axes.axvline(PROTECTED, color='#888888', linestyle='--', linewidth=1)
    axes.annotate('main requires a pull request\nand passing tests', (PROTECTED, len(merged) * 0.8),
                  xytext=(-8, 0), textcoords='offset points', ha='right', fontsize=8, color='#555555')
    axes.set_ylabel('Pull requests merged')
    axes.set_title(f'Pull requests merged into main over time ({len(merged)} in all)', fontsize=10)
    axes.xaxis.set_major_formatter(dates.DateFormatter('%d %b'))
    axes.spines[['top', 'right']].set_visible(False)
    figure.tight_layout()
    figure.savefig(HERE / 'pulls.png', dpi=200)


commits()
pulls()
