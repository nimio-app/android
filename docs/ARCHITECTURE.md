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
- `org.nimio.app.feature.connections`
- `org.nimio.app.feature.profile`
- `org.nimio.app.navigation`
- `org.nimio.app.di`

## State model
- Screen state: immutable `UiState` exposed via `StateFlow`
- One-time events: event flow (`SharedFlow` or channel-backed stream)
- Composables are stateless where possible

## Data strategy
- Room is the local source of truth
- DataStore stores lightweight preferences
- Repository coordinates local + remote data

## Network strategy
- Retrofit + OkHttp + Kotlinx Serialization
- DTO -> entity/domain mapping in data layer
- WorkManager for background sync/retries

## Testing approach
- Unit tests for domain and ViewModels
- Integration tests for repositories and DAOs
- Compose UI tests for critical flows

## Future scaling
When feature count and build times justify it, split into gradle modules:
- `:app`
- `:core:*`
- `:feature:*`

## Related roadmap
- `docs/BACKEND-ROADMAP.md` documents the planned backend contracts, status communication model, social connection lifecycle, and sync strategy.

