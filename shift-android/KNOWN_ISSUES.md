# Known issues and unverified items
1. Never compiled or run. Expect compile errors on first build; fix and record them here.
2. Dependency versions unverified against current releases; targetSdk may need raising for Play.
3. No Gradle wrapper jar; no APK; no signing key; no Android App Bundle.
4. Unit tests (`InterestSelectionTest`, `QuizScorerTest`, `ContentAssetsTest`) written, not executed.
5. Quiz state uses `remember`, so an in-progress quiz resets on rotation or process death.
6. Dark theme surface colours (#17191C) are my choice, not contrast-checked beyond reasoning.
7. Touch-target size on filter chips is the Material default and not audited; no TalkBack pass done.
8. Lesson content is a draft: top-tier brake fluid label (DOT 5 vs 5.1) unconfirmed; hydraulic principles rest on trade sources.
9. Sources panel shows URLs as text only (no browser link, by design, because the app is offline).
10. Curriculum lesson counts (395 planned) are planning estimates, not validated.
11. Only US/UN/EU/UK/Japan regulation sources researched; many research gaps remain (see docs/SHIFT_Source_Registry.xlsx, Conflicts & Gaps).
