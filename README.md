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

1. Add Hilt wiring and replace manual `DefaultAppContainer` construction.
2. Introduce Room-backed entities/DAO for richer status history.
3. Add account API contracts and sync workers.
4. Build social graph workflows (invites, circles, visibility rules).
