# Folio

[![Build Status](https://github.com/zer0k7/Folio/actions/workflows/build.yml/badge.svg)](https://github.com/zer0k7/Folio/actions)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android_API_26+-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)

A wiki-style offline reference application for foundational technology concepts.
Dense, structured technical knowledge designed to work completely without network access or accounts.

## Overview

Folio is a pocket reference for software engineers, systems programmers, students, and hardware enthusiasts. It provides concise, clear, and structured explanations of core computer science, networking, operating system, security, display, and hardware concepts. The entire application operates completely offline with zero telemetry, zero accounts, and zero remote dependencies.

## Features

| Feature | Description |
| :--- | :--- |
| Offline-First Architecture | All reference material, diagrams, and indexes are stored locally with zero network requests required. |
| Dense Knowledge Base | Core technical concepts broken down into structured definitions, diagrams, and technical comparisons. |
| Instant Full-Text Search | Client-side search indexing across titles, summaries, and deep technical topics with keyword highlighting. |
| Custom Technical Diagrams | Vector-rendered architecture and protocol diagrams that adapt dynamically to dark and light display modes. |
| Bookmarking System | Save key concepts and technical specifications for rapid one-tap offline retrieval. |
| Responsive Layouts | Optimized viewports accommodating compact handheld phones through large tablet displays. |

## Screenshots

Screenshots will be added after first stable release.

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | MVVM + Repository Pattern |
| Local Database | Room (SQLite) |
| Design System | Material 3 (Custom Tailored Tokens) |
| Navigation | Navigation Compose |
| Serialization | kotlinx-serialization |
| Typography | Google Fonts (DM Serif Display, Inter, JetBrains Mono) |
| CI / Automation | GitHub Actions |

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK API 26+

### Build

```bash
git clone https://github.com/zer0k7/Folio.git
cd Folio
./gradlew assembleDebug
```

### Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Download

Pre-built signed release APK packages are available on the [GitHub Releases](https://github.com/zer0k7/Folio/releases) page.

## Contributing

Contributions are welcome. Please adhere to the following guidelines:

1. Fork the repository and create a feature branch (`feat/your-feature`, `fix/issue-description`, or `content/article-topic`).
2. Adhere strictly to existing coding conventions and design standards.
3. Commit messages must be imperative, lowercase, and without emojis or trailing periods (e.g. `feat: add ethernet protocol reference`).
4. Ensure all automated tests and lint checks pass before opening a Pull Request.

## License

Distributed under the Apache 2.0 License. See [LICENSE](LICENSE) for full text.
