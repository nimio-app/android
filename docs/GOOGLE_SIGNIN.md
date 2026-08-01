# Google Sign-In Integration

This document describes the Google Sign-In implementation using the modern `androidx.credentials` (Credential Manager) API.

## Setup

### 1. Google OAuth 2.0 Configuration

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create an OAuth 2.0 Web Application credential
3. Add your Android app's signing key certificate fingerprint:
   - **Package name**: `org.nimio.app`
   - **SHA-1 fingerprint**: `27:85:33:E5:E4:7A:7E:A7:28:52:C2:00:8B:7C:8D:6A:0D:3F:25:7E` (debug)
   - For production, use your release keystore SHA-1
4. Note the **Web Client ID** (looks like: `1234567890-abcdef...apps.googleusercontent.com`)

### 2. Configure Android App

Update `app/src/main/res/values/strings.xml`:

```xml
<string name="google_web_client_id">YOUR_WEB_CLIENT_ID_HERE</string>
```

Replace `YOUR_WEB_CLIENT_ID_HERE` with the actual Web Client ID from step 1.

### 3. Dependencies

The following dependencies are already added to `app/build.gradle.kts`:

```kotlin
implementation("androidx.credentials:credentials:1.3.0")
implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
```

## Architecture

### Components

1. **GoogleSignInHelper.kt** - Core flow wrapper
   - `signInWithGoogle(activity, webClientId, onResult)` - Handles CredentialManager flow
   - Returns `GoogleSignInResult` sealed type (Success, Cancelled, Error)

2. **AccountApi.kt** - Network layer
   - `POST /v1/auth/google` - Sends ID Token to backend

3. **AccountRepository** - Domain contract
   - `googleSignIn(idToken: String): NimioResult<AccountSession>`

4. **RemoteAccountRepository** - Implementation
   - Calls API endpoint
   - Saves JWT token and profile to local storage
   - Returns account session

5. **AuthViewModel** - UI state
   - `googleSignIn(idToken)` - Processes result and updates UI state

6. **AuthScreen** - UI composable
   - "Sign in with Google" button
   - Integrates CredentialManager flow

### Flow Diagram

```
User taps "Sign in with Google"
  ↓
CredentialManager.getCredential() → Google Play Services
  ↓
User selects/signs in with Google account
  ↓
Google returns ID Token (JWT)
  ↓
POST /v1/auth/google { id_token: "..." }
  ↓
Backend validates & returns Nimio JWT
  ↓
Save to DataStore + update UI state
  ↓
Navigate to main app
```

## Usage

### User Flow

1. User opens auth screen
2. Clicks "Sign in with Google" button
3. CredentialManager sheet appears
4. User selects or signs in with their Google account
5. ID Token is sent to backend
6. Backend responds with Nimio JWT
7. User is logged in and navigated to main app

### Error Handling

- **User cancellation**: Silently ignored, user stays on auth screen
- **Network error**: Displayed in red text below the button
- **Invalid token**: Returned as error from backend, shown to user

## Testing

### Unit Tests

The existing test fakes in `AuthViewModelTest.kt` have been updated to support `googleSignIn()`:

```bash
./gradlew :app:testDebugUnitTest
```

### Manual Testing (Local)

1. Update `strings.xml` with your Web Client ID
2. Ensure Google Play Services is installed on device/emulator
3. Run the app
4. Tap "Sign in with Google"
5. Complete the Google sign-in flow

### Manual Testing (Production)

After deploying, ensure:
1. Release app is signed with production keystore
2. SHA-1 of production certificate is registered in Google Cloud Console
3. Web Client ID is updated in `strings.xml` for release build

## Backend Contract

**Endpoint**: `POST /v1/auth/google`

**Request**:
```json
{
  "id_token": "<JWT from Google>"
}
```

**Response** (success):
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "email_verified": false,
      "created_at": "2026-07-29T...",
      "updated_at": "2026-07-29T..."
    },
    "profile": {
      "user_id": "uuid",
      "username": "google_user_123",
      "display_name": "John Doe",
      "avatar_url": null,
      "bio": null,
      "created_at": "2026-07-29T...",
      "updated_at": "2026-07-29T..."
    },
    "token": "eyJhbGc..."
  }
}
```

**Response** (error):
```json
{
  "success": false,
  "error": {
    "code": "INVALID_TOKEN",
    "message": "Google ID token is invalid or expired"
  }
}
```

## Files Modified/Created

- ✅ `app/build.gradle.kts` - Added Credential Manager dependencies
- ✅ `app/src/main/java/org/nimio/app/core/common/GoogleSignInHelper.kt` - New: Core Google Sign-In flow
- ✅ `app/src/main/java/org/nimio/app/feature/account/data/AccountNetworkModels.kt` - Added `GoogleSignInRequestDto`
- ✅ `app/src/main/java/org/nimio/app/feature/account/data/AccountApi.kt` - Added `googleSignIn` endpoint
- ✅ `app/src/main/java/org/nimio/app/feature/account/domain/AccountRepository.kt` - Added `googleSignIn` contract
- ✅ `app/src/main/java/org/nimio/app/feature/account/data/RemoteAccountRepository.kt` - Implemented `googleSignIn`
- ✅ `app/src/main/java/org/nimio/app/feature/account/data/InMemoryAccountRepository.kt` - Stubbed `googleSignIn`
- ✅ `app/src/main/java/org/nimio/app/feature/account/ui/AuthViewModel.kt` - Added `googleSignIn(idToken)` method
- ✅ `app/src/main/java/org/nimio/app/feature/account/ui/AuthScreen.kt` - Added "Sign in with Google" button
- ✅ `app/src/main/res/values/strings.xml` - Added Google Sign-In strings
- ✅ `app/src/test/java/org/nimio/app/feature/account/ui/AuthViewModelTest.kt` - Updated test fakes

## Build Status

```
./gradlew :app:testDebugUnitTest
Result: BUILD SUCCESSFUL ✓
```

All unit tests pass with the new Google Sign-In feature.

