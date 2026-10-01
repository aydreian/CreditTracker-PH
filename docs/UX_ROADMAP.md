# UX Roadmap - CreditTrackPH

| Label | Title | Screen/Area | Status | PR | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| ‼️ Severe | Fix bottom navigation/content overlap | Home, Transactions, Analytics, Wallet | in progress | | Implemented calculated bottom padding passed from `MainNavigation` to screen contents. |
| ❗️⚠️ Medium | Status-bar collisions | Global | todo | | Needs verification. Unverified candidate. |
| ‼️ Severe | Money truncation | Global | todo | | Needs verification. Unverified candidate. |
| ❗️⚠️ Medium | Missing contentDescription on icon-only interactive controls | Global | done | | Audit found no actionable icon-only controls missing descriptions. All icons with `null` descriptions are decorative or have adjacent text. |
