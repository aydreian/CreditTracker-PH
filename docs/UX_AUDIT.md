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
* **Evidence:** Unverified candidate.

### Finding: Money truncation
* **Screen/Area:** Unknown
* **Severity:** ‼️ Severe
* **Description:** Monetary values might truncate under layout constraints.
* **Evidence:** Unverified candidate.

### Finding: Missing contentDescription on icon-only interactive controls
* **Screen/Area:** Global
* **Severity:** ❗️⚠️ Medium
* **Description:** Several `IconButton` and custom icon-only interactive components lack meaningful `contentDescription` properties.
* **Evidence:** Verified via codebase inspection. However, most `Icon` elements with `contentDescription = null` are accompanied by adjacent text labels or aren't independently interactive, which is standard accessibility practice to avoid TalkBack spam. Audit revealed no *interactive, icon-only* elements without descriptions. `IconButton` usages either have descriptions or are grouped with text in a way where adding a description to the icon would cause TalkBack spam.
