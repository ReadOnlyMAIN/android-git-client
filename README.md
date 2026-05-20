# AGit Client

> A modern Git client for Android built with Kotlin, Android Studio, and Material Design 3.

![Platform](https://img.shields.io/badge/platform-Android-green)
![Language](https://img.shields.io/badge/language-Kotlin-purple)
![Status](https://img.shields.io/badge/status-Pre--release-orange)
![Version](https://img.shields.io/badge/version-0.1-blue)


AGit Client is an open-source Android application designed to provide a clean, fast, and native Git experience directly on mobile devices.

The project focuses on simplicity, responsiveness, and modern Android UI/UX while progressively integrating more advanced Git features.

⚠️ **Current UI status:**
AGit Client is currently optimized for **tablets and large screens only**.
Smartphone support and responsive layouts are one of the main priorities for upcoming versions.

# 🗨️ Developer Note

I am currently the sole developer of this project and originally started building AGit Client for my own workflow.

I develop projects using the Godot Engine on Android devices and needed a Git client with:

* a clean and intuitive user experience,
* a responsive and optimized interface,
* low resource consumption,
* and a modern Android-native architecture built with Kotlin and Material Design 3.

Most existing mobile Git clients either felt outdated, overloaded, or not optimized for the workflow I wanted.
AGit Client aims to provide a lightweight, reactive, and efficient Git experience designed specifically for Android devices.

The project is still in early development (`v0.1`) and evolves progressively as my own needs grow.

# 📱 Tech Stack

* **Language:** Kotlin
* **IDE:** Android Studio
* **UI:** Material Design 3
* **Platform:** Android
* **Architecture Goal:** Modern native Android application with responsive UI support

# 🧪 Project Status

AGit Client is currently in **pre-release** stage (`v0.1`).

The application is under active development and some features may be incomplete, unstable, or subject to major changes.

Feedback, ideas, and contributions are welcome.

# ✨ Features

## Current Features (v0.1)

* Repository cloning/import
* Repository list management
* Refresh repository list on clone/import/delete
* Editable Git credentials
* Material Design 3 interface
* Native Android experience
* Kotlin-based architecture

## 🚧 Planned Features

The project is currently in early development.
Here are some of the features planned for upcoming versions:

* Host URL linked credentials
* Commit details panel

  * Reset here
  * Additional commit actions
* File change details view
* Automatic fetch/synchronization

  * Periodic background refresh
  * Refresh when reopening the app
* File status indicators

  * Added
  * Modified
  * Deleted
* Commit description input
* Prevent duplicate local repository imports
* Warning when cloning the same repository multiple times
* Create new local branches
* Save default clone directory
* Automatically create repository folder during clone
* Responsive smartphone layout support

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

# 🤝 Contributing

Contributions are welcome.

If you'd like to contribute:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Open a Pull Request

Please keep the codebase clean and consistent with the existing architecture and Material Design 3 guidelines.

# 🐛 Issues & Suggestions

If you encounter a bug or have a feature request, feel free to open an issue in the GitHub repository.
