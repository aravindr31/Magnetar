# NorthLight 🎬

**NorthLight** is a modular Android TV application that brings together YTS movie browsing, TMDB rich metadata, an in-app sequential torrent streaming server, and seamless playback via either the built-in Media3 player or external VLC.

---

## 🌟 Key Features

1. **Movie Discovery & Search (YTS + TMDB)**:
   - Browse trending, latest, and top-rated movies directly from YTS.
   - Filter by genres (Action, Comedy, Sci-Fi, Drama, Horror, etc.) and sort options.
   - Live debounced search by movie title with TV-friendly keyboard input.
   - Enriched metadata (high-res backdrops, cast & director info, tagline, synopsis) powered by TMDB using IMDB code mapping.

2. **Built-in Local Torrent Streaming Server**:
   - Sequential P2P downloading using `libtorrent4j`.
   - Downloads headers and initial buffer directly on-device.
   - Embedded local HTTP server (`NanoHTTPD`) with HTTP range-request support for instant video seeking.
   - No external accounts or subscriptions needed.

3. **Dual Playback Support (Built-in + VLC)**:
   - **Built-in Player**: Google Media3 ExoPlayer optimized for Android TV with D-pad navigation.
   - **VLC Integration**: 1-click "Open in VLC" button launching VLC via Android Intent (`org.videolan.vlc`) with stream URL and title.
   - Automatic fallback and Google Play Store link if VLC is not installed.

4. **Modular & Switchable Architecture**:
   - `StreamingProvider` interface allows switching between **Local Torrent** (default), **Real-Debrid**, and **Seedr**.
   - Room Database for watch history and favorites.
   - Trunk-based development standards with clean multi-module separation.

---

## 🏗️ Architecture

```
                                      ┌──────────────┐
                                      │   YTS API    │
                                      └──────┬───────┘
                                             │
┌──────────────┐      ┌──────────────┐       ▼
│   TMDB API   │◄────►│ Movie Details│◄── Select Movie
└──────────────┘      └──────┬───────┘
                             │ Hit "Stream"
                             ▼
              ┌─────────────────────────────┐
              │      StreamingProvider      │
              │  (Local Torrent / Fallback) │
              └──────────────┬──────────────┘
                             │ Sequential download + Local HTTP server
                             ▼
              ┌─────────────────────────────┐
              │  http://127.0.0.1:<port>/   │
              └──────────────┬──────────────┘
                             │
              ┌──────────────┴──────────────┐
              ▼                             ▼
       ┌──────────────┐              ┌──────────────┐
       │ Media3 Player│              │  VLC for TV  │
       │ (Built-in)   │              │  (Intent)    │
       └──────────────┘              └──────────────┘
```

---

## 📦 Modules

| Module | Namespace | Purpose |
|---|---|---|
| `:app` | `com.northlight` | TV Application entry, MainActivity, Hilt DI root, Navigation host |
| `:core:common` | `com.northlight.core.common` | `Result<T>` sealed class, `SafeApiCall`, `AppDispatchers` |
| `:core:network` | `com.northlight.core.network` | Retrofit, OkHttp, Moshi, JSON converters |
| `:core:database` | `com.northlight.core.database` | Room database (Watch history & favorites DAOs) |
| `:core:ui` | `com.northlight.core.ui` | TV theme, colors, `MovieCard`, `LoadingScreen`, `ErrorScreen`, `Routes` |
| `:core:yts` | `com.northlight.core.yts` | YTS API service, models, magnet link builder, repository |
| `:core:tmdb` | `com.northlight.core.tmdb` | TMDB API service, models, IMDB lookup, repository |
| `:core:streaming` | `com.northlight.core.streaming` | `StreamingProvider` interface, `StreamingBackend` enum, `StreamingConfig` |
| `:streaming:torrent` | `com.northlight.streaming.torrent` | **Primary backend**: `libtorrent4j` sequential torrent client + `TorrentHttpServer` |
| `:streaming:realdebrid` | `com.northlight.streaming.realdebrid` | Cached cloud streaming provider (optional) |
| `:streaming:seedr` | `com.northlight.streaming.seedr` | Cloud torrent fetcher with 2GB free tier (optional fallback) |
| `:feature:browse` | `com.northlight.feature.browse` | Home movie grid, category filters, and live search screen |
| `:feature:details` | `com.northlight.feature.details` | Movie info, TMDB cast, quality picker, stream buttons |
| `:feature:player` | `com.northlight.feature.player` | Media3 ExoPlayer TV UI, VLC Intent helper, playback controller |
| `:feature:settings` | `com.northlight.feature.settings` | Provider switcher, backend inspection, settings screen |

---

## 🚀 Getting Started

1. Copy `local.properties.example` to `local.properties`:
   ```bash
   cp local.properties.example local.properties
   ```
2. (Optional) Add your TMDB API key in `local.properties`:
   ```properties
   TMDB_API_KEY=your_tmdb_key_here
   ```
3. Open project in **Android Studio (Ladybug / Koala or newer)** and build on Android TV emulator or physical Android TV device (min SDK 23).
