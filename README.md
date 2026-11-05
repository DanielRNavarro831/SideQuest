# SideQuest

A personal-growth tracker that turns chores and habits into RPG skills. Everything stays on the phone; no network access.

## Build

Needs JDK 17 or 21 and the Android SDK (`ANDROID_HOME`). Android Studio bundles both.

```bash
./gradlew testDebugUnitTest assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

## Install on a phone

Enable Developer options → USB debugging (or Wireless debugging), connect, then:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Layout

| Path | What |
|------|------|
| `model/` | Pure Kotlin rules: skills, XP/levels, overall level, text validation, step math. Unit tested in `app/src/test`. |
| `data/Repository.kt` | In-memory state saved to a JSON file in app storage. |
| `steps/` | Step counter foreground service, boot receiver, permission helpers. |
| `ui/` | Jetpack Compose screens: parchment scroll, profile header, skill list/detail/editor. |

## Adding art

Art is placeholder emoji until real images exist. Drop PNGs into `app/src/main/res/drawable/` with these names and they're used automatically (scaled with crisp pixels):

- Heroes: `hero_warrior`, `hero_wizard`, `hero_archer`, `hero_rogue`, `hero_bard`
- Skill icons: `skill_footsteps`, `skill_sword`, `skill_spellbook`, `skill_scroll`, `skill_dishes`, `skill_broom`, `skill_d20`, `skill_training_dummy`, `skill_shield`, `skill_dragon`, `skill_bow_and_arrow`, `skill_quill_and_parchment`, `skill_potion`

## Before publishing to the Play Store

- `applicationId` in `app/build.gradle.kts` is permanent once published.
- Create a release signing key and back it up; losing it means no more updates.
- The step service uses the `health` foreground service type, which Play asks you to justify in the console.
