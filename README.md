# CreditTrack PH

CreditTrack PH is an Android application designed for Filipino cardholders to track multiple credit cards, manage installment payments, and categorize expenses. It features a built-in AI assistant, Financier, which supports voice dictation, speech synthesis, and automatic categorization.

## Features

### AI Assistant (Financier)
- **Voice Dictation:** Uses Groq Whisper AI (`whisper-large-v3-turbo`) for multilingual voice transcription.
- **Push-to-Talk:** Record audio directly in the app without external dialogs.
- **Text-to-Speech (TTS):** Audio playback for assistant responses.
- **Natural Language Commands:** Supports commands like undoing accidental payments or marking items as unpaid.
- **Category Inference:** Automatically categorizes expenses based on brand names and keywords common in the Philippines (e.g., Jollibee, Grab, SM).

### Card Management
- Manage multiple credit cards from major Philippine banks (BPI, BDO, Metrobank, Security Bank, UnionBank, RCBC, EastWest, etc.).
- Track credit limits, outstanding balances, and available credit.
- Light and dark mode support.

### Installment Tracking
- Log purchases with full amortization schedules.
- Supports both 0% interest and interest-bearing installments.
- Due date tracking and upcoming due alerts.
- Options for early payoff and group deletion of installment schedules.

### Multi-Profile System
- Add family members or authorized users to shared credit cards.
- Track purchases by profile.
- Filter transactions by specific users.

### Dashboard & Analytics
- Overview of total credit, outstanding balances, and available credit.
- Notification center for payment reminders.
- Spending breakdown by category.

### Security
- Biometric authentication (fingerprint and face unlock).
- Local data encryption using SQLCipher. Data is stored on-device.
- Secure API key injection from `local.properties` to keep repository secrets safe.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM + Clean Architecture
- **Dependency Injection:** Hilt
- **Database:** Room + SQLCipher
- **AI Models:** Groq API (`openai/gpt-oss-20b`, `qwen/qwen3.8-27b`, `whisper-large-v3-turbo`)
- **Networking:** Retrofit + OkHttp
- **Build System:** Gradle (KTS) with KSP

## Getting Started

### Prerequisites
- Android Studio (Ladybug or later recommended)
- JDK 17+
- Android SDK 36 (Minimum SDK 31)

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/aydreian/CreditTracker-PH.git
   cd CreditTracker-PH
   ```

2. **Configure API Key:**
   Add your Groq API key to the `local.properties` file in the project root:
   ```properties
   groq.api.key=your_groq_api_key
   ```
   You can obtain an API key at [console.groq.com](https://console.groq.com).

3. **Build the project:**
   To build a debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
   To build a release APK:
   ```bash
   ./gradlew assembleRelease
   ```

## Download
Signed APKs are available on the [Releases](https://github.com/aydreian/CreditTracker-PH/releases) page.

## Author
[Aydreian](https://github.com/aydreian)
