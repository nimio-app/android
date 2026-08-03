# Nimio Android Architecture

## Architectural style
Nimio uses **Clean Architecture + MVVM** with a **feature-first** package structure.

Layers:
- `ui`: Compose screens, UI state, and ViewModels
- `domain`: use cases and domain models (framework-agnostic)
- `data`: repositories, local/remote sources, and mappers

## Package layout (single-module foundation)
- `org.nimio.app.core.common`
- `org.nimio.app.core.ui`
- `org.nimio.app.core.database`
- `org.nimio.app.core.network`
- `org.nimio.app.feature.status`
- `org.nimio.app.feature.social`
- `org.nimio.app.feature.account`
- `org.nimio.app.feature.sync`
- `org.nimio.app.navigation`
- `org.nimio.app.di`

## State model
- Screen state: immutable `UiState` exposed via `StateFlow`
- One-time events: event flow (`SharedFlow` or channel-backed stream)
- Composables are stateless where possible

## Data strategy
- Repositories coordinate remote APIs and local caches
- DataStore currently stores lightweight persisted user state (profile/status/token-adjacent state)
- Room is available as a dependency and remains planned for richer offline history/reconciliation use cases

## Network strategy
- Retrofit + OkHttp + Kotlinx Serialization
- Auth interceptor injects bearer tokens for protected APIs
- DTO -> entity/domain mapping in data layer
- WorkManager is used for status expiry today and is the base for broader sync/retry workflows

## Testing approach
- Unit tests for domain and ViewModels
- Integration tests for repositories and persistence adapters
- Compose UI tests for critical flows

## Future scaling
When feature count and build times justify it, split into gradle modules:
- `:app`
- `:core:*`
- `:feature:*`

## Related roadmap
- `docs/BACKEND-ROADMAP.md` documents the planned backend contracts, status communication model, social connection lifecycle, and sync strategy.

