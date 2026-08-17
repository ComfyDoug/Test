# YouTube Auto Skip

A tiny personal-use Android app that uses an AccessibilityService restricted to the official YouTube Android package (`com.google.android.youtube`). When YouTube exposes a visible skip-ad control in the accessibility tree, the service attempts to click that control.

## Privacy / scope

- No `INTERNET` permission.
- Accessibility events are restricted to YouTube in the service configuration.
- No screen capture, OCR, analytics, accounts, or network requests.
- No coordinate-based tapping.
- The service must be manually enabled in Android Accessibility settings.

## Build

Requirements:

- Recent Android Studio compatible with Android Gradle Plugin 9.2
- Android SDK 36
- JDK supported by that Android Studio/AGP version

Open this folder in Android Studio and let it sync. Then choose:

`Build > Build APK(s)`

The debug APK is normally written to:

`app/build/outputs/apk/debug/app-debug.apk`

You can also build from a terminal once Gradle is available:

`gradle :app:assembleDebug`

## Install

Copy `app-debug.apk` to the Android device, allow installs from the app you use to open the APK, and install it. Launch **YouTube Auto Skip**, tap **Open Accessibility Settings**, and enable **YouTube Auto Skip**.

Android always requires the user to enable an AccessibilityService explicitly.

## Testing

1. Enable the service.
2. Open the official YouTube app.
3. Play a video that receives a skippable ad.
4. When YouTube exposes the Skip control, the service should click it.
5. If your YouTube UI is in another language and it does not work, add the exact visible/accessibility skip label to the app, one label per line.

## Limitations

YouTube can change its UI at any time. Some versions may not expose the skip control through accessibility in the same way. The detector prioritizes resource IDs containing `skip` and conservative text/content-description matching rather than blindly tapping a location.

## Build the APK in GitHub Actions (phone/web only)

You do not need a laptop for this method.

1. Create a new **private** repository on GitHub. Do not add a README, `.gitignore`, or license when GitHub asks, because this project already contains the files it needs.
2. Upload the **contents of this project folder** to the repository. Make sure `.github/workflows/build-apk.yml` is included. If your phone hides folders beginning with a dot, see the note below.
3. Commit the uploaded files to the repository's `main` branch.
4. Open the repository's **Actions** tab. The **Build Android APK** workflow should start automatically after the commit.
5. Open the completed workflow run. At the bottom of its Summary page, under **Artifacts**, download **youtube-auto-skip-apk**.
6. GitHub downloads the artifact as a ZIP. Extract it on the Android phone; inside is `app-debug.apk`.
7. Open `app-debug.apk` and allow installation from your browser/files app if Android asks. Then open **YouTube Auto Skip** and enable its accessibility service.

You can also rebuild manually at any time: repository → **Actions** → **Build Android APK** → **Run workflow**.

### If your phone does not upload the hidden `.github` folder

GitHub's web file uploader may be awkward with hidden folders on some Android file managers. In that case, first upload the normal project files. Then, on GitHub's website, use **Add file → Create new file**, enter this exact filename:

```
.github/workflows/build-apk.yml
```

Then paste the workflow file from this project and commit it. GitHub will create the hidden folders automatically.

### Security note

For personal use, a private repository is recommended. The debug APK is automatically signed with a development key by the Android build tools. It can be installed directly on your devices, but it is not intended for Play Store distribution.
