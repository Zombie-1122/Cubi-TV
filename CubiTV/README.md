# Cubi TV (phone + Android TV)

Kotlin + Jetpack Compose + Media3. One APK for phone and TV, with in-app auto update from GitHub Releases.

## Build with GitHub (no Android Studio needed)
1. Create a **public** GitHub repo and upload this whole folder (or `git push` it).
2. Go to **Actions**: every push to `main` builds an APK (download it from the run's *Artifacts*).
3. To publish a release that the app can auto-update to, push a version tag:
   ```
   git tag v1.0.1
   git push origin v1.0.1
   ```
   (or GitHub > Releases > Draft a new release > create tag `v1.0.1` > Publish).
   The workflow builds `CubiTV-1.0.1.apk` and attaches it to the release.
4. Install that APK once on phone/TV. After that, the app checks the latest Release on launch
   and shows an **Update now** dialog when a newer tag exists.

The repo name is filled in automatically by the workflow (`-PgithubRepo`), nothing to edit.
For local builds, set `githubRepo=you/CubiTV` in `gradle.properties`.

Notes
- First update asks you to allow "install unknown apps" for Cubi TV (Android requirement).
- Updates only install over the previous version if signed with the same key. The key is
  `keystore/cubitv.jks` (password `cubitv123`). It is public in your repo, so for a
  private key: `keytool -genkeypair -keystore my.jks -alias myalias -keyalg RSA -keysize 2048 -validity 36500`,
  then add repo secrets `KEYSTORE_BASE64` (`base64 -w0 my.jks`), `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
  Never switch keys after users installed, or updates will fail.

## Local build
Open in Android Studio and run, or `gradle assembleRelease` (Gradle 8.9, JDK 17).

## Add a source
**Code:** copy `source/DemoSource.kt`, implement `AnimeSource`, add it to `SourceRegistry.all`.

**JSON catalog (no code):** host a JSON file and add
`GenericJsonSource("mycatalog", "My Catalog", "https://.../catalog.json")` to `SourceRegistry`.

```json
{ "anime": [ { "id": "1", "title": "Name", "cover": "https://.../cover.jpg",
  "description": "...",
  "episodes": [ { "id": "1", "number": 1, "title": "Episode 1",
    "streams": [ { "url": "https://.../index.m3u8", "quality": "1080p" } ] } ] } ] }
```

Only use sources you are licensed or allowed to stream from.
