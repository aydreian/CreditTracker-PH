package com.example.credittrackph.data.model

enum class Bank(
    val displayName: String,
    val shortCode: String,
    val monthlyInterestRate: Double, // as decimal e.g. 0.0354
    val lateFeeAmount: Double
) {
    BDO("BDO Unibank", "BDO", 0.0354, 850.0),
    BPI("Bank of the Philippine Islands", "BPI", 0.02, 850.0),
    RCBC("Rizal Commercial Banking Corp", "RCBC", 0.02, 850.0),
    METROBANK("Metrobank", "METRO", 0.03, 1000.0),
    UNIONBANK("UnionBank", "UBP", 0.02, 900.0),
    SECURITY_BANK("Security Bank", "SECU", 0.02, 800.0),
    EASTWEST("EastWest Bank", "EWB", 0.035, 850.0),
    PNB("Philippine National Bank", "PNB", 0.02, 750.0),
    CHINABANK("China Bank", "CBC", 0.02, 850.0),
    CITIBANK("Citibank Philippines", "CITI", 0.02, 850.0),
    HSBC("HSBC Philippines", "HSBC", 0.03, 1000.0),
    LANDBANK("Landbank of the Philippines", "LBP", 0.02, 700.0),
    PSB("Philippine Savings Bank", "PSB", 0.02, 800.0),
    AUB("Asia United Bank", "AUB", 0.025, 850.0),
    MAYBANK("Maybank Philippines", "MAYB", 0.02, 850.0),
    OTHER("Other Bank", "OTHER", 0.02, 850.0)
}

enum class CardType(val displayName: String) {
    VISA("Visa"),
    MASTERCARD("Mastercard"),
    JCB("JCB"),
    AMEX("AMEX"),
    OTHER("Other")
}

enum class ExpenseCategory(val displayName: String, val emoji: String) {
    FOOD("Food & Dining", "🍔"),
    TRANSPORT("Transport", "🚗"),
    SHOPPING("Shopping", "🛍️"),
    UTILITIES("Utilities", "💡"),
    HEALTH("Health & Medical", "🏥"),
    ENTERTAINMENT("Entertainment", "🎬"),
    TRAVEL("Travel", "✈️"),
    EDUCATION("Education", "📚"),
    GROCERIES("Groceries", "🛒"),
    ONLINE("Online Purchase", "💻"),
    OTHER("Other", "📦")
}

enum class ExpenseSource { MANUAL, SMS_AUTO }

enum class InterestType { ZERO_PERCENT, WITH_INTEREST, UNCERTAIN }
