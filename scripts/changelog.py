#!/usr/bin/env python3
"""
Changelog tool for NorthLight.
Extracts release notes, validates changelog entries, and bumps versions.
"""

import sys
import os
import re
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
        unreleased_body = "- Maintenance and dependency updates."
        
    new_section = f"## [Unreleased]\n\n## [{new_version}] - {today}\n\n{unreleased_body}\n"
    updated_content = re.sub(unreleased_pattern, new_section, content, count=1, flags=re.DOTALL)
    
    with open(CHANGELOG_PATH, "w", encoding="utf-8") as f:
        f.write(updated_content)
        
    print(f"Successfully bumped CHANGELOG.md to [{new_version}] - {today}")

def main():
    if len(sys.argv) < 3:
        print("Usage: changelog.py <extract|validate|bump> <version>")
        sys.exit(1)
        
    command = sys.argv[1].lower()
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
