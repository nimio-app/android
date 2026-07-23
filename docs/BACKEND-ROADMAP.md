# Nimio Backend and Communication Draft

Date: 2026-07-23

## 1) Current reality check

### Status expiry
Status expiry is **fully implemented** locally as of 2026-07-23.

Implemented:
- Expiry options in domain (`StatusExpiry`)
- Expiry selection in UI (`StatusScreen`)
- Expiry timestamp calculation on save (`StatusViewModel.saveStatus`)
- Expiry timestamp persistence in DataStore (`StatusPreferencesDataSource`)
- `StatusExpiryWorker` (CoroutineWorker) reads DataStore and clears expired status
- Worker scheduled/cancelled from `StatusViewModel` via `StatusExpiryScheduler`
- `WorkManagerStatusExpiryScheduler` wired in `StatusScreen`
- Full unit test coverage for scheduling, cancellation, and persistence behaviour

Note: server-side expiry enforcement is still pending and will be the source of truth once the backend is live.

## 2) Product model: how status should be communicated

### Core principle
"Online does not mean available." Nimio communicates **intentional availability** to specific people, not presence to everyone.

### Communication model
- User publishes a status update: availability + optional note + expiry.
- Status has an audience defined by social graph rules.
- Authorized connections retrieve status via feed/snapshot APIs.
- Important changes can fan out by push notification.
- Expired statuses are auto-cleared and can notify clients of invalidation.

### Audience and visibility
Recommended visibility levels for MVP+:
- `PRIVATE` (only me)
- `ALL_CONNECTIONS`
- `CIRCLE_ONLY` (family, close friends, etc.)
- `CUSTOM_LIST` (explicit allow-list)

## 3) Connection model: how people connect

### Entities
- `User`
- `Connection` (accepted relation between two users)
- `ConnectionInvite` (pending request)
- `Circle`
- `CircleMember`

### Invite lifecycle
1. Sender creates invite to receiver.
2. Receiver sees pending request.
3. Receiver accepts/declines.
4. On accept, create `Connection` and default visibility policy.

### Blocking and safety
- Support `BLOCKED` relationship state.
- Blocked users cannot send invites or view status.
- Add abuse reporting hooks from day one, even if backend handling is manual initially.

## 4) Backend architecture draft (Phase 3)

### Core services
- **Auth service**: account identity, token issuance/validation.
- **Profile service**: display name, avatar metadata, bio.
- **Social graph service**: invites, connections, circles.
- **Status service**: publish, read, expire, visibility checks.
- **Notification service**: push fanout via FCM/APNs bridge.

For MVP, these can be one deployable backend with modular boundaries.

### APIs (minimum set)
Auth/Profile:
- `POST /v1/auth/register`
- `POST /v1/auth/login`
- `GET /v1/me`
- `PATCH /v1/me/profile`

Connections:
- `POST /v1/invites`
- `GET /v1/invites/inbox`
- `POST /v1/invites/{id}/accept`
- `POST /v1/invites/{id}/decline`
- `GET /v1/connections`

Status:
- `PUT /v1/me/status`
- `GET /v1/me/status`
- `GET /v1/feed/status` (statuses from my visible graph)
- `DELETE /v1/me/status` (manual clear)

### Storage model (logical)
- `users`
- `profiles`
- `connection_invites`
- `connections`
- `circles`
- `circle_members`
- `statuses` (current status snapshot)
- `status_events` (append-only history for analytics/audit)
- `visibility_policies`

## 5) Sync and delivery strategy

### Client behavior
- On app start: fetch `me`, connections, and initial status feed.
- Foreground: refresh feed on interval and on app resume.
- On push event: fetch changed resources (do not trust push payload as source of truth).
- Offline: keep last known local snapshot; mark stale when needed.

### Expiry strategy
- Server is source of truth for expiry.
- On write, server computes `expiresAt` if a relative option is supplied.
- Expiry processor clears or marks status inactive when expired.
- Push `status_expired` event to affected clients.

### Conflict strategy
- Prefer server timestamp ordering.
- Use last-write-wins for current status snapshot.
- Keep event log for reconciliation/debugging.

## 6) Security and privacy baseline

- Token-based auth (short-lived access token + refresh token).
- Enforce authorization at every status read path.
- Store only required profile fields.
- Signed upload URLs for avatar uploads.
- Basic rate limiting on invites and status writes.

## 7) Android implementation plan aligned to this draft

### A. ~~Complete local expiry~~ ✅ Done (2026-07-23)
- `StatusExpiryWorker` clears expired local status via DataStore.
- Worker scheduled/cancelled from `StatusViewModel` after every save.
- Full unit test coverage for scheduling, cancellation, and persistence.

### B. Add remote contracts
- Add Retrofit interfaces + DTOs for auth/profile/social/status.
- Add repository mappings (DTO <-> domain).
- Keep local DataStore as cache/fallback.

### C. Introduce sync workers
- Periodic sync for feed refresh.
- One-time retry work for failed status writes.
- Push-triggered sync entry point.

### D. Build social workflows
- Invite list screen
- Accept/decline actions
- Basic circles and status visibility controls

## 8) Definition of done for “real backend is live”

- Users can register/login and maintain profile.
- Users can invite and accept at least one connection.
- Status updates are visible only to authorized connections.
- Expiry works on server and reflected on client.
- Offline updates eventually reconcile.
- Core flows have integration tests and observability (logs/metrics).

