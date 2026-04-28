# CLAUDE.md

이 파일은 이 저장소에서 작업하는 모든 AI 에이전트의 공통 작업 규칙이다.

## 목적

- 30시간 과제에서 컨텍스트 낭비를 줄이고 구현 일관성을 유지한다.
- 설계 판단은 `docs/kim-sweetbook-design-20260428.md`를 따른다.
- 구현 순서와 범위는 `docs/kim-sweetbook-plan-20260428.md`를 따른다.
- 워크트리/브랜치 운영은 `docs/WORKTREES.md`를 따른다.

## Truth Source

- 설계 의도: `docs/kim-sweetbook-design-20260428.md`
- 구현 기준: `docs/kim-sweetbook-plan-20260428.md`
- 작업 분리: `docs/WORKTREES.md`

문서가 충돌하면 우선순위는 다음과 같다.

1. `docs/kim-sweetbook-plan-20260428.md`
2. `docs/kim-sweetbook-design-20260428.md`
3. `docs/WORKTREES.md`

충돌을 발견하면 임의로 해석하지 말고 문서를 먼저 갱신한다.

## 프로젝트 규칙

- 현재 스택은 고정: Spring Boot 3.5, Java 21, JPA, MySQL 8, Vue 3, Vite, TypeScript, Tailwind.
- 헥사고날, DDD 대확장, Gradle 전환, Spring Boot 4 전환 금지.
- API/JSON/멀티파트/ZIP 내부 JSON은 모두 camelCase.
- SQL 스키마와 DB 컬럼명만 snake_case.
- Vue Router는 `createWebHistory()` 기준이며 SPA fallback을 깨뜨리면 안 된다.
- 이미지는 파일시스템 + Docker 볼륨 기준으로 유지한다.
- ZIP 익스포트 구조는 plan 문서의 14개 엔트리 기준을 유지한다.

## 패키지/파일 규칙

- 도메인 패키지는 `com.sweetbook.domain.story.*`, `com.sweetbook.domain.order.*`.
- `repository/`, `service/`, `web/dto/`는 평탄 구조 유지.
- 새 파일을 만들기 전에 기존 패턴을 먼저 확인한다.
- 범위 밖 리팩터링 금지.

## 구현 방식

- 한 번에 하나의 작은 단위를 구현한다.
- 각 변경은 plan 문서의 task/step에 대응되어야 한다.
- 변경할 파일, 바꾸지 않을 파일, 테스트 방법을 먼저 확인한다.
- 구현 중 plan과 실제 코드가 충돌하면 plan을 truth source로 보고 문서부터 수정한다.

## 빌드/테스트

- Git Bash 기준.
- Maven은 시스템 `mvn` 대신 `./mvnw` 사용.
- 프론트 작업은 필요한 워크트리에서만 `npm install`.
- 커밋 전 최소한 관련 테스트를 돌린다.

자주 쓰는 명령:

```bash
cd backend && ./mvnw -DskipTests compile
cd backend && ./mvnw test
cd backend && ./mvnw spring-boot:run

cd frontend && npm install
cd frontend && npm run dev
cd frontend && npm test -- --run

docker compose up
docker compose build --no-cache
```

## 커밋 전 체크

- plan 범위를 벗어난 변경이 없는가
- API 필드명이 camelCase로 유지되는가
- SPA fallback 관련 경로를 깨지 않았는가
- 관련 테스트 또는 수동 검증을 수행했는가
- 문서와 코드가 어긋나면 문서도 함께 갱신했는가

## 금지사항

- 임의로 API 계약 변경
- plan에 없는 대규모 구조 변경
- 관련 없는 파일 정리
- 테스트 없이 핵심 플로우 수정
- `main`에서 feature 역할까지 한꺼번에 뒤섞어 작업
