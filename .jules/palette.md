## 2026-09-30 - Contextual Content Descriptions for Icons
**Learning:** Setting `contentDescription = null` for all icons is an accessibility anti-pattern, but universally setting strings for all icons also causes TalkBack spam.
**Action:** Apply contextual rules for icons:
- Interactive icon-only control with no meaningful accessibility description → add it.
- Interactive icon-only control with an appropriate description → leave unchanged.
- Icon adjacent to identical visible text → use `contentDescription = null`.
- Decorative icon → use `contentDescription = null`.

## 2026-09-30 - Bottom Navigation Inset/Overlap Pattern
**Learning:** When using a custom overlaid/floating bottom navigation bar (e.g., in a `Box` above a `HorizontalPager`), using arbitrary padding or standard `WindowInsets` on individual screens causes overlap or unreachable content.
**Action:** The container component (e.g., `MainNavigation.kt`) is responsible for calculating the total reserved bottom height (`customNavHeight` + `verticalPadding` + `WindowInsets.navigationBars.calculateBottomPadding()`) and passing this exact value down to the scrollable content layers. Do not guess padding at the individual screen level.

## 2026-10-01 - Avoid Money Truncation Constraints
**Learning:** Hardcoding `softWrap = false` on `Text` components that display critical monetary values can cause the text to be hard-clipped and unreadable on smaller screens or when large font scaling is active.
**Action:** Let monetary text wrap naturally if space runs out by omitting `softWrap = false` (or explicitly allowing `softWrap = true`), or ensure adequate flexible space is guaranteed to the monetary field without forcing truncation.
