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

The project is still in early development (`v0.1`) and evolves progressively as my own needs grow.

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

# ✨ Features

## Current Features (v0.1)

* HTTP git connection *(no SSH yet)*
* Repository cloning/import
* Repository list management
* Editable Git credentials
* Fetch, Pull, Commit and Push

## 🚧 Roadmap

| Feature | Type | Status | Version |
|---|---|---|---|
| Responsive smartphone layout support | Feature | 🚧 In Progress | v0.2 |
| Prevent duplicate local repository imports | Bug Fix | 📅 Planned | v0.2 |
| Warning when cloning duplicate repositories | Bug Fix | 📅 Planned | v0.2 |
| Local branches creation | Feature | 📅 Planned | v0.2 |
| Commit details panel | Feature | 📅 Planned | v0.2 |
| ├─ Reset here action | Feature | 📅 Planned | v0.2 |
| └─ Additional commit actions | Feature | 📅 Planned | v0.2 |
| File change details view | Feature | 📅 Planned | v0.2 |
| SSH Git connection support | Feature | 📅 Planned | v0.2 |
| Commit description input | Feature | 📅 Planned | v0.2 |
| Save default clone directory | QoL | 📅 Planned | v0.2 |
| Automatically create repository folder when cloning | QoL | 📅 Planned | v0.2 |
| File status indicators | QoL | 📅 Planned | v0.2 |
| ├─ Added status | QoL | 📅 Planned | v0.2 |
| ├─ Modified status | QoL | 📅 Planned | v0.2 |
| └─ Deleted status | QoL | 📅 Planned | v0.2 |
| Automatic fetch/synchronization | QoL | 📅 Planned | v0.2 |
| ├─ Periodic background refresh | QoL | 📅 Planned | v0.2 |
| └─ Refresh on app reopen | QoL | 📅 Planned | v0.2 |

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
