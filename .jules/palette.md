## $(date +%Y-%m-%d) - Add Content Descriptions to Icon-Only Buttons
**Learning:** Found several icon-only buttons (`IconButton`s) in Compose UI where `contentDescription` was explicitly set to `null`, making them inaccessible to screen readers.
**Action:** Always provide meaningful `contentDescription` (e.g., "Back", "More options") for standalone icons or icon buttons to ensure full accessibility support.
