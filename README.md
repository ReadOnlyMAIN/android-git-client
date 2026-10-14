# AGit Client

> A modern Git client for Android built with Kotlin, Android Studio, and Material Design 3.

![Platform](https://img.shields.io/badge/platform-Android-green)
![Language](https://img.shields.io/badge/language-Kotlin-purple)
![Status](https://img.shields.io/badge/status-Pre--release-orange)
![Version](https://img.shields.io/badge/version-0.1-blue)

**Next version:** v0.2 — in development. See [ROADMAP.md](ROADMAP.md).


AGit Client is an open-source Android application designed to provide a clean, fast, and native Git experience directly on mobile devices.

The project focuses on simplicity, responsiveness, and modern Android UI/UX while progressively integrating more advanced Git features.

⚠️ **Current UI status:**
The v0.1 interface primarily targets **tablets and large screens**.
Responsive smartphone layouts are already being developed for v0.2, but still require validation and refinement.

# 📱 Tech Stack

* **Language:** Kotlin
* **IDE:** Android Studio
* **UI:** Jetpack Compose and Material Design 3
* **Platform:** Android 14+ (API 34+)
* **Git engine:** JGit
* **Architecture:** ViewModels, coroutines, Flow, and Hilt dependency injection
* **Storage:** Preferences DataStore; Git credentials encrypted with Android Keystore

# 🧪 Project Status

AGit Client is currently in **pre-release** stage (`v0.1`).

Development is focused on **v0.2**. Its first priority is to correct, optimize, and stabilize the existing application logic, without adding, removing, or modifying UI. Functional additions and UX/UI improvements belong to a second phase, after the internal reliability work has been validated.

The application is under active development and some features may be incomplete, unstable, or subject to major changes.

Feedback, ideas, and contributions are welcome.

# 📄 License

AGit Client is licensed under the GNU General Public License v3.0.

This means you are free to use, study, modify, and redistribute the software, including commercially, provided that any distributed modified versions also remain open-source under the same license.

# 🗨️ Developer Note

I am currently the sole developer of this project and originally started building AGit Client for my own workflow.

I develop projects using the Godot Engine on Android devices and needed a Git client with:

* a clean and intuitive user experience,
* a responsive and optimized interface,
* low resource consumption,
* and a modern Android-native architecture built with Kotlin and Material Design 3.

Most existing mobile Git clients either felt outdated, overloaded, or not optimized for the workflow I wanted.
AGit Client aims to provide a lightweight, reactive, and efficient Git experience designed specifically for Android devices.

The project is still in early development, with v0.2 in progress, and evolves progressively as my own needs grow.

# 🐛 Issues & Suggestions

If you encounter a bug or have a feature request, feel free to open an issue in the GitHub repository.

# 🤝 Contributing

Contributions are welcome.

If you'd like to contribute:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Open a Pull Request

Please keep the codebase clean and consistent with the existing architecture and Material Design 3 guidelines.
See [Architecture](docs/ARCHITECTURE.md) and [Testing and release validation](docs/TESTING.md) for implementation boundaries and verification commands.
Follow the phase ordering in [ROADMAP.md](ROADMAP.md): internal reliability work comes before new functionality or UX/UI changes.

# ✨ Features

## Current Features (v0.1)

* HTTP(S) Git connections *(no SSH yet)*
* Repository cloning/import
* Repository list management
* Git credential storage and management *(in-place credential editing is not yet implemented)*
* Fetch, Pull, Commit and Push

## Already present in the v0.2 development code

* Responsive workspace and selection dialogs for compact screens
* Local branch creation, checkout, and deletion, with an optional remote deletion action
* Cancellation controls for fetch, pull, and push
* Automatic creation of a repository subfolder when cloning

These implementations still require reliability and device testing; their presence does not imply release readiness.

## 🚧 Roadmap

See [ROADMAP.md](ROADMAP.md) for version-specific goals, implementation status, and the separation between internal logic work and subsequent functional / UX/UI improvements.

# 📦 Installation

## Clone the repository

```bash
git clone https://github.com/ReadOnlyMAIN/android-git-client.git
```

## Open in Android Studio

1. Open Android Studio
2. Select **Open**
3. Choose the cloned project folder
4. Sync Gradle dependencies
5. Run the application on an emulator or Android device
