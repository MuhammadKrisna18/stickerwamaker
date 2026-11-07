# Sticker WA Maker 🎨📱

A clean, modern, and **100% ad-free** WhatsApp Sticker Maker for Android built with **Kotlin** and **Jetpack Compose**.

Created out of frustration with existing sticker maker apps on the Play Store that are bloated with intrusive full-screen ads, paywalls, and forced account creation.

---

## ✨ Features & Architecture Highlights

- 🚫 **Zero Bloat & Ad-Free**: No interstitial ads, no paywalls, no forced logins.
- 🔒 **Privacy-First (100% Offline)**: All stickers and image processing stay on your device.
- ✂️ **Smart Sticker Studio**:
  - **Auto Cutout / Background Removal**: Powered by **Google ML Kit Segmentation** running 100% on-device (fast, free, offline).
  - **White Outline / Stroke Generator**: Signature WhatsApp sticker white border around cutouts with customizable thickness.
  - **Meme Caption & Typography**: High-contrast text overlay with solid black border (readable on any light or dark chat theme) and color selection.
- 📦 **Sticker Pack Management**:
  - Create and manage custom packs (3–30 stickers per pack).
  - Custom tray/cover icon selection.
  - Interactive grid with delete and set-as-tray options.
- 🚀 **Official WhatsApp Integration**:
  - Compliant with WhatsApp technical specifications (**512x512 px WebP**, `< 100 KB`).
  - Implements custom `StickerContentProvider` to serve stickers securely to WhatsApp via Binder IPC.
  - One-tap export via `com.whatsapp.intent.action.ENABLE_STICKER_PACK`.
  - Supports both **WhatsApp** and **WhatsApp Business** with smart app detection.
- 💾 **Offline Backup & Share**:
  - Export any sticker pack as a `.zip` archive via Android `FileProvider` and native share sheet.
  - Share sticker collections with friends or backup without needing an external cloud server.

---

## 🛠️ Tech Stack & Clean Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) (2.2 / 2.3)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
- **Navigation**: Type-safe navigation (`androidx.navigation3`)
- **Architecture**: MVVM + Clean Architecture + SOLID Principles + UDF (Unidirectional Data Flow)
- **Image Processing**: Android Graphics / Canvas, Coil
- **Machine Learning**: Google ML Kit Segmentation Selfie (On-Device)
- **Serialization**: Kotlinx Serialization JSON
- **WhatsApp Bridge**: Android `ContentProvider` (`StickerContentProvider`) + `FileProvider`
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 16 (API 36 / 37)

---

## 🚀 Getting Started

1. Clone this repository:
   ```bash
   git clone https://github.com/MuhammadKrisna18/stickerwamaker.git
   ```
2. Open in **Android Studio** (Ladybug, Meerkat, or newer).
3. Sync Gradle and run on an Android device or emulator running API 24+.

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
