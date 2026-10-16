# Changelog

All notable changes to the **NorthLight** Android TV application will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- Integrated official NorthLight application icons across all density buckets (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) and round icon variants.
- High-definition 16:9 Android TV Leanback launcher banner (`app_banner.png`).

### Changed
- Revamped application `README.md` into a TV-first product showcase with living room features, sideloading tutorials, and architectural breakdown.
- Configured CI (`ci.yml`) and CodeQL (`codeql.yml`) to run exclusively on Pull Requests.
- Automated Release pipeline (`release.yml`) to trigger on merge to `main` with unreleased version detection from `CHANGELOG.md`.

### Planned
- Subtitle track picker (.srt / .vtt) in built-in player
- Trakt.tv watch history sync
- Seedr OAuth WebView authentication fallback

---

## [1.0.0] - 2026-10-15

### Added
- **Multi-Module Architecture**: 15 distinct Gradle modules cleanly separated by domain (`core`, `streaming`, `feature`, `app`).
- **YTS Movie Browsing (`:core:yts`, `:feature:browse`)**:
  - Paginated movie catalogue fetched via YTS public API.
  - Genre filter chips (Action, Comedy, Drama, Sci-Fi, Horror, etc.).
  - Sorting options (Latest, Top Rated, Most Popular, Year).
  - Real-time debounced search bar with TV-friendly keyboard support.
- **TMDB Enrichment (`:core:tmdb`)**:
  - Automatic IMDB code mapping to TMDB API v3.
  - High-resolution cinematic backdrops and movie posters.
  - Movie tagline, detailed overview, and top cast photos.
- **In-App Local Torrent Streaming (`:streaming:torrent`)**:
  - Sequential P2P piece downloading using `libtorrent4j`.
  - Embedded local streaming server (`NanoHTTPD`) exposing `http://127.0.0.1:<port>/`.
  - HTTP Range Request (`Range: bytes=start-end`) support allowing video seeking.
  - Automatic priority assignment to the primary video file.
- **Dual Video Playback (`:feature:player`)**:
  - Built-in Google Media3 ExoPlayer with Android TV D-pad playback controls.
  - 1-click **"Open in VLC"** button launching VLC for Android TV (`org.videolan.vlc`) via Android Intent.
  - Fallback prompt and Google Play Store link when VLC is not installed.
- **Pluggable Streaming Architecture (`:core:streaming`, `:feature:settings`)**:
  - `StreamingProvider` interface with runtime backend selection.
  - `RealDebridProvider` for high-speed cached cloud streaming.
  - `SeedrProvider` for cloud torrent fetching with 2GB free tier.
  - Jetpack DataStore-backed settings screen for provider selection.
- **Local Persistence (`:core:database`)**:
  - Room database with `WatchHistoryDao` and `FavoriteMovieDao`.
- **CI/CD & Release Automation (`.github/workflows`)**:
  - Automated CI build and unit test workflow on every push and PR.
  - Automated Release workflow for GitHub Releases, APK packaging, and changelog verification.
  - Dynamic `versionCode` and `versionName` configuration via environment variables.

