---
trigger: always_on
description: BulkYoutubeSearch-specific overrides and facts for the shared agent-workflow skills and rules. Wins over them.
---

# Project overrides — BulkYoutubeSearch

Shared skills/rules are links into agent-workflow (`scripts/link-project.sh`); this file and
`.agents/flows/` are this repo's own.

- **Android-only Jetpack Compose** (not CMP): from `cmp-best-practices` only the Compose, state and
  resources rules apply; there are no `commonMain`/platform source sets.
- **Theme**: `AppTheme` in `app/src/main/kotlin/.../bys/ui/theme/Theme.kt`; motion roles in
  `ui/theme/AppMotion.kt`. Components: `AppMotion`, `StaggeredEntrance`, `EmptyState`, `ShapeIcon`,
  `StatusPill`; search/filter/sort lists. Missing helpers (`DriftingShape`, `sharedMatchContainer`,
  `liquidRipple`, `ThemeReveal`): copy from SCUS into `ui/components` when first needed.
- **Preview example**: `app/src/main/kotlin/io/github/raghavsatyadev/bys/ui/components/ComponentsPreviews.kt StatusPillPreview`.
- **After a PR merges**: `git checkout master && git pull && git branch -d <branch> && git fetch --prune`.
