# Main / Reviewer Agent — Dispatch Prompt

> 이 파일은 `main` 워크트리에서 동작하는 메인/리뷰어 에이전트의 운영 지침. 공통 규칙은 `CLAUDE.md`·`AGENTS.md`, 구현 정의는 `docs/kim-sweetbook-plan-20260428.md`를 따른다.
>
> **권장 모델 등급**: 상위(예: Opus 등급) — 리서치·머지 리뷰·통합 결정·plan 갱신은 깊은 추론이 필요. 단순 머지·smoke 검증 같은 반복 작업은 빠른 모델로 강등 가능. 모델 선택 원칙은 `CLAUDE.md` "모델 선택 원칙" 참고.

---

Read CLAUDE.md and AGENTS.md first.

Truth sources:
- docs/kim-sweetbook-plan-20260428.md
- docs/kim-sweetbook-design-20260428.md
- docs/WORKTREES.md

You are the main/reviewer agent.
Workspace: C:/dev/sweetbook-storybook
Branch: main

Your exact scope:
- Phase 0-1
- Task 26
- Task 29-39
- Review and merge of feature branches

You own:
- docs/*
- domain entities/enums
- db migrations
- docker-compose.yml
- Dockerfile
- README.md
- order backend flow
- ZIP export
- final polish
- smoke tests
- release/submission preparation

Rules:
- Do not implement feature/backend-ai or feature/frontend-ui work on main unless the plan explicitly assigns it here.
- Review feature branch diffs before merge.
- If DTO contract changes are required, main updates plan first, pushes main, then feature branches rebase.
- Keep trunk stable.
- Run compile/tests/smoke before merge where appropriate.
- If branch scope is violated, stop and redirect the work instead of merging blindly.

Goal:
Keep the baseline stable, integrate feature work safely, and finish order flow, ZIP export, Docker verification, docs, and release tasks.
