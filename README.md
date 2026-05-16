# Glass Calc

A modern Android calculator with a glassmorphism aesthetic — translucent pill-shaped keys floating over a deep gradient with soft glowing orbs. Built with Kotlin + Jetpack Compose + Material 3.

Operations: `+  −  ×  ÷`, `%`, `±` (sign flip), `.` (decimal), `C` (clear), `=`.
All arithmetic uses `BigDecimal` so you won't see `0.1 + 0.2 = 0.30000000000000004`.

---

## Build the APK

### Option A — GitHub Actions (no install needed, recommended)

The project includes a `.github/workflows/build.yml` that builds the APK on GitHub's servers for free. You never touch Android Studio.

1. Sign up for a free GitHub account at https://github.com if you don't have one.
2. Click the "+" in the top right → **New repository**. Pick any name (e.g. `glass-calc`). Leave it public or private — both work. Don't add a README or `.gitignore` from the GitHub side (this project already has those). Click **Create repository**.
3. On the next page, click the **uploading an existing file** link (it's in the "Quick setup" section). Drag and drop *every file and folder* from the unzipped `GlassCalculator` directory onto the page. Make sure the hidden `.github` folder comes along — on macOS Finder you may need ⌘⇧. to show hidden folders before you can drag them; on Windows Explorer enable "Show hidden files" in View. Then click **Commit changes**.
4. Click the **Actions** tab at the top of the repo. You'll see a workflow run called "Build APK" — it starts automatically after the upload. Wait ~3–5 minutes for it to finish (green checkmark).
5. Click into the finished run. Scroll to the bottom — there's an **Artifacts** section with `GlassCalculator-debug-apk`. Click it to download a zip. Inside the zip is `app-debug.apk`. That's your APK.
6. Transfer the APK to your phone (Drive, email, USB) and follow the install steps below.

If the workflow fails, click into the failed step to see the log — common issues are a missing file from the upload (drag-and-drop sometimes skips empty folders or `.github`). The `.github/workflows/build.yml` file *must* be there.

### Option B — Android Studio (easiest if you have it / want it)

1. Install [Android Studio](https://developer.android.com/studio) (Hedgehog 2023.1.1 or newer).
2. Open Android Studio → **File → Open…** → pick this `GlassCalculator` folder.
3. Wait for the first Gradle sync. It will download the Android Gradle Plugin, Kotlin, Compose, etc. (a few minutes the first time).
4. When sync finishes, in the menu: **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
5. A toast appears at the bottom right with a **locate** link. Click it. The file you want is:

   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

That APK is the file you install on your phone. It is **debug-signed**, which is fine for personal use — Android will warn you it's from an unknown developer when you install it. You'll need to allow that.

### Option C — Command line

You need JDK 17 and the Android SDK installed locally. Then from this folder:

```bash
# If there's no gradle/wrapper/gradle-wrapper.jar yet, generate the wrapper once:
gradle wrapper --gradle-version 8.6

# Then build:
./gradlew assembleDebug
```

APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Install on your Android phone

There are three ways. Pick whichever is easiest for you.

### 1. ADB (one command — recommended if you already have `adb`)

1. On your phone, enable **Developer options** → **USB debugging** (tap "Build number" 7 times in Settings → About phone if developer options aren't visible).
2. Plug the phone into your computer with a USB cable. Accept the "Allow USB debugging" prompt on the phone.
3. Run:

   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### 2. Drag-and-drop sideload

1. Copy `app-debug.apk` to your phone (Drive, AirDroid, USB transfer, email it to yourself, whatever).
2. Open it from your phone's file manager.
3. Android will prompt: *"For your security, your phone isn't allowed to install unknown apps from this source."* Tap **Settings** → enable **Allow from this source** → back out → tap the APK again → **Install**.

### 3. Direct from Android Studio (if your phone is plugged in)

With your phone plugged in and USB debugging on, just click the green ▶ **Run 'app'** button in Android Studio. It builds + installs + launches in one step.

---

## Project layout

```
GlassCalculator/
├── app/
│   ├── build.gradle.kts                    # App module config (SDK levels, deps)
│   └── src/main/
│       ├── AndroidManifest.xml             # App entry point
│       ├── java/com/calc/glass/
│       │   ├── MainActivity.kt             # Activity hosting the Compose UI
│       │   ├── CalculatorEngine.kt         # State machine + BigDecimal math
│       │   └── ui/
│       │       ├── CalculatorScreen.kt     # Layout: background, display, keypad
│       │       ├── GlassButton.kt          # One translucent pill key
│       │       └── Theme.kt                # Material 3 color scheme
│       └── res/
│           ├── values/                     # Strings, theme, colors
│           ├── values-night/               # Dark theme override
│           ├── drawable/                   # Vector launcher art
│           └── mipmap-anydpi-v26/          # Adaptive launcher icon
├── build.gradle.kts                        # Root build script (plugin versions)
├── settings.gradle.kts                     # Module list + repos
├── gradle.properties                       # Gradle JVM args + AndroidX flags
└── gradle/wrapper/gradle-wrapper.properties # Pins Gradle 8.6
```

## Version pins

These are the combination I picked because they're known-good together. If you change one, you may need to change the others.

| Component         | Version  |
|-------------------|----------|
| Gradle            | 8.6      |
| Android Gradle Plugin | 8.4.0 |
| Kotlin            | 1.9.23   |
| Compose Compiler  | 1.5.11   |
| Compose BOM       | 2024.04.01 |
| Compile SDK       | 34       |
| Min SDK           | 26 (Android 8.0) |
| Target SDK        | 34       |
| JDK               | 17       |

## Tweaks you might want

- **Change app name**: edit `app/src/main/res/values/strings.xml` → `app_name`.
- **Change package id**: edit `applicationId` in `app/build.gradle.kts` and `namespace`. Move source folders to match.
- **Change gradient colors**: `BackgroundGradient()` in `CalculatorScreen.kt` and the launcher gradient in `drawable/ic_launcher_background.xml`.
- **Add backspace key**: the engine already supports `Action.Backspace` — just add a `Key("⌫", ButtonAccent.Function) { onAction(Action.Backspace) }` to the keypad.
