# Carmilla Spring Boot server — AI agent instructions

This Kotlin / Spring Boot backend is paired with `puriakazemieh/Shop-Kotlin-kmp` (client + WordPress products).

## Task safety and architecture
- When implementing a queued Carmilla product task, use the client's `docs/tasks.md`: **only first READY**, then read its `docs/tasks/<TASK-ID>.md` and active `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` contract fully; use relevant master checklist sections. For explicitly requested agent/tool maintenance, do not change product-task statuses.
- Start with `git status --short`; preserve user changes; keep a minimal diff, no unrelated refactors or dependency upgrades.
- Keep API behavior consistent with the client and approved contracts; inspect nearby controllers, services, repositories, migrations and tests before changing data semantics.
- Never expose secrets or PII. Keep authorization and tenant boundaries, avoid permissive production CORS, and preserve transactional/atomic payment and order behavior. Follow the client's `RTK.md` where relevant.
- Do not mark queued work DONE without appropriate evidence and required manual QA confirmation for network/migration/UI behavior; record evidence in the client project's `docs/evidence/<TASK-ID>/`.

## Token-efficient navigation
- Prefer scoped `rg --files` / `rg -n` and symbol-focused code reads under `src/main/kotlin` or `src/test/kotlin` to recursive file dumps.
- If GitNexus MCP is connected and the **server's own** index is fresh, use `query`, `context`, and `impact` for significant dependency investigations. Check relevant actual source before editing. Small straightforward fixes do not need graph calls.
- This server and the KMP client have **separate** indexes by default; do not assume cross-repository relationships are in one graph.
- Run focused Gradle tests first (`./gradlew.bat test --tests "<pattern>"` on Windows), then required broader gates; report what was actually executed.

GitNexus setup and local ccusage baseline are scripted in the paired KMP repo: `tools/codex/` and `docs/engineering/CODEX_TOKEN_OPTIMIZATION.md`.
