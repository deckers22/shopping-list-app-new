# Shopping List

Android app (Kotlin, Jetpack Compose, Room) for a shopping list, recipes and standard groceries.

- **List** – add, edit, check off, delete and drag-to-reorder items.
- **Recipes** – ingredients with amounts for a number of people; change the number of
  people to rescale; one button puts all ingredients on the shopping list.
- **Staples** – standard groceries; tap one (or "Add all to list") to put it on the list.
- **Backup** – save all data to a JSON file and restore it later.

## Getting the app

Every push builds the app and publishes the newest APK on the repository's **Releases**
page: <https://github.com/deckers22/shopping-list-app-new/releases/latest>. Open that
page on your phone, tap `shopping-list.apk` and open the downloaded file to install.

Locally: open the project in Android Studio, or run `./gradlew assembleRelease`
(needs the Android SDK and JDK 17).

## Updating without losing data

- Data lives in an on-device database; installing a newer APK over the old one keeps it.
- Android refuses to update an app with an APK signed by a different key, so all builds
  must use the same signing key (see below). Keep that key and its password safe.
- When changing a database table, raise the version in `AppDatabase.kt` and add a Room
  `Migration`. Never use destructive migration.
- Before risky changes, use **Backup → Save backup file** in the app.

## Signing key (one-time setup)

The key is deliberately not stored in the repository. Create one once:

```
keytool -genkeypair -keystore app.keystore -alias app -keyalg RSA -keysize 2048 -validity 36500
```

Then add three repository secrets (**Settings → Secrets and variables → Actions**):

| Secret | Value |
| --- | --- |
| `KEYSTORE_BASE64` | output of `base64 -w0 app.keystore` |
| `KEYSTORE_PASSWORD` | the password you chose |
| `KEY_ALIAS` | `app` |

Without these secrets the workflow still builds an installable APK, but each one is signed
with a throwaway key, so it can only be installed after uninstalling the previous version
(which deletes the app's data - save a backup file first).

For local builds, set `KEYSTORE_FILE` (path to the keystore), `KEYSTORE_PASSWORD` and
`KEY_ALIAS` as environment variables.
