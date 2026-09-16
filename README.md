# 💳 CreditTrack PH

A premium, AI-powered credit card installment tracker built for Filipino cardholders. Track multiple credit cards, manage installment payments, and let **Financier** — your AI bulldog assistant — handle the heavy lifting.

---

## ✨ Features

### 🏦 Multi-Card Management
- Add and manage multiple credit cards from major Philippine banks (BPI, BDO, Metrobank, Security Bank, UnionBank, and more)
- Track credit limits, outstanding balances, and available credit at a glance
- Beautiful card UI with bank-specific branding

### 📊 Installment Tracking
- Log installment purchases with full amortization schedules
- Support for **0% interest** and **interest-bearing** installments
- Automatic monthly amortization calculation
- Due date tracking with overdue alerts

### 🐕 Financier AI Assistant
- Built-in AI chat powered by Groq for lightning-fast responses
- Natural language commands to create installments and mark payments
- Smart task automation — just tell Financier what you need:
  - *"Add an installment on my BPI card for an iPhone, 24 months, no interest, ₱49,288.50"*
  - *"I have paid the laptop installment, please check it for me"*

### 👥 Who Swiped? — Profile System
- Add family members who share your credit cards
- Track which person made each transaction
- Filter transactions by profile (Overall, Main User, Family Members)
- AI-aware: say *"Create an installment for Nathan..."* and Financier assigns it automatically

### 📱 Dashboard & Analytics
- Real-time spending overview with total credit, outstanding, and available balances
- Upcoming due date alerts with urgency indicators
- Recent transaction feed with "Swiped by" labels
- Monthly spending breakdown

### 🔒 Security
- Biometric authentication support
- Encrypted local storage via SQLCipher
- All data stays on-device — no cloud sync required

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM + Clean Architecture |
| **DI** | Hilt (Dagger) |
| **Database** | Room + SQLCipher |
| **AI** | Groq API (LLM) |
| **Networking** | Retrofit + OkHttp |
| **Build** | Gradle (KTS) with KSP |

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

   Open `app/build.gradle.kts` and replace the placeholder:
   ```kotlin
   buildConfigField("String", "GROQ_API_KEY", "\"YOUR_GROQ_API_KEY_HERE\"")
   ```
   Get a free API key at [console.groq.com](https://console.groq.com)

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open in Android Studio and click ▶️ Run.

---

## 📦 Download

Check the [Releases](https://github.com/aydreian/CreditTracker-PH/releases) page for the latest APK.

> **Note:** The release APK is unsigned. To install on your device, you may need to enable "Install from unknown sources" or sign it with your own keystore.

---

## 📂 Project Structure

```
app/src/main/java/com/example/credittrackph/
├── data/
│   ├── db/          # Room database, DAOs, entities
│   ├── model/       # Bank info & enums
│   ├── network/     # Groq API service
│   └── repository/  # Data repositories
├── di/              # Hilt dependency injection modules
├── domain/
│   ├── calculator/  # Installment & due date calculators
│   └── usecase/     # SMS parser use case
├── notification/    # Due date reminder workers
├── presentation/
│   ├── components/  # Reusable UI components
│   ├── screen/      # Compose screens
│   └── viewmodel/   # ViewModels
├── security/        # Biometric & encryption managers
├── service/         # SMS receiver
├── theme/           # Colors, typography, theming
└── util/            # Preferences manager
```

---

## 🤝 Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## 👨‍💻 Author

**Aydreian** — [@aydreian](https://github.com/aydreian)

---

<p align="center">
  Made with ❤️ in the Philippines 🇵🇭
</p>
