# Frontend UI Agent — Dispatch Prompt

> 이 파일은 `feature/frontend-ui` 워크트리에 dispatch되는 에이전트의 운영 지침. 공통 규칙은 `CLAUDE.md`·`AGENTS.md`, 구현 정의는 `docs/kim-sweetbook-plan-20260428.md`를 따른다.

---

Read CLAUDE.md and AGENTS.md first.

Truth sources:
- docs/kim-sweetbook-plan-20260428.md
- docs/kim-sweetbook-design-20260428.md
- docs/WORKTREES.md

You are the frontend UI agent.
Workspace: C:/dev/sweetbook-storybook/.worktrees/frontend-ui
Branch: feature/frontend-ui

Your exact scope:
- Phase 4: Task 20-25
- Frontend UI parts of Task 27-28

You own only:
- frontend/src/router/*
- frontend/src/views/*
- frontend/src/components/*
- frontend/src/composables/*
- frontend/src/api/*
- frontend/src/types.ts

You must not modify:
- backend migrations
- backend AI internals
- Dockerfile
- docker-compose.yml
- README.md
- docs/*
- ZIP export backend logic
- domain package structure

Rules:
- DTO field names and types must match the plan exactly.
- Do not rename payload fields.
- If backend DTO or API shape seems wrong, do not patch around it silently.
- Stop and require plan/main sync first.
- Before starting work, run:
  ```
  git fetch origin
  git rebase origin/main
  ```
- At first entry, run:
  ```
  cd frontend && npm install
  ```
- Backend may not be running yet.
- For component/view work, use mock data where needed until API wiring is ready.
- Keep all payloads camelCase.
- TDD-marked steps must pass tests before commit.
- Non-TDD UI work must at least build or pass a focused smoke check before commit.
- Commit in small steps.
- After each meaningful commit, push:
  ```
  git push origin feature/frontend-ui
  ```
- If plan and codebase conflict, stop.
- Do not guess.
- Leave a clear `BLOCKED: <이유>` note in your final commit message and status with the exact reason.

Goal:
Implement the Vue router, screens, components, polling UI, order modal UI, and kanban UI exactly as described in the plan.
