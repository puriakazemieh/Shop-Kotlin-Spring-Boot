# Carmilla Spring Boot server — AI agent instructions

This Kotlin / Spring Boot backend is paired with `puriakazemieh/Shop-Kotlin-kmp` (client + WordPress products). The client repository owns the canonical `docs/tasks.md` queue, task cards, active product contract, master checklist, and `RTK.md`.

## Strict task management (never relax to save tokens)
1. **Queue order:** For queued product work, inspect the client's `docs/tasks.md`; pick **only the first READY task** and execute **exactly one task per turn**. Never choose TODO/BLOCKED or a later READY. If no READY exists, inspect dependencies, **STOP**, and report blockers.
2. **Required context:** Read the selected `docs/tasks/<TASK-ID>.md` and active `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` **in full** before coding. Use relevant master checklist sections, applicable `RTK.md` and task acceptance criteria.
3. **Oversized/ambiguous work:** For L/XL (size > M), too many boundaries, ambiguity, or missing dependencies, **STOP before coding**. Propose ordered XS/S/M subtasks with scope, dependency, acceptance and evidence criteria. Create/reorder task cards only if authorized. Do not implement any subtask until properly READY, nor in the decomposition turn. Report unresolved decisions.
4. **Safety:** Check `git status --short` before editing; preserve user changes. Keep a minimal task-scoped diff. No unrelated upgrades/refactors or secret/PII disclosures.
5. **Evidence and QA:** Do not mark DONE without task evidence in the client's `docs/evidence/<TASK-ID>/` and required verification. For network/UI/migration impacts, specify human QA location, steps and success criteria and leave `AWAITING_MANUAL_QA` until confirmed; use `BLOCKED` when external dependencies prevent progress.
6. **Queue updates:** After genuine completion, update **only the current task's row and card Completion record** and the evidenced master checklist item. Next task may be READY only when dependencies are satisfied; **never execute it in the same turn**. Do not assert unverified gate completion.
7. **Maintenance exception:** For explicit agent/tooling maintenance outside the queue, keep it isolated and do not change queued task states.

## Backend architecture and verification
- Keep API behavior consistent with approved client contracts; inspect related controllers, services, repositories, migrations, and tests before modifying data semantics.
- Protect tenant and authorization boundaries, avoid permissive production CORS, and keep payment/order operations transactional and atomic. Apply client `RTK.md` stop-ship rules.
- Run focused Gradle tests first (Windows: `./gradlew.bat test --tests "<pattern>"`), then broader required gates. Report actual outcomes; do not bypass mandatory QA.

## Token-efficient navigation (subordinate to task rules)
- Prefer targeted `rg --files` / `rg -n` and symbol-level reads in `src/main/kotlin` or `src/test/kotlin` to recursive file dumps. Never skip mandatory full task-card / contract reads.
- If the **server's own** GitNexus index is current and MCP connected, use `query`, `context`, and `impact` for substantial dependency analysis; verify against source. Use normal search if unavailable; skip graph overhead for small fixes.
- The server and KMP client have **separate** graphs by default; do not infer cross-repository edges without checking actual contracts.

## Post-commit graph refresh and weekly Codex usage reporting
- **After every successful Git commit created by this agent**, refresh the GitNexus index for **each repository in which a commit was made** before concluding the task. In that repository run `npx --yes gitnexus@latest analyze --index-only` and then `npx --yes gitnexus@latest status`; inspect the exit codes and freshness. Do not regenerate or overwrite `AGENTS.md`, do not commit generated `.gitnexus/` data, and do not create an extra commit just for indexing. If GitNexus is not installed, its license is not cleared, or indexing fails, report the precise blocker; do not auto-install or claim the graph is current. This instruction covers **commits made by Codex**; Git commits made independently in an IDE/terminal need a separately installed local `post-commit` hook to guarantee refresh.
- **Once per ISO calendar week, on the next active local Codex session** (not a background job), check for the canonical report `<KMP repo>/.artifacts/codex-usage/weekly/YYYY-Www.md`. If absent and local ccusage/Codex logs are accessible, run `npx --yes ccusage@latest codex daily --last 7 --json --no-cost` (and `codex session --json --no-cost` only if needed for session-level analysis). Verify the returned JSON, save raw aggregate data and a concise Markdown summary alongside the marker under the ignored `.artifacts/codex-usage/weekly/` folder, and **show the user the weekly summary in the Codex chat**. Include the exact covered dates, measured input/output/cached token counts and total, limitations, and comparison to a prior matching period **only if valid and comparable**. The report is local Codex CLI-log usage, not ChatGPT subscription credits nor reliably restricted to Shop; do not invent savings. Create the weekly marker only after successful measurement and reporting. If unavailable, explain briefly without blocking the task or claiming a report was delivered. Reuse the shared KMP marker when working from the server repository to prevent duplicate reports.
- Reporting is best-effort **while Codex is actively used**, never a promise to send notifications when Codex is closed. This maintenance must never bypass queued-task constraints, required QA, security checks, or explicit owner approval.

GitNexus setup and local ccusage baseline: paired KMP repository `tools/codex/` and `docs/engineering/CODEX_TOKEN_OPTIMIZATION.md`.
