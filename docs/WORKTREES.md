# 워크트리 운영 가이드

이 프로젝트는 **Strategy B + 옵션 2**로 작업을 격리합니다 — main + feature 워크트리 2개. 헥사고날·DDD 같은 무거운 구조 변경 없이, 패키지 경계만 가볍게 정리하고 작업 트랙만 분리합니다.

> **셸 가정**: 이 문서의 모든 명령·경로 예시는 **Git Bash**(또는 macOS/Linux bash) 기준입니다. PowerShell·cmd 사용자는 다음을 변환:
> - 경로 `/c/dev/sweetbook-storybook` → `C:\dev\sweetbook-storybook`
> - `./mvnw` → `.\mvnw.cmd`
> - `&&` → `;` (PowerShell 5.1) 또는 그대로 (PowerShell 7+)
> - `cp` → `Copy-Item`, `rm` → `Remove-Item` 등 명령어 변환
>
> 본 프로젝트는 Windows + Git Bash 환경에서 부트스트랩됨. 같은 환경 사용을 권장.

## 워크트리 토폴로지

| 경로 | 브랜치 | 담당 phase | 주 작업 |
|------|--------|------------|---------|
| `sweetbook-storybook/` | `main` | Phase 0·1·5·6·7·8·9 | 도메인·주문·ZIP·폴리시·Docker·README·제출 |
| `.worktrees/backend-ai/` | `feature/backend-ai` | Phase 2·3 | Story REST API + AI 통합 (OpenAI / mock fallback) |
| `.worktrees/frontend-ui/` | `feature/frontend-ui` | Phase 4 | Vue 라우터·뷰·컴포넌트·useStoryStatus |

## 왜 이렇게 나눴나

- **main**은 도메인 + 인프라 + 통합 단계가 모이는 trunk. Phase 1에서 entities/state machine을 박은 뒤 두 feature 워크트리가 갈라져 나오고, Phase 5부터 다시 main으로 합류.
- **feature/backend-ai**는 OPENAI_API_KEY를 다루고 비동기 generation 디버깅이 길어질 수 있는 영역. AI 호출 실패·재시도 같은 휘발성 코드를 다른 백엔드 코드와 격리하면 main이 안전.
- **feature/frontend-ui**는 Vue 변경이 백엔드 컴파일에 영향 주지 않아 자연스러운 병행 트랙. node_modules도 worktree에 갇혀 main의 디스크 영향 0.

## 작업 진입

```bash
# main 워크스페이스
cd /c/dev/sweetbook-storybook

# Backend AI 워크스페이스
cd /c/dev/sweetbook-storybook/.worktrees/backend-ai

# Frontend UI 워크스페이스
cd /c/dev/sweetbook-storybook/.worktrees/frontend-ui
```

각 워크스페이스는 자기 브랜치에 체크아웃된 독립 작업 디렉토리. `git status`·`git log`·`git commit`이 그 디렉토리 안에서 동작.

## Phase별 워크트리 매핑

```
Phase 0 (스캐폴드)              → main
Phase 1 (도메인 + 시드)         → main                       ← 먼저 끝나야 다른 트랙 시작 가능
Phase 2 (Story REST)            → feature/backend-ai
Phase 3 (AI 통합)               → feature/backend-ai
Phase 4 (Vue 프론트엔드)        → feature/frontend-ui        ← Phase 1 완료 후 시작
Phase 5 (주문 Lv2)              → main                       ← 두 feature 머지 후
Phase 6 (ZIP Lv3)               → main
Phase 7 (폴리시)                → main
Phase 8 (Docker 통합)           → main
Phase 9 (제출)                  → main
```

## 머지 케이던스

| 시점 | 행동 | 이유 |
|------|------|------|
| Phase 1 진행 중 매 task | `git push origin main` | feature 브랜치들이 빨리 rebase 가능 |
| feature 워크트리 시작 시 | `git fetch && git rebase origin/main` | 최신 도메인 위에서 작업 |
| Phase 4 시작 시점 | feature/backend-ai의 DTO 부분만 main에 먼저 머지하고 frontend에서 rebase | Vue가 실 API contract와 일치 |
| Phase 2/3 완료 | feature/backend-ai → main 머지 | trunk 합류 |
| Phase 4 완료 | feature/frontend-ui → main 머지 | 마지막 합류, Phase 5 시작 |

DTO/Controller 시그니처 변경이 일어나면 즉시 main push → 두 feature에서 `git fetch && git rebase origin/main`. 머지 충돌 최소화.

## 머지 방법

```bash
# main 워크스페이스에서
cd /c/dev/sweetbook-storybook
git fetch
git merge --no-ff feature/backend-ai     # 또는 --ff-only (linear history 선호 시)
git push origin main
```

`--no-ff` 사용 이유: 머지 커밋이 남아 어떤 task 묶음이 어디 들어왔는지 git log에서 식별 가능. 30h 마감 + 면접 시연에서 git log를 보여줄 때 가독성 ↑.

## 완료 보고 후 머지

