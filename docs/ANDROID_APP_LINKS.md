# Android App Links for Email Verification

This project now declares Android App Links for:

- `https://nimio.org/verify-email?token=...`
- `https://www.nimio.org/verify-email?token=...`

Manifest intent-filters are in `app/src/main/AndroidManifest.xml` with `android:autoVerify="true"`.

## 1) Host `assetlinks.json`

Publish this file at both:

- `https://nimio.org/.well-known/assetlinks.json`
- `https://www.nimio.org/.well-known/assetlinks.json`

Use your real release certificate SHA-256 fingerprint:

```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "org.nimio.app",
      "sha256_cert_fingerprints": [
        "REPLACE_WITH_RELEASE_CERT_SHA256"
      ]
    }
  }
]
```

## 2) Get certificate fingerprints

Debug keystore fingerprint (local testing):

```zsh
keytool -list -v -alias androiddebugkey -keystore "$HOME/.android/debug.keystore" -storepass android -keypass android | grep "SHA256:"
```

Release keystore fingerprint:

```zsh
keytool -list -v -alias "<your_release_alias>" -keystore "<path_to_release_keystore>" | grep "SHA256:"
```

## 3) Verify App Links on device

Force domain verification re-check:

```zsh
adb shell pm verify-app-links --re-verify org.nimio.app
adb shell pm get-app-links org.nimio.app
```

Open a verification link directly:

```zsh
adb shell am start -a android.intent.action.VIEW -d "https://nimio.org/verify-email?token=test-token" org.nimio.app
```

If Digital Asset Links are valid and the app is installed, Android should open Nimio directly without browser handoff.

## 4) Backend verify endpoint expected by app

The app attempts to verify email token at:

1. `POST /v1/auth/verify-email` with body `{ "token": "..." }`
2. Fallback: `GET /v1/auth/verify-email?token=...`

If your server uses a different contract, align `AccountApi` accordingly.

