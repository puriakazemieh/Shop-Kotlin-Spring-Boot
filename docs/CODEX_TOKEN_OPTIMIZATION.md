# Codex context efficiency — Spring Boot server

This repository is the server in the two-repository Carmilla project. The client repo `puriakazemieh/Shop-Kotlin-kmp` holds the shared Windows PowerShell setup scripts and the detailed instructions at `docs/engineering/CODEX_TOKEN_OPTIMIZATION.md`.

- Local Codex token baseline: run `tools/codex/measure-codex-usage.ps1 -Label baseline` from the **client** repo before graph indexing. The generated `.artifacts/` report stays local and reflects available Codex sessions, not a fabricated project-level percentage.
- GitNexus: run the client's `tools/codex/setup-gitnexus.ps1 -ServerRepoPath '<this repository path>'`. It indexes this repository **separately** using `analyze --index-only` and configures a single global Codex MCP connection.
- Confirm here with `npx --yes gitnexus@latest status`. The generated `.gitnexus/` index is excluded from Git.
- The `AGENTS.md` optimizes context navigation while preserving queue, security, transactional and verification requirements.
- No Graph indexing, Codex installation or token measurement can be verified from GitHub file changes alone; local execution is required.

No runtime logic, public API, database schema, secrets or queue statuses are changed by this configuration.