상세 절차는 문서에 길게 적지 않는다. 백엔드/프론트엔드가 각각 완료 보고를 끝낸 뒤에는 로컬 스킬 `merge-release`를 사용한다.

예시 트리거:

- `$merge-release`
- `백엔드/프론트 완료 기준으로 순서 머지해줘`

스킬이 내부적으로 repo 스크립트 `scripts/merge-feature-sequence.ps1`를 사용해 backend 선머지/테스트 → frontend 후머지/빌드·테스트 순서를 강제한다.

## 워크트리 정리 (Phase 9 마무리 후)

```bash
cd /c/dev/sweetbook-storybook
git worktree remove .worktrees/backend-ai
git worktree remove .worktrees/frontend-ui
git branch -d feature/backend-ai feature/frontend-ui   # 머지된 브랜치만 삭제 허용
```

브랜치를 삭제 안 하고 그대로 두는 것도 OK — 면접 시연에서 git log 보여줄 때 흐름이 보기 좋음.

## 함정 / 주의사항

- **DB 인스턴스는 하나만**: 세 워크스페이스가 동시에 `docker compose up` 띄우면 3306 포트 + mysql-data 볼륨이 충돌. 한 번에 한 곳만 띄우거나 `.env`의 `DB_PORT`를 다르게.
- **uploads/ 볼륨**: 각 워크트리는 자기 `uploads/` 디렉토리. 통합 테스트는 main에서. feature 워크트리에서 시드 자산 점검은 `backend/src/main/resources/seed/`만 바라봄.
- **node_modules**: 일반 규칙 — `npm install`은 frontend 작업하는 워크트리에서만 1회 (= `feature/frontend-ui`). main에선 설치 안 해도 됨. **예외**: Phase 0 스캐폴드 직후 main에서 검증용으로 1회 install이 가능하지만, 본 프로젝트는 Phase 0 시점에 install 없이 커밋했음(c50def9). Phase 4 시작 직전 `feature/frontend-ui` 워크트리에서 `cd frontend && npm install` 실행 권장.
- **maven wrapper**: `backend/.mvn/wrapper/`는 git tracked → 모든 워크트리에 자동 복제됨. 별도 설치 불필요.
- **docs/ 동기화**: 설계·계획 문서(`docs/*.md`) 변경은 main에서만. 다른 worktree는 rebase로 가져옴.
- **`.worktrees/` 자체는 gitignored**: 워크트리 디렉토리가 main의 git status에 안 잡힘. 안전.

## 패키지 구조 컨벤션 (헥사고날 안 씀)

layered + 도메인 layer만 feature 그룹핑. 다른 layer는 평탄.

```
com.sweetbook/
  config/                      # WebConfig, AsyncConfig, AiConfig
  domain/
    story/                     # Story · StoryStatus · Page · PageLayout
    order/                     # Order · OrderStatus · OrderItem · BookSize · CoverType
  repository/                  # 인터페이스 3개 (flat)
  service/                     # StoryService · StoryGenerationService · OrderService · ZipExportService · FileStorageService · SeedService
    ai/                        # AiClient · OpenAiClient · MockAiClient · StyleDescriptor · StoryDraft
  web/
    dto/                       # 모든 record DTO
    StoryController · OrderController · GlobalExceptionHandler
```

**근거**:
- `domain/` 안에 entity·enum·value object 9개 → story/order로 그룹핑하면 한눈에 들어옴.
- repository·web은 파일 수가 적음(3-4개) → 평탄이 더 가독성 좋음.
- service는 5개로 적당하지만 `ai/` 서브패키지는 이미 있어서 그것만 유지.
- 면접에서 "왜 헥사고날 안 썼나요?" 질문엔 "30시간 마감 + 도메인 응집은 패키지 그룹핑으로 충분 + 추후 확장 시 헥사고날로 이행 쉬움"으로 답변.

## 컨벤션 기준 task 경로 변환

계획서(`docs/kim-sweetbook-plan-20260428.md`) Task 5/6/7의 도메인 파일 경로는 위 컨벤션을 적용하면:

| 원래 경로 | 변환 경로 |
|-----------|-----------|
| `domain/Story.java` | `domain/story/Story.java` |
| `domain/StoryStatus.java` | `domain/story/StoryStatus.java` |
| `domain/Page.java` | `domain/story/Page.java` |
| `domain/PageLayout.java` | `domain/story/PageLayout.java` |
| `domain/Order.java` | `domain/order/Order.java` |
| `domain/OrderStatus.java` | `domain/order/OrderStatus.java` |
| `domain/OrderItem.java` | `domain/order/OrderItem.java` |
| `domain/BookSize.java` | `domain/order/BookSize.java` |
| `domain/CoverType.java` | `domain/order/CoverType.java` |

Java `package` 선언과 다른 클래스에서 import도 동일하게 적용:
- `package com.sweetbook.domain;` → `package com.sweetbook.domain.story;` 또는 `package com.sweetbook.domain.order;`
- `import com.sweetbook.domain.Story;` → `import com.sweetbook.domain.story.Story;`
- 다른 layer(service/web/repository)에서 두 도메인 모두 참조 시 import 두 줄.
