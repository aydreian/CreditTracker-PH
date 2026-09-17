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

fun inferExpenseCategory(merchant: String): ExpenseCategory {
    val m = merchant.lowercase(java.util.Locale.getDefault())
    return when {
        m.contains("jollibee") || m.contains("mcdo") || m.contains("starbucks") || m.contains("kfc") ||
        m.contains("food") || m.contains("chowking") || m.contains("mang inasal") || m.contains("pizza") ||
        m.contains("restaurant") || m.contains("cafe") || m.contains("coffee") || m.contains("burger") ||
        m.contains("bistro") || m.contains("bakery") || m.contains("dining") -> ExpenseCategory.FOOD

        m.contains("grab") || m.contains("angkas") || m.contains("joyride") || m.contains("shell") ||
        m.contains("petron") || m.contains("caltex") || m.contains("gas") || m.contains("fuel") ||
        m.contains("parking") || m.contains("toll") || m.contains("easytrip") || m.contains("autosweep") ||
        m.contains("taxi") || m.contains("transport") -> ExpenseCategory.TRANSPORT

        m.contains("supermarket") || m.contains("grocer") || m.contains("puregold") || m.contains("robinsons super") ||
        m.contains("sm super") || m.contains("hypermarket") || m.contains("waltermart") || m.contains("dali") ||
        m.contains("savemore") || m.contains("market") -> ExpenseCategory.GROCERIES

        m.contains("meralco") || m.contains("maynilad") || m.contains("manila water") || m.contains("pldt") ||
        m.contains("globe") || m.contains("smart") || m.contains("converge") || m.contains("dito") ||
        m.contains("electricity") || m.contains("water") || m.contains("utility") || m.contains("internet") -> ExpenseCategory.UTILITIES

        m.contains("mercury drug") || m.contains("watsons") || m.contains("pharmacy") || m.contains("hospital") ||
        m.contains("clinic") || m.contains("doctor") || m.contains("medical") || m.contains("dental") ||
        m.contains("generika") || m.contains("health") -> ExpenseCategory.HEALTH

        m.contains("cinema") || m.contains("netflix") || m.contains("spotify") || m.contains("steam") ||
        m.contains("playstation") || m.contains("disney") || m.contains("movie") || m.contains("game") ||
        m.contains("concert") || m.contains("ticketnet") || m.contains("entertainment") -> ExpenseCategory.ENTERTAINMENT

        m.contains("cebu pacific") || m.contains("philippine airlines") || m.contains("pal") || m.contains("airasia") ||
        m.contains("hotel") || m.contains("resort") || m.contains("agoda") || m.contains("booking") ||
        m.contains("airbnb") || m.contains("klook") || m.contains("travel") || m.contains("flight") -> ExpenseCategory.TRAVEL

        m.contains("tuition") || m.contains("school") || m.contains("university") || m.contains("college") ||
        m.contains("books") || m.contains("national book store") || m.contains("fully booked") ||
        m.contains("academy") || m.contains("education") -> ExpenseCategory.EDUCATION

        m.contains("shopee") || m.contains("lazada") || m.contains("tiktok shop") || m.contains("amazon") ||
        m.contains("online") -> ExpenseCategory.ONLINE

        m.contains("uniqlo") || m.contains("zara") || m.contains("h&m") || m.contains("nike") ||
        m.contains("adidas") || m.contains("sm store") || m.contains("department store") || m.contains("apple") ||
        m.contains("samsung") || m.contains("mall") || m.contains("shopping") || m.contains("clothing") -> ExpenseCategory.SHOPPING

        else -> ExpenseCategory.OTHER
    }
}

