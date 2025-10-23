# Codexia

Codexia is an Android application designed to let users manually **track**, **annotate**, and **visualize** their reading journey.  
It combines refined aesthetics with a deeply personal journaling experience — a sanctuary for readers who want to keep their story records private, organized, and meaningful.

---

## Core Vision

Codexia embodies the **“Arcane Grimoire”** design philosophy:  
a cozy, sophisticated, and magical digital journal where every interaction feels intimate and intentional.  
The app is completely **offline-first** and focuses on **user control**, **manual input**, and **visual storytelling**.

---

## Key Features

### Home Screen (Dashboard)
- **Time-sensitive greeting** (e.g., “Good Morning, Acin”).
- **Continue Reading** carousel — quick access to your current series.
- **Recent Logs** list with timestamps, notes, and accent highlights.

### Library Screen (Bookshelf)
- Customizable **shelves** (e.g., Reading, Favorites, Dropped).
- Comprehensive **filtering** and **sorting** controls.
- Floating Action Button to quickly add a new series.

### Series Detail & Log Screen (Journal)
- Dynamic masthead showing cover, title, and author.
- Manual **progress logging** with notes.
- Chronological **reading history** with pinned highlights.
- Quick access to synopsis, genres, and external source link.

### Insights Screen (The Oracle)
- Visual statistics for:
  - Chapters read this week / month
  - Current reading streak
  - **activity charts** and **genre timelines**.

### Add & Edit Series Screen (The Scribe’s Desk)
- Comprehensive data entry form.
- Cover selection via **gallery** or **online search** (Google Images API).
- Genre tagging, shelf assignment, and status fields.

---

## Technical Stack

| Layer | Technology |
|-------|-------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Database** | Room (SQLite) |
| **Networking** | Retrofit |
| **Image Loading** | Coil |
| **Async Operations** | Coroutines & Flow |
| **Navigation** | Jetpack Compose Navigation |

---

## Database Schema

| Table | Fields |
|--------|---------|
| **Series** | id, title, author, coverPath, synopsis, status, sourceUrl |
| **Shelves** | id, name, order |
| **Genres** | id, name |
| **LogEntries** | id, seriesId, chapterNumber, timestamp, note, isPinned |
| **CrossRefs** | For many-to-many (Series ↔ Genres) |

---

## Setup & Installation

### Prerequisites
- Android Studio (latest stable)
- Kotlin 1.9+
- Android SDK 24+
- Internet connection (only required for online image search)
