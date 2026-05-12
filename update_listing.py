#!/usr/bin/env python3
"""
Update Google Play Store listing (title, short description, full description)
for each locale from local text files.

Usage:
    python3 update_listing.py [--dry-run]

Reads from:
    play_listing/<locale>/title.txt
    play_listing/<locale>/short_description.txt
    play_listing/<locale>/full_description.txt

Where <locale> is e.g. de-DE, en-US.
"""
import argparse
import sys
from pathlib import Path

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError

PACKAGE_NAME = "com.ywesee.parados"
SERVICE_ACCOUNT_FILE = Path(__file__).parent / "parados.json"
LISTING_DIR = Path(__file__).parent / "play_listing"

# Play Store limits
LIMIT_TITLE = 30
LIMIT_SHORT = 80
LIMIT_FULL = 4000


def read_listing(locale_dir: Path) -> dict:
    title = (locale_dir / "title.txt").read_text(encoding="utf-8").strip()
    short = (locale_dir / "short_description.txt").read_text(encoding="utf-8").strip()
    full = (locale_dir / "full_description.txt").read_text(encoding="utf-8").rstrip()

    if len(title) > LIMIT_TITLE:
        raise ValueError(f"{locale_dir.name}/title.txt: {len(title)} chars > limit {LIMIT_TITLE}")
    if len(short) > LIMIT_SHORT:
        raise ValueError(f"{locale_dir.name}/short_description.txt: {len(short)} chars > limit {LIMIT_SHORT}")
    if len(full) > LIMIT_FULL:
        raise ValueError(f"{locale_dir.name}/full_description.txt: {len(full)} chars > limit {LIMIT_FULL}")

    return {"title": title, "shortDescription": short, "fullDescription": full}


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dry-run", action="store_true",
                        help="Validate and print what would be uploaded, but do not commit")
    args = parser.parse_args()

    if not SERVICE_ACCOUNT_FILE.exists():
        print(f"Service account file not found: {SERVICE_ACCOUNT_FILE}", file=sys.stderr)
        return 1

    locale_dirs = sorted(d for d in LISTING_DIR.iterdir() if d.is_dir())
    if not locale_dirs:
        print(f"No locales found in {LISTING_DIR}", file=sys.stderr)
        return 1

    listings = {}
    for d in locale_dirs:
        try:
            listings[d.name] = read_listing(d)
        except (FileNotFoundError, ValueError) as e:
            print(f"✗ {e}", file=sys.stderr)
            return 1

    for locale, body in listings.items():
        print(f"--- {locale} ---")
        print(f"title ({len(body['title'])}/{LIMIT_TITLE}): {body['title']}")
        print(f"short ({len(body['shortDescription'])}/{LIMIT_SHORT}): {body['shortDescription']}")
        print(f"full  ({len(body['fullDescription'])}/{LIMIT_FULL} chars)")
        print()

    if args.dry_run:
        print("Dry run — no changes pushed.")
        return 0

    credentials = service_account.Credentials.from_service_account_file(
        str(SERVICE_ACCOUNT_FILE),
        scopes=["https://www.googleapis.com/auth/androidpublisher"],
    )
    service = build("androidpublisher", "v3", credentials=credentials)

    edit_id = None
    try:
        edit = service.edits().insert(body={}, packageName=PACKAGE_NAME).execute()
        edit_id = edit["id"]

        for locale, body in listings.items():
            service.edits().listings().update(
                editId=edit_id,
                packageName=PACKAGE_NAME,
                language=locale,
                body=body,
            ).execute()
            print(f"✓ Listing updated for {locale}")

        commit = service.edits().commit(editId=edit_id, packageName=PACKAGE_NAME).execute()
        print(f"✓ Edit #{commit['id']} has been committed")
        return 0

    except HttpError as e:
        if edit_id:
            try:
                service.edits().delete(editId=edit_id, packageName=PACKAGE_NAME).execute()
            except Exception:
                pass
        print(f"✗ Update failed: {e._get_reason()}", file=sys.stderr)
        return 1
    except Exception as e:
        if edit_id:
            try:
                service.edits().delete(editId=edit_id, packageName=PACKAGE_NAME).execute()
            except Exception:
                pass
        print(f"✗ Unexpected error: {e}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
