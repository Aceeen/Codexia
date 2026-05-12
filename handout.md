# 📚 Codexia: The Ultimate Series Tracker
**v2.0 Beta — Production-Ready Offline-First Architecture**

Codexia is a premium, feature-rich Android application designed for avid readers of web novels, light novels, and manga. It serves as a centralized, offline-first hub to track reading progress, manage personal libraries, and gain insights into reading habits through beautiful analytics.

---

## ✨ Key Features

### 🏠 Immersive Home Experience
*   **Dynamic Greetings**: Personalized time-of-day greetings (Good Morning, Evening, etc.).
*   **Continue Reading**: A high-impact "Hero" section using **HorizontalPager** with dynamic scaling effects, allowing users to jump back into their top series instantly.
*   **Quick Insights**: At-a-glance view of the current reading streak and total library count.

### 📖 Robust Library Management
*   **Smart Filtering**: Real-time search and shelf-based filtering (e.g., Reading, Completed, Dropped).
*   **Genre Tagging**: Categorize series with multiple genre tags for better organization.
*   **Dynamic Shelves**: Create and manage custom shelves directly within the addition flow.

### 📝 Precision Tracking (The Scribe System)
*   **Reading Logs**: Add chapter-specific logs with personal notes.
*   **Conflict Resolution**: Automatically updates existing logs if you re-log the same chapter.
*   **Log Management**: Pin important milestones, sort by newest/oldest/chapter, and use **Swipe-to-Delete** with a safety "Undo" snackbar.
*   **Interactive Progress**: Visual progress bars and automated "Latest Chapter" updates.

### 🖼️ Intelligent Cover Discovery
*   **In-App Google Search**: A seamless, keyless in-app image search using Google Search scraping and Google Books API. Find covers for niche webnovels without leaving the app.
*   **Gallery Integration**: Support for picking local images from the device gallery.

### 📈 The Oracle (Insights)
*   **Reading Streaks**: Automated calculation of daily reading streaks with motivational milestones.
*   **Activity Visualization**: Interactive bar charts showing reading frequency over the last 6 months.
*   **Time-Range Filters**: Drill down into stats for the current week, month, year, or all time.

---

## 🛠️ Technical Architecture

Codexia is built using modern Android development best practices:

| Layer | Technology | Purpose |
| :--- | :--- | :--- |
| **UI** | **Jetpack Compose** | Fully declarative, reactive UI with Material 3. |
| **Navigation** | **Compose Navigation** | Type-safe(ish) routing with dynamic argument support. |
| **Persistence** | **Room Database** | Offline-first storage with SQLite. |
| **State Management** | **ViewModel + StateFlow** | Reactive UI states surviving configuration changes. |
| **DI** | **Manual Dependency Injection** | Clean separation of concerns via `CodexiaApplication`. |
| **Async** | **Kotlin Coroutines** | Non-blocking database and network operations. |
| **Images** | **Coil** | High-performance image loading and caching. |
| **Preferences** | **DataStore** | Persistent user settings (e.g., Display Name). |

---

## 📂 Project Structure

```text
com.example.codexiabeta
├── data
│   ├── dao         # Room DAOs for Series, Logs, Shelves, Genres
│   ├── entity      # Room Entities and Cross-Ref junctions
│   ├── repository  # Clean Data Access Layer (Series, Log, Shelf)
│   └── AppDatabase # Database config with Seeding logic
├── screens         # Compose UI screens (Home, Library, Detail, etc.)
├── viewmodel       # Reactive business logic for each screen
├── ui.theme        # Custom Material 3 color system and typography
└── MainActivity    # Navigation Graph and Entry Point
```

---

## 🚀 Getting Started

### Prerequisites
*   Android Studio Iguana or newer.
*   Android SDK 34+.
*   Kotlin 1.9.0+.

### Build & Run
1.  Clone the repository.
2.  Open in Android Studio.
3.  Sync Gradle (using **KSP** for annotation processing).
4.  Run `assembleDebug` or deploy directly to a physical device/emulator.

---

## 🔮 Roadmap (Upcoming)
- [ ] **v2.1**: Cloud Sync (Firebase/Supabase integration).
- [ ] **v2.1**: Advanced CSV/JSON Export & Import.
- [ ] **v2.2**: Dark/Light mode manual toggle and custom themes.
- [ ] **v2.5**: Optical Character Recognition (OCR) for logging chapters from screenshots.

---

