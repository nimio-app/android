# Nimio

The right moment to connect.

Nimio helps people share their availability, so you know when to reach out without interrupting.

## Philosophy

Online does not mean available.

Nimio gives people a lightweight way to communicate their current moment.

## Current foundation

- Kotlin + Jetpack Compose Android app
- App shell with top-level navigation (`Status`, `Social`, `Account`)
- Hilt dependency injection with feature repositories
- Retrofit + OkHttp networking against Nimio backend APIs
- Local profile/status persistence using DataStore
- Feature-first package layout aligned with Clean Architecture + MVVM
- Implemented account, status, and social graph feature flows

## Implementation status (2026-08-03)

- Status expiry is fully implemented: UI, domain, persistence, worker execution, and scheduling are all wired and tested.
- Auth flows are wired to backend: register, login, Google sign-in, and email verification.
- Social graph and status repositories are wired to remote APIs with local state updates.
- Session-driven startup refresh is implemented for status and connection data.

## Project structure

- `app/src/main/java/org/nimio/app/core` shared primitives and platform services
- `app/src/main/java/org/nimio/app/feature` feature code (`status`, `social`, `account`, `sync`)
- `app/src/main/java/org/nimio/app/navigation` navigation shell and destinations
- `docs/ARCHITECTURE.md` architecture overview
- `docs/adr/0001-foundation-architecture.md` first architecture decision record

## Quick start

```zsh
cd /Users/lakky/Documents/GitHub/Nimio
./gradlew :app:assembleDebug
```

## Run tests

```zsh
cd /Users/lakky/Documents/GitHub/Nimio
./gradlew :app:testDebugUnitTest
```

## Next milestones

1. ~~Complete local expiry execution (worker implementation + scheduling).~~ ✅ Done
2. ~~Add Hilt wiring and replace manual dependency construction.~~ ✅ Done
3. ~~Introduce remote API contracts for profile/social/status.~~ ✅ Done
4. Harden background sync strategy (periodic refresh, retry semantics, push-triggered sync).
5. Expand social graph UX and policy controls (invite ergonomics, circles, visibility management).
6. Introduce Room-backed history/reconciliation where longer-lived local storage is needed.

## Product and backend draft

The full draft for status communication, connection lifecycle, and backend architecture is documented in:

- `docs/BACKEND-ROADMAP.md`

This includes:
- how status should be communicated to authorized people,
- how invites/connections should work,
- a proposed API surface,
- data model and sync strategy,
- and a definition of done for the first real backend release.
