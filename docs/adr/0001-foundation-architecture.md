# ADR 0001: Foundation Architecture

- **Date**: 2026-07-21
- **Status**: Accepted

## Context
Nimio is an open-source Android app expected to grow from local-only MVP behavior into account-based and synced experiences.

## Decision
Adopt Clean Architecture + MVVM with feature-first packaging in a single app module initially.

## Consequences
### Positive
- Predictable structure for contributors
- Separation of UI, domain, and data concerns
- Easier migration to multi-module setup later

### Negative
- Slightly more upfront ceremony than a purely screen-based layout
- Requires discipline in boundary enforcement

## Follow-up
- Introduce ViewModels and use cases per feature increment
- Add Room schema and repository contracts
- Add API client contracts when backend scope is finalized

