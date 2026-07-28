# Cloud Avatar Storage Integration

The Nimio Android app now uploads user avatars to Cloudflare R2 instead of storing them locally.

## Architecture

### User Flow

1. User selects/crops a photo in onboarding or profile screen
2. Photo saved temporarily to app cache
3. `AccountViewModel.uploadAvatar(filePath)` called
4. Multipart POST to `POST /v1/me/avatar`
5. Backend stores in Cloudflare R2, returns public URL
6. App stores cloud URL in local DataStore
7. Old local file cleaned up

### API Contract

**Upload Avatar:**
```
POST /v1/me/avatar
Authorization: Bearer <token>
Content-Type: multipart/form-data

Form field: "avatar" (binary file)
Max size: 5MB
Formats: JPEG, PNG, GIF, WebP

Response:
{
  "success": true,
  "data": {
    "avatar_url": "https://pub-xxxxx.r2.dev/avatars/uuid.jpg",
    "message": "Avatar uploaded successfully"
  }
}
```

**Delete Avatar:**
```
DELETE /v1/me/avatar
Authorization: Bearer <token>

Response:
{
  "success": true,
  "data": {
    "message": "Avatar deleted successfully"
  }
}
```

## Implementation Details

### RemoteAccountRepository

- `uploadAvatar(filePath: String): NimioResult<String>`
  - Validates file exists and ≤5MB
  - Creates multipart request with `avatar` form field
  - Returns cloud URL on success
  - Cleans up local file after successful upload

- `deleteAvatar(): NimioResult<Unit>`
  - Sends DELETE request
  - Clears local avatar URL on success

### Data Model

**LocalProfile**
- Stores `avatarUri` (cloud URL after upload)
- Persisted to DataStore Preferences with key `avatar_uri`

**AvatarUploadResponseDto**
- `avatar_url: String` - Cloud URL from R2
- `message: String` - Success message

### UI Integration

**AccountScreen** (Profile Edit)
- Shows upload/change/remove buttons
- Calls `viewModel.uploadAvatar(filePath)`
- Displays loading state during upload
- Updates preview immediately after upload

**OnboardingScreen** (First Time Setup)
- Photo selection/cropping via uCrop
- Auto-upload when transitioning to next screen
- Shows "Uploading photo..." state

## Key Features

✅ **Progress Feedback**: Loading state during upload
✅ **File Validation**: 5MB max, MIME type checked
✅ **Error Handling**: User-friendly messages for failures
✅ **Retry Support**: Failed uploads can be retried
✅ **Local Cleanup**: Temp files cleaned after upload
✅ **Offline-First**: Cloud URL stored locally, survives app restarts

## Testing

**Unit Test:**
```kotlin
// Mock uploads return success URL
accountRepository.uploadAvatar("/path/to/file.jpg")
// Returns: NimioResult.Success("https://pub-xxxxx.r2.dev/avatars/uuid.jpg")
```

**Manual Testing:**
```bash
# With valid JWT token
curl -X POST https://api.nimio.org/v1/me/avatar \
  -H "Authorization: Bearer <token>" \
  -F "avatar=@/path/to/photo.jpg"
```

## Error Cases Handled

- File not found: `"Avatar file not found: ..."`
- File too large (>5MB): `"Avatar file exceeds 5MB limit."`
- Network failure: `"Unable to complete this request right now."`
- Server error: Parsed from response envelope

## Environment

Backend requires Cloudflare R2 credentials:
- `R2_ACCOUNT_ID`
- `R2_ACCESS_KEY_ID`
- `R2_SECRET_ACCESS_KEY`
- `R2_BUCKET_NAME` (e.g., `nimio`)
- `R2_PUBLIC_URL` (e.g., `https://pub-xxxxx.r2.dev`)

See backend README for full setup.

