# 💳 CreditTrack PH

A premium, AI-powered credit card installment tracker built for Filipino cardholders. Track multiple credit cards, manage installment payments, and let **Financier** — your AI bulldog assistant — handle the heavy lifting with voice dictation, speech synthesis, and smart category automation.

---

## ✨ Features

### 🎙️ Financier AI Voice & Copilot (Stage 8 & 8.5)
- **Groq Whisper AI (`whisper-large-v3-turbo`):** State-of-the-art multilingual voice dictation with sub-second transcription latency.
- **Push-to-Talk (Hold-to-Speak & Tap-to-Toggle):** Zero Google modal popups. Record audio directly in-app by holding the microphone with haptic feedback and live soundwave animation.
- **Auto-Punctuation & Vocabulary Steering:** Solved accent misinterpretations (e.g., *"mark as done"* is never misheard as *"asdan"*), with native capitalization, commas, and periods.
- **Text-to-Speech (TTS):** Native audio playback of Financier's answers with per-message speaker buttons and an Auto-Read toggle.
- **AI Undo & Reversal:** Easily reverse accidental payments through natural chat commands (*"undo that"*, *"mark as unpaid"*) or one-tap UI buttons.
- **Smart Category Inference:** Automatically infers expense categories from Filipino brands and keywords (Jollibee, Grab, Angkas, Puregold, SM, Meralco, Mercury Drug, Shopee, Lazada, Uniqlo, etc.).

### 🏦 Multi-Card Management
- Add and manage multiple credit cards from major Philippine banks (BPI, BDO, Metrobank, Security Bank, UnionBank, RCBC, EastWest, and more)
- Authentic Philippine bank badges and card network logos (Mastercard, Visa, JCB, Amex)
- Track credit limits, outstanding balances, and available credit at a glance
- Clean light mode & dark mode support with instant theme switching

### 📊 Installment Lifecycle Management
- Log installment purchases with full amortization schedules
- Support for **0% interest** and **interest-bearing** installments
- Automatic monthly amortization calculation
- Due date tracking with upcoming due alerts
- **Early Payoff:** Settle all remaining months of an installment plan early via AI or direct UI button
- **Group Deletion:** Delete an entire multi-month installment schedule with one tap

### 👥 Who Swiped? — Multi-Profile System
- Add family members or authorized users who share your credit cards
- Track which person made each purchase
- Filter transactions by profile (Overall transaction, or specific family member)
- AI-aware: say *"Nathan bought Nike shoes for ₱4,500 on 3 months installment"* and Financier assigns it automatically

### 📱 Dashboard & Analytics
- Real-time spending overview with total credit, outstanding, and available balances
- Upcoming due date alerts with urgency indicators
- Notification Center with SMS auto-tracking status and 7-day payment reminders
- Smooth animated donut chart and category breakdown with entrance animations

### 🔒 Security & Privacy
- Biometric authentication support (fingerprint / face unlock)
- Encrypted local storage via SQLCipher
- All financial data stays strictly on your device — no third-party cloud database required

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM + Clean Architecture |
| **DI** | Hilt (Dagger) |
| **Database** | Room + SQLCipher |
| **AI LLM** | Groq API (`openai/gpt-oss-20b` & `qwen/qwen3.8-27b`) |
| **AI STT** | Groq Whisper (`whisper-large-v3-turbo`) |
| **Networking** | Retrofit + OkHttp |
| **Build** | Gradle (KTS) with KSP & ProGuard/R8 |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or later
- JDK 17+
- Android SDK 36 (min SDK 31)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/aydreian/CreditTracker-PH.git
   cd CreditTracker-PH
   ```

2. **Add your Groq API Key**
   Add your free Groq API key to your local `local.properties` file:
   ```properties
   groq.api.key=gsk_your_groq_api_key_here
   ```
   Get a free API key at [console.groq.com](https://console.groq.com).

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or generate the signed release APK:
   ```bash
   ./gradlew assembleRelease
   ```

---

## 📦 Download

Check the [Releases](https://github.com/aydreian/CreditTracker-PH/releases) page for the latest signed APK (`app-release.apk`).

---

## 👨‍💻 Author

**Aydreian** — [@aydreian](https://github.com/aydreian)

---

<p align="center">
  Made with ❤️ in the Philippines 🇵🇭
</p>
