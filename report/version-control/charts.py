# /// script
# requires-python = ">=3.12"
# dependencies = ["matplotlib==3.10.7"]
# ///
"""
Draws the two version control charts in §8 from the git history of main: commits per week by member
(contributors.png) and pull requests merged into main over time, by member
(pulls.png). A pull request counts for the member who wrote most of its commits.

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

# Each member's git e-mail addresses, as they appear in the history
MEMBERS = {
    'Chirag Aggarwal': ['chiragaggarwal5k@gmail.com'],
    'Dhruv Punj': ['dpunj810@gmail.com', '124335534+DHRUVx22@users.noreply.github.com'],
    'Bharat Doodi': ['107737002+AKABharat@users.noreply.github.com'],
    'DineshReddy Mogili': [],
}
COLOURS = {
    'Chirag Aggarwal': '#2a6fdb',
    'Dhruv Punj': '#e07b39',
    'Bharat Doodi': '#3a9e5c',
    'DineshReddy Mogili': '#9b59b6',
}


def git(*arguments: str) -> str:
    return subprocess.run(['git', *arguments], check=True, capture_output=True, text=True).stdout


def member(email: str) -> str | None:
    for name, emails in MEMBERS.items():
        if email in emails:
            return name
    return None


def commits(revisions: str) -> list[tuple[datetime, str]]:
    found = []
    for line in git('log', revisions, '--no-merges', '--format=%aI %ae').splitlines():
        moment, email = line.split(' ', 1)
        name = member(email)
        if name:
            found.append((datetime.fromisoformat(moment), name))
    return found


def monday(moment: datetime) -> date:
    return moment.date() - timedelta(days=moment.weekday())


def contributors() -> None:
    history = commits('origin/main')
    first = monday(min(day for day, _ in history))
    last = monday(max(day for day, _ in history))
    weeks = [first + timedelta(weeks=index) for index in range((last - first).days // 7 + 1)]
    counts = Counter((monday(day), name) for day, name in history)
    totals = Counter(name for _, name in history)

    figure, axes = pyplot.subplots(figsize=(8, 3.2))
    bottom = [0] * len(weeks)
    for name in MEMBERS:
        heights = [counts[(week, name)] for week in weeks]
        axes.bar(weeks, heights, width=5, bottom=bottom, color=COLOURS[name], label=f'{name} ({totals[name]})')
        bottom = [low + height for low, height in zip(bottom, heights)]
    axes.set_ylabel('Commits')
    axes.set_title('Commits to main per week, by member (merge commits left out)', fontsize=10)
    axes.set_xticks(weeks)
    axes.xaxis.set_major_formatter(dates.DateFormatter('%d %b'))
    axes.legend(frameon=False, fontsize=8)
    axes.spines[['top', 'right']].set_visible(False)
    figure.tight_layout()
    figure.savefig(HERE / 'contributors.png', dpi=200)


def pulls() -> None:
    merged: dict[str, list[datetime]] = {name: [] for name in MEMBERS}
    for line in git('log', 'origin/main', '--first-parent', '--reverse', '--format=%H %cI %P|%ae|%s').splitlines():
        head, email, subject = line.split('|', 2)
        commit, moment, *parents = head.split()
        if len(parents) > 1:
            branch = commits(f'{commit}^1..{commit}^2')
            pull = subject.startswith('Merge pull request #') and branch
            author = Counter(name for _, name in branch).most_common(1)[0][0] if pull else None
        else:
            # A squash merge is one commit on main whose subject ends in the pull request's number, e.g. (#27)
            author = member(email) if re.search(r'\(#\d+\)$', subject) else None
        if author:
            merged[author].append(datetime.fromisoformat(moment))
    first = min(moment for moments in merged.values() for moment in moments)
    last = max(moment for moments in merged.values() for moment in moments)

    figure, axes = pyplot.subplots(figsize=(8, 3.2))
    for name, moments in merged.items():
        axes.step([first, *moments, last], [0, *range(1, len(moments) + 1), len(moments)], where='post',
                  color=COLOURS[name], linewidth=1.8, label=f'{name} ({len(moments)})')
    axes.set_ylabel('Pull requests merged')
    axes.set_title('Pull requests merged into main over time, by member', fontsize=10)
    axes.xaxis.set_major_formatter(dates.DateFormatter('%d %b'))
    axes.legend(frameon=False, fontsize=8, loc='upper left')
    axes.spines[['top', 'right']].set_visible(False)
    figure.tight_layout()
    figure.savefig(HERE / 'pulls.png', dpi=200)


contributors()
pulls()
