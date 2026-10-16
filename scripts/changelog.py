#!/usr/bin/env python3
"""
Changelog tool for NorthLight.
Extracts release notes, validates changelog entries, bumps versions,
and provides automatic changelog generation from Conventional Commits.
"""

import sys
import os
import re
import subprocess
from datetime import datetime

CHANGELOG_PATH = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "CHANGELOG.md")

def read_changelog():
    if not os.path.exists(CHANGELOG_PATH):
        print(f"Error: {CHANGELOG_PATH} not found", file=sys.stderr)
        sys.exit(1)
    with open(CHANGELOG_PATH, "r", encoding="utf-8") as f:
        return f.read()

def clean_version(version_str):
    """Normalize version string by stripping 'v' prefix."""
    return version_str.lstrip("v").strip()

def extract_release_notes(version):
    version = clean_version(version)
    content = read_changelog()
    
    # Pattern to find the section for this version
    pattern = rf"##\s*\[{re.escape(version)}\][^\n]*\n(.*?)(?=\n##\s*\[|\Z)"
    match = re.search(pattern, content, re.DOTALL)
    
    if not match:
        print(f"Error: Version [{version}] not found in CHANGELOG.md", file=sys.stderr)
        sys.exit(1)
        
    notes = match.group(1).strip()
    print(notes)
    return notes

def validate_version(version):
    version = clean_version(version)
    content = read_changelog()
    pattern = rf"##\s*\[{re.escape(version)}\]"
    if re.search(pattern, content):
        print(f"Success: CHANGELOG.md contains entry for version [{version}]")
        sys.exit(0)
    else:
        print(f"Error: No entry found for [{version}] in CHANGELOG.md. Please update CHANGELOG.md before releasing.", file=sys.stderr)
        sys.exit(1)

def bump_version(new_version):
    new_version = clean_version(new_version)
    content = read_changelog()
    
    today = datetime.now().strftime("%Y-%m-%d")
    unreleased_pattern = r"(##\s*\[Unreleased\]\s*\n)(.*?)(?=\n##\s*\[|\Z)"
    match = re.search(unreleased_pattern, content, re.DOTALL)
    
    if not match:
        print("Error: Could not locate [Unreleased] section in CHANGELOG.md", file=sys.stderr)
        sys.exit(1)
        
    unreleased_body = match.group(2).strip()
    if not unreleased_body:
        unreleased_body = "- General improvements and bug fixes."
        
    new_section = f"## [Unreleased]\n\n## [{new_version}] - {today}\n\n{unreleased_body}\n"
    updated_content = re.sub(unreleased_pattern, new_section, content, count=1, flags=re.DOTALL)
    
    with open(CHANGELOG_PATH, "w", encoding="utf-8") as f:
        f.write(updated_content)
        
    print(f"Successfully bumped CHANGELOG.md to [{new_version}] - {today}")

def get_latest_tag():
    try:
        tag = subprocess.check_output(
            ["git", "describe", "--tags", "--abbrev=0"],
            stderr=subprocess.DEVNULL
        ).decode("utf-8").strip()
        return clean_version(tag)
    except Exception:
        return "1.0.0"

def parse_semver(version_str):
    clean = clean_version(version_str)
    parts = clean.split(".")
    major = int(parts[0]) if len(parts) > 0 and parts[0].isdigit() else 1
    minor = int(parts[1]) if len(parts) > 1 and parts[1].isdigit() else 0
    patch = int(parts[2]) if len(parts) > 2 and parts[2].isdigit() else 0
    return major, minor, patch

def auto_generate():
    """
    Analyzes git commits since the last tag and automatically:
    1. Determines next SemVer (major/minor/patch) based on Conventional Commits.
    2. Groups commits into Added, Fixed, Changed.
    3. Updates CHANGELOG.md automatically.
    """
    last_tag = get_latest_tag()
    major, minor, patch = parse_semver(last_tag)

    # Get commit messages since last tag
    try:
        raw_log = subprocess.check_output(
            ["git", "log", f"v{last_tag}..HEAD", "--pretty=format:%s"],
            stderr=subprocess.DEVNULL
        ).decode("utf-8").strip()
    except Exception:
        raw_log = subprocess.check_output(
            ["git", "log", "-n", "30", "--pretty=format:%s"]
        ).decode("utf-8").strip()

    commits = [line.strip() for line in raw_log.split("\n") if line.strip()]

    added = []
    fixed = []
    changed = []
    has_breaking = False
    has_feat = False

    for c in commits:
        if "BREAKING CHANGE" in c or "!:" in c:
            has_breaking = True
        if c.startswith("feat"):
            has_feat = True
            msg = re.sub(r"^feat(\([^)]+\))?:\s*", "", c)
            added.append(f"- {msg}")
        elif c.startswith("fix"):
            msg = re.sub(r"^fix(\([^)]+\))?:\s*", "", c)
            fixed.append(f"- {msg}")
        elif c.startswith(("refactor", "perf", "chore", "ci")):
            msg = re.sub(r"^(refactor|perf|chore|ci)(\([^)]+\))?:\s*", "", c)
            changed.append(f"- {msg}")

    # Determine next version
    if has_breaking:
        next_version = f"{major + 1}.0.0"
    elif has_feat:
        next_version = f"{major}.{minor + 1}.0"
    else:
        next_version = f"{major}.{minor}.{patch + 1}"

    # Build section
    lines = []
    if added:
        lines.append("### Added\n" + "\n".join(added))
    if fixed:
        lines.append("### Fixed\n" + "\n".join(fixed))
    if changed:
        lines.append("### Changed\n" + "\n".join(changed))
    if not lines:
        lines.append("- Maintenance, dependency updates, and optimizations.")

    section_body = "\n\n".join(lines)
    today = datetime.now().strftime("%Y-%m-%d")

    content = read_changelog()
    new_release_header = f"## [{next_version}] - {today}\n\n{section_body}\n"
    
    unreleased_pattern = r"(##\s*\[Unreleased\]\s*\n)"
    updated_content = re.sub(unreleased_pattern, rf"\1\n{new_release_header}\n", content, count=1)

    with open(CHANGELOG_PATH, "w", encoding="utf-8") as f:
        f.write(updated_content)

    print(f"NEXT_VERSION={next_version}")
    return next_version

def get_latest_version():
    content = read_changelog()
    pattern = r"##\s*\[([0-9]+\.[0-9]+\.[0-9]+[^\]]*)\]"
    match = re.search(pattern, content)
    if match:
        version = match.group(1).strip()
        print(version)
        return version
    else:
        print("Error: No release version found in CHANGELOG.md", file=sys.stderr)
        sys.exit(1)

def main():
    if len(sys.argv) < 2:
        print("Usage: changelog.py <extract|validate|bump|auto|latest> [version]")
        sys.exit(1)
        
    command = sys.argv[1].lower()
    
    if command == "auto":
        auto_generate()
        return

    if command == "latest":
        get_latest_version()
        return

    if len(sys.argv) < 3:
        print(f"Usage: changelog.py {command} <version>")
        sys.exit(1)

    version = sys.argv[2]
    if command == "extract":
        extract_release_notes(version)
    elif command == "validate":
        validate_version(version)
    elif command == "bump":
        bump_version(version)
    else:
        print(f"Unknown command: {command}")
        sys.exit(1)

if __name__ == "__main__":
    main()

