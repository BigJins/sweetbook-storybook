# AGENTS.md

이 파일은 서브에이전트 역할 분담 문서다. 공통 규칙은 루트 `CLAUDE.md`를 따른다.

## 목표

- 리서치, 플랜, 구현, 리뷰를 섞지 않는다.
- 각 에이전트는 자기 책임 범위 안에서만 수정한다.
- 충돌 가능성이 높은 파일은 main에서만 다룬다.

## 역할

### 1. Main / Reviewer

책임:

- `docs/` 문서 관리
- 도메인 엔티티, enum, migration
- 주문 흐름, ZIP 익스포트, Docker, README, 제출물
- feature 브랜치 머지 전 검토
- smoke test와 최종 검증

주 작업 위치:

- `main`
- `backend/src/main/java/com/sweetbook/domain/**`
- `backend/src/main/resources/db/migration/**`
- `docker-compose.yml`
- `Dockerfile`
- `README.md`
- `docs/**`

### 2. Backend AI Agent

책임:

- Story REST API
- AI client, mock fallback
- 비동기 generation
- story 관련 controller/service/test

주 작업 브랜치/워크트리:

- `feature/backend-ai`
- `.worktrees/backend-ai`

주요 파일:

- `backend/src/main/java/com/sweetbook/service/StoryService.java`
- `backend/src/main/java/com/sweetbook/service/StoryGenerationService.java`
- `backend/src/main/java/com/sweetbook/service/ai/**`
- `backend/src/main/java/com/sweetbook/web/StoryController.java`
- `backend/src/test/java/com/sweetbook/web/StoryController*.java`
- `backend/src/test/java/com/sweetbook/integration/StoryCreateSmokeTest.java`

건드리면 안 되는 것:

- 주문/ZIP/Docker/README
- domain 패키지 구조 변경
- API camelCase 규칙 변경

완료 조건:

- story 생성/조회/재시도/재생성 흐름이 plan과 일치
- mock 모드와 비동기 테스트가 통과

### 3. Frontend UI Agent

책임:

- Vue router, view, component, composable
- 폼 검증, polling UI, empty/loading/error state
- 주문 모달과 주문 화면 UI

주 작업 브랜치/워크트리:

- `feature/frontend-ui`
- `.worktrees/frontend-ui`

주요 파일:

- `frontend/src/router/**`
- `frontend/src/views/**`
- `frontend/src/components/**`
- `frontend/src/composables/**`
- `frontend/src/types.ts`
- `frontend/src/api/**`

건드리면 안 되는 것:

- backend migration
- AI service 내부 구현
- Dockerfile / docker-compose

완료 조건:

- `/`, `/stories/new`, `/stories/:id`, `/orders`가 plan대로 동작
- API payload가 camelCase로 backend와 일치
- `useStoryStatus` 테스트와 주요 수동 검증 완료

## 문서 입력물

### 리서치 문서

리서치 단계 결과물에는 아래가 들어가야 한다.

- 시스템이 어떻게 동작하는가
- 어떤 파일들이 연관되는가
- 어떤 엣지 케이스가 있는가
- 어떤 패턴을 따라야 하는가

### plan 문서

구현 단계로 넘길 plan에는 아래가 들어가야 한다.

- 어떤 파일을 만들 건지
- 무엇을 바꿀 건지
- 어떻게 테스트할 건지
- 무엇을 바꾸지 않을지

## handoff 규칙

- 구현 에이전트는 가능하면 `plan.md` 성격의 문서와 자기 역할만 들고 시작한다.
- plan과 실제 코드가 다르면 리서치/플랜 단계로 되돌려 갱신한다.
- 머지 전에 main/reviewer가 범위 초과 변경을 확인한다.

## 브랜치/워크트리 규칙

- 공통 운영은 `docs/WORKTREES.md`를 따른다.
- feature 시작 전 `git fetch && git rebase origin/main`.
- DTO/API 계약 변경 시 즉시 main 기준 문서와 동기화.
- worktree 간 중복 수정이 예상되면 먼저 책임자를 다시 정한다.

## 커밋 규칙

- 작은 단위로 자주 커밋
- 커밋 메시지는 task 의미가 드러나게 작성
- 관련 테스트 또는 검증 결과 없이 대규모 묶음 커밋 금지

## 최종 원칙

- 설계보다 완성도 우선
- 최신 스택 욕심보다 데모 안정성 우선
- 문서, 코드, 테스트 셋 중 하나라도 어긋나면 바로 맞춘다
