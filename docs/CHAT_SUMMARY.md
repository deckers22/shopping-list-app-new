# Running summary

Lean log of the project discussion, so a new chat can pick up where the last one stopped.

## Goal
Personal Android app: shopping list (edit, delete, reorder), recipes with ingredient
amounts per number of people (rescalable), add a recipe's ingredients to the shopping
list, a list of standard groceries to add quickly, and data that survives app updates.

## Decisions (2026-11-10)
- Stack: Kotlin + Jetpack Compose + Room (SQLite). Single `app` module, min Android 8.0.
- Backup: on-device database + manual JSON export/import + Android auto-backup enabled.
- Duplicates: adding a recipe/staple always creates separate lines (no merging);
  recipe items show "for <recipe name>".
- UI language: English. This summary lives in this markdown file.

## Assumptions made
- Single user, single phone; no sync or sharing.
- Changing the number of people on a recipe rescales the view and what is added to the
  list; the stored recipe keeps its original amounts.
- Ingredient amount and unit are optional (e.g. "salt").
- "Staples" = the standard groceries list; tapping a staple adds it to the list.
- Restoring a backup replaces all current data (no merge).
- Reordering is offered for the shopping list and staples, not for recipe ingredients.

## State
- First version implemented; see README for structure and how to get the APK.
- The cloud dev environment cannot download the Android SDK (dl.google.com blocked),
  so builds are verified by the GitHub Actions workflow `Build APK`, not locally.
- Not yet tested on a real device.

## Open
- Signing key: updates only keep data if every APK is signed with the same key. The key
  is not in the repo; the workflow reads it from repository secrets (README, "Signing
  key"). Until the secrets exist, each CI APK has a throwaway signature.

## Ideas not built
- Merge duplicate items, categories/shop order, reorder ingredients, sharing/sync,
  GitHub Release with APK for easier phone download.
