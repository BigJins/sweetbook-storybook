# 에이전트 dispatch 프롬프트

세 역할(main/reviewer · backend-ai · frontend-ui)에 dispatch할 때 그대로 복사해 쓸 수 있는 운영 지침.

| 파일 | 역할 | 워크트리 | 권장 모델 등급 |
|------|------|----------|----------------|
| [`main-reviewer.md`](main-reviewer.md) | 베이스라인 유지 + feature 머지 + 주문/ZIP/Docker/README | `main` (`C:/dev/sweetbook-storybook`) | 상위 (Opus 등급) |
| [`backend-ai.md`](backend-ai.md) | Story REST + AI 통합 + 비동기 generation | `feature/backend-ai` (`.worktrees/backend-ai`) | 빠른 (Sonnet 등급) |
| [`frontend-ui.md`](frontend-ui.md) | Vue 라우터·뷰·컴포넌트·폴링·주문 모달·칸반 UI | `feature/frontend-ui` (`.worktrees/frontend-ui`) | 빠른 (Sonnet 등급) |

> 모델 등급 선택 원칙은 `CLAUDE.md`의 "모델 선택 원칙" 섹션 참고. 두 번 막히면 상위로 승격, 결정 끝나면 빠른 모델로 강등이 기본.

## 사용법

각 파일의 `---` 아래 본문을 그대로 에이전트 프롬프트로 복사. 상단의 안내(이 파일 위치·truth source 참조)는 사람용 컨텍스트.

## 공통 원칙 (세 프롬프트에 모두 박혀 있음)

- **truth source**: `docs/kim-sweetbook-plan-20260428.md` (구현 정의), `docs/kim-sweetbook-design-20260428.md` (설계 결정), `docs/WORKTREES.md` (브랜치 운영)
- **DTO 컨트랙트**: plan에 명시된 필드명·타입 정확히 사용. 변경 시 plan을 main에 push 후 feature 브랜치에서 rebase.
- **시작 절차**: `git fetch origin && git rebase origin/main`.
- **테스트 기준**: TDD 표시 step은 PASS 후 commit, 그 외는 컴파일·smoke 후 commit.
- **블록 시 동작**: 추측하지 말고 마지막 commit에 `BLOCKED: <사유>` 명시 후 종료.

## 역할 간 책임 경계 요약

| 영역 | main | backend-ai | frontend-ui |
|------|------|------------|-------------|
| 도메인 entities/enums | ✓ | ✗ | ✗ |
| db migration | ✓ | ✗ | ✗ |
| Story REST API | ✗ | ✓ | ✗ |
| AI client / mock | ✗ | ✓ | ✗ |
| Order REST API (Task 26) | ✓ | ✗ | ✗ |
| ZIP export (Task 29-30) | ✓ | ✗ | ✗ |
| Vue 라우터/뷰/컴포넌트 | ✗ | ✗ | ✓ |
| useStoryStatus polling | ✗ | ✗ | ✓ |
| OrderModal·KanbanColumn UI | ✗ | ✗ | ✓ |
| Dockerfile / docker-compose | ✓ | ✗ | ✗ |
| README / 제출 자료 | ✓ | ✗ | ✗ |
| `docs/*` 변경 | ✓ | ✗ | ✗ |

DTO 변경처럼 cross-cut 결정이 필요하면 **항상 main에서 먼저 반영 → push → feature rebase** 순서.
