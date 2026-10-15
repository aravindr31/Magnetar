# NorthLight 🎬

Android TV app for streaming movies from YTS with built-in torrent streaming.

## Architecture

Multi-module Android TV app with a pluggable streaming backend:

```
YTS API + TMDB API → Movie browse/search → Select movie → Resolve stream → ExoPlayer/VLC
```

### Modules

| Module | Purpose |
|---|---|
| `core/common` | Shared utils, Result type, dispatchers |
| `core/network` | Retrofit/OkHttp base setup |
| `core/yts` | YTS API client + repository |
| `core/tmdb` | TMDB API client + repository |
| `core/streaming` | **StreamingProvider interface** (switchability layer) |
| `streaming/torrent` | Built-in libtorrent4j streaming |
| `streaming/realdebrid` | Real-Debrid API backend |
| `streaming/seedr` | Seedr fallback backend |
| `feature/browse` | Home + Search UI |
| `feature/details` | Movie details screen |
| `feature/player` | ExoPlayer + VLC intent |
| `feature/settings` | Backend switching + credentials |

## Setup

1. Get a [TMDB API key](https://www.themoviedb.org/settings/api)
2. Add to `local.properties`:
   ```
   TMDB_API_KEY=your_key_here
   ```
3. Build and run on Android TV / emulator (min SDK 23)

## Switching Streaming Backends

Go to **Settings** in the app → choose between:
- 🔵 **Local Torrent** — built-in, no account needed
- 🟡 **Real-Debrid** — fastest, requires subscription
- 🔴 **Seedr** — cloud fallback, free tier available

## Development

Trunk-based development. See `CONTRIBUTING.md` for branch conventions.
