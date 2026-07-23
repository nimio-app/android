# Nimio

The right moment to connect.

Nimio helps people share their availability, so you know when to reach out without interrupting.

## Philosophy

Online does not mean available.

Nimio gives people a lightweight way to communicate their current moment.

## Current foundation

- Kotlin + Jetpack Compose Android app
- App shell with top-level navigation (`Status`, `Social`, `Account`)
- Local status system using DataStore
- Feature-first package layout aligned with Clean Architecture + MVVM
- Scaffolds for account, sync, and social graph layers

## Implementation status (2026-07-23)

- Status expiry is fully implemented: UI, domain, persistence, worker execution, and scheduling are all wired and tested.
- Connection workflows and backend sync are still in design/roadmap stage.

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
2. Add Hilt wiring and replace manual dependency construction.
3. Introduce remote API contracts and sync workers for profile/social/status.
4. Build social graph workflows (invites, circles, visibility rules).
5. Introduce Room-backed status history and reconciliation.

## Product and backend draft

The full draft for status communication, connection lifecycle, and backend architecture is documented in:

- `docs/BACKEND-ROADMAP.md`

This includes:
- how status should be communicated to authorized people,
- how invites/connections should work,
- a proposed API surface,
- data model and sync strategy,
- and a definition of done for the first real backend release.
