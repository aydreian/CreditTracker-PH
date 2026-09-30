# UX Audit - CreditTrackPH

## Last Full Audit: 2026-09-30

### Finding: Bottom navigation/content overlap
* **Screen/Area:** Home, Transactions, Analytics, Wallet
* **Severity:** ‼️ Severe
* **Description:** The custom floating `BottomNavBar` partially obscures scrollable content at the bottom of the main screens because the parent container does not enforce adequate bottom padding.
* **Evidence:** Verified by inspecting `MainNavigation.kt` (where `BottomNavBar` is overlaid in a `Box` on top of `HorizontalPager`) and screen implementations (`HomeScreen.kt`, `TransactionsScreen.kt`, `WalletScreen.kt`, `AnalyticsScreen.kt`) which use arbitrary spacers or insufficient contentPadding (`32.dp`, `96.dp`, `80.dp`). The `BottomNavBar` height is 72dp plus 20dp padding, causing the overlap.

### Finding: Status-bar collisions
* **Screen/Area:** Home, Transactions, Statistics, Wallet
* **Severity:** ❗️⚠️ Medium
* **Description:** Potential collision between status bar and content due to unverified insets.
* **Evidence:** Unverified.

### Finding: Money truncation
* **Screen/Area:** Unknown
* **Severity:** ‼️ Severe
* **Description:** Monetary values might truncate under layout constraints.
* **Evidence:** Unverified.