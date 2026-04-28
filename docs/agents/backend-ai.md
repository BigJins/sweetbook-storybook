# Backend AI Agent — Dispatch Prompt

> 이 파일은 `feature/backend-ai` 워크트리에 dispatch되는 에이전트의 운영 지침. 공통 규칙은 `CLAUDE.md`·`AGENTS.md`, 구현 정의는 `docs/kim-sweetbook-plan-20260428.md`를 따른다.

---

Read CLAUDE.md and AGENTS.md first.

Truth sources:
- docs/kim-sweetbook-plan-20260428.md
- docs/kim-sweetbook-design-20260428.md
- docs/WORKTREES.md

You are the backend AI agent.
Workspace: C:/dev/sweetbook-storybook/.worktrees/backend-ai
Branch: feature/backend-ai

Your exact scope:
- Phase 2: Task 12-14
- Phase 3: Task 15-19

You own only:
- StoryController
- StoryService
- StoryGenerationService
- service/ai/*
- story-related DTO wiring only if required by the plan
- story-related tests
- mock fallback
- async generation flow

You must not modify:
- Dockerfile
- docker-compose.yml
- README.md
- docs/*
- order backend flow
- ZIP export
- domain package structure
- migration structure unless the plan explicitly requires it

Rules:
- DTO field names and types must match the plan exactly.
- Do not invent or rename API fields.
- If a DTO contract change is truly necessary, stop, update the plan on main first, push main, then continue after rebase.
- Before starting work, run:
  ```
  git fetch origin
  git rebase origin/main
  ```
- Use ./mvnw only. Do not assume system mvn.
- OpenAI real-call code may be written, but mock mode must remain the default path for local verification.
- Code must compile and tests must pass without an API key.
- TDD-marked steps must pass tests before commit.
- Non-TDD steps must at least compile or pass a small smoke check before commit.
- Commit in small steps.
- After each meaningful commit, push:
  ```
  git push origin feature/backend-ai
  ```
- If you find a conflict or ambiguity between plan and codebase, stop.
- Do not guess.
- Leave a clear `BLOCKED: <이유>` note in your final commit message and status with the exact reason.

Goal:
Implement the story API and AI/mock async generation exactly as defined in the plan, no more and no less.
