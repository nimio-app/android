# Nimio Copilot Instructions

## Product context
Nimio helps people share their current availability and activity so others can connect at the right moment.

Core philosophy: **"Online does not mean available."**

## Tech baseline
- Android app written in Kotlin
- Jetpack Compose UI
- Package namespace: `org.nimio.app`
- Architecture: Clean Architecture + MVVM, feature-first packages

## Code expectations
- Keep implementations simple and MVP-focused
- Prefer Jetpack libraries and modern Android APIs
- Avoid adding business logic to composables
- Use immutable UI state models (`StateFlow` for state, event stream for one-offs)
- Keep domain models separate from data entities/DTOs
- Add explicit mappings between domain/data layers

## Structure conventions
- `core/*` contains shared primitives and platform services
- `feature/<name>/{ui,domain,data}` contains feature-scoped code
- `navigation/*` owns app routes and navigation graphs
- `di/*` owns dependency injection wiring

## Quality guardrails
- New behavior should include tests when practical
- Keep functions small and names intention-revealing
- No wildcard imports
- Avoid speculative abstractions until needed by at least two call sites
- Favor readability over cleverness

## PR guidance
- Explain user-visible impact
- List architectural trade-offs for non-trivial changes
- Keep commits focused and reviewable

