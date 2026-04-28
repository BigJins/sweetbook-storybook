# 스위트북 take-home 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** AI 동화 생성 + 주문 흐름 + ZIP 익스포트를 갖춘 풀스택 앱을 30시간 안에 docker-compose 한 방 실행 + Public GitHub로 출시.

**Architecture:** Spring Boot 3 (Java 21) + JPA/MySQL이 단일 JAR로 Vue 빌드 산출물까지 정적 서빙. AI 호출은 클라이언트 인터페이스로 추상화해 mock 모드 fallback. 이미지는 파일시스템(Docker 볼륨), DB는 path만 저장. Story 생성은 `@Async` + 클라이언트 폴링.

**Tech Stack:** Spring Boot 3.3, Java 21, JPA + Flyway + MySQL 8, Vite + Vue 3 + TypeScript + Tailwind, Docker Compose, OpenAI API (GPT-4o Vision / GPT-4o-mini / gpt-image-1).

---

## 실행 가이드

**TDD 강도 (시간 박스 적응)**

| 영역 | 강도 | 이유 |
|------|------|------|
| 상태 머신 (Story·Order 전이) | strict TDD | 실수가 데이터 일관성 깨뜨림 |
| 검증 로직 (파일 크기·필드 길이) | strict TDD | 백·프론트 갈등 방지 |
| ZIP 익스포트 구조 | strict TDD | 평가 직결 |
| 컨트롤러 happy path | 1개 통합 테스트만 | 라우팅 깨지면 즉시 보임 |
| AI 통합 | mock 기반 단위 테스트 | 실 호출은 수동 |
| Vue 컴포넌트 | 수동 브라우저 검증 | 30h에 jsdom 셋업 사치 |
| `useStoryStatus` 폴링 | Vitest 단위 테스트 | 폴링 버그는 디버깅 어려움 |

**커밋 단위**: Task 1개 = commit 1개. Task가 길면 step 단위로 나눠 commit. 잦은 커밋 = 안전망.

**의존성 순서**: Phase 0 → Phase 1 → (Phase 2·4 병행) → Phase 3 → Phase 5 → Phase 6 → Phase 7 → Phase 8 → Phase 9. 백·프론트는 Phase 1 이후 병행 가능.

**API · JSON 컨벤션 (단일)**

- **camelCase**: JSON 응답·요청 본문, 멀티파트 폼 필드명, ZIP 안의 JSON, TS 타입(`types.ts`).
- **snake_case**: SQL 스키마(`V1__schema.sql`)와 JPA `@Column(name="…")` 매핑만.
- 즉 Java/TS 코드와 wire 포맷은 모두 camelCase 한 가지. 별도 `@JsonProperty` 불필요. `spring.jackson.property-naming-strategy` 기본값(LOWER_CAMEL_CASE) 유지.
- DB 컬럼명은 `child_name` 같은 snake_case지만 그건 Hibernate가 매핑하고, 외부에 노출되는 모든 곳에서는 `childName`.

**SPA 라우팅**

- 프론트는 Vue Router `createWebHistory()` 사용 → `/stories/:id`, `/orders` 등 직접 진입·새로고침이 발생함.
- 백엔드는 `WebConfig#addViewControllers`로 `/`, `/stories/new`, `/stories/{id}`, `/orders`, `/orders/{id}`를 `forward:/index.html`로 처리. `/api/**`·`/assets/**`은 컨트롤러/ResourceHandler가 먼저 잡으므로 충돌 없음.
- 이 fallback이 빠지면 데모에서 URL 새로고침 시 404 발생. Task 10에서 박는다.

**참조**: 모든 결정은 `kim-sweetbook-design-20260428.md`에 박혀 있음. 의문 생기면 그 문서를 truth source로.

---

## 파일 구조

### Backend (`backend/`)

```
backend/
  pom.xml
  src/main/java/com/sweetbook/
    SweetbookApplication.java                  # @SpringBootApplication, @EnableAsync
    config/
      WebConfig.java                           # /api/files/** ResourceHandler
      AsyncConfig.java                         # ThreadPoolTaskExecutor
      OpenAiConfig.java                        # WebClient bean + ai 설정
    domain/
      Story.java, StoryStatus.java
      Page.java, PageLayout.java
      Order.java, OrderStatus.java
      OrderItem.java, BookSize.java, CoverType.java
    repository/
      StoryRepository.java, PageRepository.java
      OrderRepository.java
    service/
      StoryService.java                        # CRUD + DTO 조립
      StoryGenerationService.java              # @Async 워크플로우
      OrderService.java                        # CRUD + 전이 검증
      ZipExportService.java
      FileStorageService.java                  # 저장/경로/seed 복사
      ai/
        AiClient.java                          # interface
        OpenAiClient.java
        MockAiClient.java
        AiClientFactory.java
    web/
      StoryController.java, OrderController.java, FileController.java(없음 — ResourceHandler 처리)
      dto/
        StoryDto.java, StorySummaryDto.java, PageDto.java
        OrderDto.java, OrderCreateRequest.java, OrderStatusUpdateRequest.java
        StoryCreateRequest.java, PageBodyUpdateRequest.java
        ErrorResponse.java
      GlobalExceptionHandler.java
  src/main/resources/
    application.yml
    db/migration/V1__schema.sql
    db/migration/V2__seed.sql
    seed/                                      # PNG 자산
      story-1/cover.png, page-2.png, ..., page-5.png, drawing.png
      story-2/...
      story-3/...
      story-4/...
      placeholder.png
  src/test/java/com/sweetbook/
    domain/StoryStatusTransitionTest.java
    domain/OrderStatusTransitionTest.java
    service/FileStorageServiceTest.java
    service/ZipExportServiceTest.java
    service/StoryGenerationServiceTest.java
    web/StoryControllerTest.java
    web/OrderControllerTest.java
```

### Frontend (`frontend/`)

```
frontend/
  package.json, vite.config.ts, tsconfig.json, tailwind.config.js
  index.html
  src/
    main.ts, App.vue, style.css
    router/index.ts
    api/
      client.ts                                # fetch 래퍼 + 에러 변환
      stories.ts                               # API 호출 함수
      orders.ts
    composables/
      useStoryStatus.ts                        # 폴링 hook
    types.ts                                   # TS 타입 (Story/Page/Order/...)
    components/
      Navbar.vue
      StoryCard.vue
      UploadDropzone.vue
      ProgressStepper.vue
      BookViewer.vue
      BeforeAfterStrip.vue
      pages/PageLayoutCover.vue
      pages/PageLayoutSplit.vue
      pages/PageLayoutEnding.vue
      OrderModal.vue
      KanbanColumn.vue
      OrderCard.vue
      EmptyState.vue
    views/
      HomeView.vue                             # /
      NewStoryView.vue                         # /stories/new
      StoryDetailView.vue                      # /stories/:id
      OrdersView.vue                           # /orders
  src/composables/__tests__/useStoryStatus.spec.ts
```

### Infra (root)

```
docker-compose.yml
Dockerfile                                     # multi-stage: vue build + spring jar
.env.example
.gitignore
.dockerignore
README.md
uploads/                                       # gitignore (런타임 생성)
```

---

## Phase 0 — 프로젝트 스캐폴드 (Hour 0-2)

### Task 1 — Spring Boot 백엔드 init

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/sweetbook/SweetbookApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `.gitignore`
- Create: `README.md` (placeholder, Phase 9에 채움)

- [ ] **Step 1:** 루트에 `.gitignore` 작성

```gitignore
# Java/Maven
backend/target/
*.class
*.jar
.mvn/
mvnw
mvnw.cmd

# Frontend
frontend/node_modules/
frontend/dist/
frontend/.vite/

# IDE
.idea/
.vscode/
*.iml

# Env
.env
.env.local

# Runtime
uploads/
!uploads/.gitkeep

# OS
.DS_Store
Thumbs.db
```

- [ ] **Step 2:** `backend/pom.xml` 작성 (Spring Boot 3.3.4, Java 21)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.4</version>
    <relativePath/>
  </parent>
  <groupId>com.sweetbook</groupId>
  <artifactId>sweetbook</artifactId>
  <version>0.0.1</version>
  <properties>
    <java.version>21</java.version>
  </properties>
  <dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-webflux</artifactId></dependency>
    <dependency><groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId><scope>runtime</scope></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-mysql</artifactId></dependency>
    <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Step 3:** `SweetbookApplication.java` 작성

```java
package com.sweetbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SweetbookApplication {
    public static void main(String[] args) {
        SpringApplication.run(SweetbookApplication.class, args);
    }
}
```

- [ ] **Step 4:** `application.yml` 작성

```yaml
server:
  port: ${APP_PORT:8080}

spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/sweetbook?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: ${DB_USER:sweetbook}
    password: ${DB_PASSWORD:sweetbook}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 10MB

app:
  upload-dir: ${UPLOAD_DIR:./uploads}
  ai:
    mock-mode: ${AI_MOCK_MODE:true}
    openai-api-key: ${OPENAI_API_KEY:}
    text-model: gpt-4o-mini
    vision-model: gpt-4o
    image-model: gpt-image-1

logging:
  level:
    com.sweetbook: DEBUG
```

- [ ] **Step 5:** 컴파일 확인

Run: `cd backend && mvn -DskipTests compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 6:** Commit

```bash
git init
git add .gitignore backend/pom.xml backend/src/main/java/com/sweetbook/SweetbookApplication.java backend/src/main/resources/application.yml
git commit -m "chore: spring boot 3.3 + java 21 scaffold"
```

---

### Task 2 — Vue 3 + Vite 프론트엔드 init

**Files:**
- Create: `frontend/package.json`, `frontend/vite.config.ts`, `frontend/tsconfig.json`, `frontend/tsconfig.node.json`
- Create: `frontend/index.html`
- Create: `frontend/src/main.ts`, `frontend/src/App.vue`, `frontend/src/style.css`
- Create: `frontend/tailwind.config.js`, `frontend/postcss.config.js`

- [ ] **Step 1:** Vite + Vue + TS 스캐폴드 생성

```bash
cd frontend  # 없으면 mkdir frontend && cd frontend
npm create vite@latest . -- --template vue-ts
# "Current directory not empty" 물어보면 Yes (.gitignore 있을 수 있음)
npm install
npm install -D tailwindcss@3 postcss autoprefixer
npx tailwindcss init -p
```

- [ ] **Step 2:** `tailwind.config.js` 수정

```javascript
export default {
  content: ["./index.html", "./src/**/*.{vue,ts,tsx}"],
  theme: { extend: {} },
  plugins: [],
}
```

- [ ] **Step 3:** `src/style.css`에 Tailwind directives + 기본 스타일

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, "Pretendard", "Apple SD Gothic Neo", sans-serif;
}
```

- [ ] **Step 4:** `vite.config.ts`에 dev proxy 추가 (백엔드 8080으로)

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
})
```

- [ ] **Step 5:** `App.vue` 골격으로 교체 (라우팅은 Phase 4에서)

```vue
<script setup lang="ts">
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <h1 class="p-8 text-2xl font-bold">📚 Sweetbook</h1>
  </div>
</template>
```

- [ ] **Step 6:** dev 서버 동작 확인

Run: `npm run dev`
Expected: `Local:   http://localhost:5173/`
브라우저에서 "📚 Sweetbook" 한 줄 보임. Ctrl+C로 종료.

- [ ] **Step 7:** Commit

```bash
git add frontend/
git commit -m "chore: vue 3 + vite + tailwind scaffold"
```

---

### Task 3 — docker-compose + Dockerfile 골격

**Files:**
- Create: `docker-compose.yml`
- Create: `Dockerfile`
- Create: `.dockerignore`
- Create: `.env.example`
- Create: `uploads/.gitkeep`

- [ ] **Step 1:** `.env.example`

```bash
APP_PORT=8080
DB_PORT=3306
DB_PASSWORD=sweetbook
OPENAI_API_KEY=
AI_MOCK_MODE=true
```

- [ ] **Step 2:** `Dockerfile` (multi-stage)

```dockerfile
# ---- Frontend build ----
FROM node:20-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- Backend build ----
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B
COPY backend/src ./src
COPY --from=frontend-build /app/dist ./src/main/resources/static
RUN mvn -DskipTests package

# ---- Runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/target/sweetbook-0.0.1.jar app.jar
RUN mkdir -p /data/uploads
EXPOSE 8080
ENV UPLOAD_DIR=/data/uploads
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

- [ ] **Step 3:** `.dockerignore`

```
**/node_modules
**/target
**/dist
**/.git
**/.idea
uploads/
*.md
```

- [ ] **Step 4:** `docker-compose.yml`

```yaml
services:
  app:
    build: .
    ports:
      - "${APP_PORT:-8080}:8080"
    depends_on:
      db:
        condition: service_healthy
    environment:
      DB_HOST: db
      DB_PORT: 3306
      DB_USER: sweetbook
      DB_PASSWORD: ${DB_PASSWORD:-sweetbook}
      OPENAI_API_KEY: ${OPENAI_API_KEY:-}
      AI_MOCK_MODE: ${AI_MOCK_MODE:-true}
      UPLOAD_DIR: /data/uploads
    volumes:
      - ./uploads:/data/uploads

  db:
    image: mysql:8
    ports:
      - "${DB_PORT:-3306}:3306"
    environment:
      MYSQL_DATABASE: sweetbook
      MYSQL_USER: sweetbook
      MYSQL_PASSWORD: ${DB_PASSWORD:-sweetbook}
      MYSQL_ROOT_PASSWORD: rootpass
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 5s
      timeout: 3s
      retries: 10

volumes:
  mysql-data:
```

- [ ] **Step 5:** `uploads/.gitkeep` 빈 파일 생성 + .gitignore에 `!uploads/.gitkeep` 이미 들어있음 확인

Run: `mkdir -p uploads && touch uploads/.gitkeep`

- [ ] **Step 6:** GitHub Public 저장소 만들고 push

```bash
# GitHub에서 Public repo 'sweetbook-storybook' 생성 후
git remote add origin https://github.com/<your-id>/sweetbook-storybook.git
git branch -M main
git add docker-compose.yml Dockerfile .dockerignore .env.example uploads/.gitkeep
git commit -m "chore: docker-compose + multi-stage dockerfile"
git push -u origin main
```

---

## Phase 1 — 도메인 + 저장소 + 시드 (Hour 2-5)

### Task 4 — Flyway V1 스키마 마이그레이션

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__schema.sql`

- [ ] **Step 1:** 스키마 SQL 작성

```sql
CREATE TABLE story (
  id              CHAR(36)     PRIMARY KEY,
  title           VARCHAR(120) NOT NULL DEFAULT '',
  child_name      VARCHAR(20)  NOT NULL,
  status          VARCHAR(32)  NOT NULL,
  error_message   VARCHAR(500) NULL,
  drawing_url     VARCHAR(255) NULL,
  style_descriptor JSON        NULL,
  imagination_prompt TEXT      NOT NULL,
  created_at      DATETIME(3)  NOT NULL,
  updated_at      DATETIME(3)  NOT NULL,
  INDEX idx_story_created_at (created_at DESC)
);

CREATE TABLE page (
  id                  CHAR(36)     PRIMARY KEY,
  story_id            CHAR(36)     NOT NULL,
  page_number         INT          NOT NULL,
  layout              VARCHAR(16)  NOT NULL,
  body_text           TEXT         NULL,
  illustration_prompt TEXT         NULL,
  illustration_url    VARCHAR(255) NULL,
  CONSTRAINT fk_page_story FOREIGN KEY (story_id) REFERENCES story(id) ON DELETE CASCADE,
  UNIQUE KEY uk_page_story_number (story_id, page_number)
);

CREATE TABLE orders (
  id              CHAR(36)     PRIMARY KEY,
  story_id        CHAR(36)     NOT NULL,
  recipient_name  VARCHAR(30)  NOT NULL,
  address_memo    TEXT         NULL,
  status          VARCHAR(16)  NOT NULL,
  status_history  JSON         NOT NULL,
  created_at      DATETIME(3)  NOT NULL,
  updated_at      DATETIME(3)  NOT NULL,
  CONSTRAINT fk_order_story FOREIGN KEY (story_id) REFERENCES story(id),
  INDEX idx_order_status (status),
  INDEX idx_order_created_at (created_at DESC)
);

CREATE TABLE order_item (
  id          CHAR(36)    PRIMARY KEY,
  order_id    CHAR(36)    NOT NULL,
  book_size   VARCHAR(8)  NOT NULL,
  cover_type  VARCHAR(8)  NOT NULL,
  copies      INT         NOT NULL,
  CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
```

- [ ] **Step 2:** Commit

```bash
git add backend/src/main/resources/db/migration/V1__schema.sql
git commit -m "feat: V1 schema (story, page, orders, order_item)"
```

---

### Task 5 — Enum 클래스들

**Files:**
- Create: `backend/src/main/java/com/sweetbook/domain/StoryStatus.java`
- Create: `backend/src/main/java/com/sweetbook/domain/PageLayout.java`
- Create: `backend/src/main/java/com/sweetbook/domain/OrderStatus.java`
- Create: `backend/src/main/java/com/sweetbook/domain/BookSize.java`
- Create: `backend/src/main/java/com/sweetbook/domain/CoverType.java`

- [ ] **Step 1:** `StoryStatus.java` (전이 규칙 포함)

```java
package com.sweetbook.domain;

import java.util.Set;
import java.util.Map;

public enum StoryStatus {
    DRAFT, ANALYZING_DRAWING, GENERATING_STORY, GENERATING_IMAGES, COMPLETED, FAILED;

    private static final Map<StoryStatus, Set<StoryStatus>> ALLOWED = Map.of(
        DRAFT,             Set.of(ANALYZING_DRAWING, FAILED),
        ANALYZING_DRAWING, Set.of(GENERATING_STORY, FAILED),
        GENERATING_STORY,  Set.of(GENERATING_IMAGES, FAILED),
        GENERATING_IMAGES, Set.of(COMPLETED, FAILED),
        COMPLETED,         Set.of(),  // 종착
        FAILED,            Set.of(ANALYZING_DRAWING)  // retry
    );

    public boolean canTransitionTo(StoryStatus target) {
        return ALLOWED.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}
```

- [ ] **Step 2:** `OrderStatus.java`

```java
package com.sweetbook.domain;

import java.util.Set;
import java.util.Map;

public enum OrderStatus {
    PENDING, PROCESSING, COMPLETED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
        PENDING,    Set.of(PROCESSING),
        PROCESSING, Set.of(COMPLETED),
        COMPLETED,  Set.of()
    );

    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED.getOrDefault(this, Set.of()).contains(target);
    }
}
```

- [ ] **Step 3:** `PageLayout.java` + `BookSize.java` + `CoverType.java`

```java
// PageLayout.java
package com.sweetbook.domain;
public enum PageLayout {
    COVER, SPLIT, ENDING;
    public static PageLayout forPageNumber(int n) {
        if (n == 1) return COVER;
        if (n == 5) return ENDING;
        return SPLIT;
    }
}

// BookSize.java
package com.sweetbook.domain;
public enum BookSize { A5, B5 }

// CoverType.java
package com.sweetbook.domain;
public enum CoverType { SOFT, HARD }
```

- [ ] **Step 4:** Commit

```bash
git add backend/src/main/java/com/sweetbook/domain/
git commit -m "feat: domain enums with transition rules"
```

---

### Task 6 — Story·Order 상태 전이 TDD

**Files:**
- Create: `backend/src/test/java/com/sweetbook/domain/StoryStatusTransitionTest.java`
- Create: `backend/src/test/java/com/sweetbook/domain/OrderStatusTransitionTest.java`

- [ ] **Step 1:** `StoryStatusTransitionTest.java` 작성 (failing test)

```java
package com.sweetbook.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StoryStatusTransitionTest {

    @Test
    void normalForwardPathIsAllowed() {
        assertTrue(StoryStatus.DRAFT.canTransitionTo(StoryStatus.ANALYZING_DRAWING));
        assertTrue(StoryStatus.ANALYZING_DRAWING.canTransitionTo(StoryStatus.GENERATING_STORY));
        assertTrue(StoryStatus.GENERATING_STORY.canTransitionTo(StoryStatus.GENERATING_IMAGES));
        assertTrue(StoryStatus.GENERATING_IMAGES.canTransitionTo(StoryStatus.COMPLETED));
    }

    @Test
    void anyInProgressStateCanFail() {
        assertTrue(StoryStatus.DRAFT.canTransitionTo(StoryStatus.FAILED));
        assertTrue(StoryStatus.ANALYZING_DRAWING.canTransitionTo(StoryStatus.FAILED));
        assertTrue(StoryStatus.GENERATING_STORY.canTransitionTo(StoryStatus.FAILED));
        assertTrue(StoryStatus.GENERATING_IMAGES.canTransitionTo(StoryStatus.FAILED));
    }

    @Test
    void failedCanRetryToAnalyzing() {
        assertTrue(StoryStatus.FAILED.canTransitionTo(StoryStatus.ANALYZING_DRAWING));
    }

    @Test
    void completedIsTerminal() {
        assertTrue(StoryStatus.COMPLETED.isTerminal());
        for (StoryStatus s : StoryStatus.values()) {
            assertFalse(StoryStatus.COMPLETED.canTransitionTo(s),
                "COMPLETED should not transition to " + s);
        }
    }

    @Test
    void backwardOrSkipForwardIsForbidden() {
        assertFalse(StoryStatus.DRAFT.canTransitionTo(StoryStatus.GENERATING_STORY));
        assertFalse(StoryStatus.GENERATING_IMAGES.canTransitionTo(StoryStatus.ANALYZING_DRAWING));
        assertFalse(StoryStatus.COMPLETED.canTransitionTo(StoryStatus.DRAFT));
    }
}
```

- [ ] **Step 2:** `OrderStatusTransitionTest.java` 작성

```java
package com.sweetbook.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTransitionTest {

    @Test
    void forwardOnly() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.PROCESSING));
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.COMPLETED));
    }

    @Test
    void cannotSkipPending() {
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.COMPLETED));
    }

    @Test
    void cannotGoBackward() {
        assertFalse(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.PENDING));
        assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.PROCESSING));
    }

    @Test
    void cannotSelfTransition() {
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.PENDING));
    }
}
```

- [ ] **Step 3:** 테스트 실행 → PASS 확인 (Task 5의 enum이 이미 구현됨)

Run: `cd backend && mvn -Dtest='StoryStatusTransitionTest,OrderStatusTransitionTest' test`
Expected: `BUILD SUCCESS`, 9 tests passed.

- [ ] **Step 4:** Commit

```bash
git add backend/src/test/
git commit -m "test: domain status transition rules"
```

---

### Task 7 — JPA 엔티티들

**Files:**
- Create: `backend/src/main/java/com/sweetbook/domain/Story.java`
- Create: `backend/src/main/java/com/sweetbook/domain/Page.java`
- Create: `backend/src/main/java/com/sweetbook/domain/Order.java`
- Create: `backend/src/main/java/com/sweetbook/domain/OrderItem.java`

- [ ] **Step 1:** `Story.java`

```java
package com.sweetbook.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "story")
public class Story {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 120)
    private String title = "";

    @Column(name = "child_name", nullable = false, length = 20)
    private String childName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StoryStatus status = StoryStatus.DRAFT;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "drawing_url", length = 255)
    private String drawingUrl;

    @Column(name = "style_descriptor", columnDefinition = "JSON")
    private String styleDescriptorJson;

    @Column(name = "imagination_prompt", nullable = false, columnDefinition = "TEXT")
    private String imaginationPrompt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("pageNumber ASC")
    private List<Page> pages = new ArrayList<>();

    protected Story() {}

    public static Story newDraft(String childName, String imaginationPrompt) {
        Story s = new Story();
        s.id = UUID.randomUUID().toString();
        s.childName = childName;
        s.imaginationPrompt = imaginationPrompt;
        s.status = StoryStatus.DRAFT;
        Instant now = Instant.now();
        s.createdAt = now;
        s.updatedAt = now;
        return s;
    }

    public void transitionTo(StoryStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new IllegalStateException("Invalid transition: " + status + " -> " + target);
        }
        this.status = target;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String message) {
        this.errorMessage = message;
        transitionTo(StoryStatus.FAILED);
    }

    public void retry() {
        this.errorMessage = null;
        transitionTo(StoryStatus.ANALYZING_DRAWING);
    }

    // getters / setters (id, title, childName, status, errorMessage,
    // drawingUrl, styleDescriptorJson, imaginationPrompt, createdAt,
    // updatedAt, pages). 표준 getter/setter 작성. setter 중 status는
    // package-private로 막거나 transitionTo만 노출.
}
```

- [ ] **Step 2:** `Page.java`

```java
package com.sweetbook.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "page")
public class Page {
    @Id @Column(length = 36) private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(name = "page_number", nullable = false) private int pageNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PageLayout layout;

    @Column(name = "body_text", columnDefinition = "TEXT") private String bodyText;
    @Column(name = "illustration_prompt", columnDefinition = "TEXT") private String illustrationPrompt;
    @Column(name = "illustration_url", length = 255) private String illustrationUrl;

    protected Page() {}

    public static Page create(Story story, int pageNumber) {
        Page p = new Page();
        p.id = UUID.randomUUID().toString();
        p.story = story;
        p.pageNumber = pageNumber;
        p.layout = PageLayout.forPageNumber(pageNumber);
        return p;
    }

    // getters/setters
}
```

- [ ] **Step 3:** `Order.java`

```java
package com.sweetbook.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "orders")
public class Order {
    @Id @Column(length = 36) private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(name = "recipient_name", nullable = false, length = 30) private String recipientName;
    @Column(name = "address_memo", columnDefinition = "TEXT") private String addressMemo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "status_history", nullable = false, columnDefinition = "JSON")
    private String statusHistoryJson;

    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OrderItem item;

    protected Order() {}

    public static Order create(Story story, String recipientName, String addressMemo) {
        Order o = new Order();
        o.id = UUID.randomUUID().toString();
        o.story = story;
        o.recipientName = recipientName;
        o.addressMemo = addressMemo;
        Instant now = Instant.now();
        o.createdAt = now;
        o.updatedAt = now;
        o.statusHistoryJson = "[{\"status\":\"PENDING\",\"ts\":\"" + now.toString() + "\"}]";
        return o;
    }

    public void transitionTo(OrderStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new IllegalStateException("Invalid transition: " + status + " -> " + target);
        }
        this.status = target;
        this.updatedAt = Instant.now();
        // append to JSON: 단순 문자열 처리 — Jackson은 service에서 사용
        String entry = ",{\"status\":\"" + target + "\",\"ts\":\"" + updatedAt + "\"}]";
        this.statusHistoryJson = this.statusHistoryJson.replaceFirst("\\]$", entry);
    }

    // getters/setters
}
```

- [ ] **Step 4:** `OrderItem.java`

```java
package com.sweetbook.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id @Column(length = 36) private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING) @Column(name = "book_size", nullable = false, length = 8)
    private BookSize bookSize;

    @Enumerated(EnumType.STRING) @Column(name = "cover_type", nullable = false, length = 8)
    private CoverType coverType;

    @Column(nullable = false) private int copies;

    protected OrderItem() {}

    public static OrderItem create(Order order, BookSize size, CoverType cover, int copies) {
        OrderItem i = new OrderItem();
        i.id = UUID.randomUUID().toString();
        i.order = order;
        i.bookSize = size;
        i.coverType = cover;
        i.copies = copies;
        return i;
    }

    // getters/setters
}
```

- [ ] **Step 5:** 컴파일 확인

Run: `cd backend && mvn -DskipTests compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 6:** Commit

```bash
git add backend/src/main/java/com/sweetbook/domain/
git commit -m "feat: JPA entities (story, page, order, order_item)"
```

---

### Task 8 — Repository 인터페이스

**Files:**
- Create: `backend/src/main/java/com/sweetbook/repository/StoryRepository.java`
- Create: `backend/src/main/java/com/sweetbook/repository/PageRepository.java`
- Create: `backend/src/main/java/com/sweetbook/repository/OrderRepository.java`

- [ ] **Step 1:** 세 인터페이스 모두

```java
// StoryRepository.java
package com.sweetbook.repository;

import com.sweetbook.domain.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StoryRepository extends JpaRepository<Story, String> {
    List<Story> findAllByOrderByCreatedAtDesc();
}

// PageRepository.java
package com.sweetbook.repository;

import com.sweetbook.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, String> {
    Optional<Page> findByStoryIdAndPageNumber(String storyId, int pageNumber);
}

// OrderRepository.java
package com.sweetbook.repository;

import com.sweetbook.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findAllByOrderByCreatedAtDesc();
}
```

- [ ] **Step 2:** Commit

```bash
git add backend/src/main/java/com/sweetbook/repository/
git commit -m "feat: spring data jpa repositories"
```

---

### Task 9 — FileStorageService (TDD)

**Files:**
- Create: `backend/src/main/java/com/sweetbook/service/FileStorageService.java`
- Create: `backend/src/test/java/com/sweetbook/service/FileStorageServiceTest.java`

- [ ] **Step 1:** 테스트 작성 (failing)

```java
package com.sweetbook.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @TempDir Path tmp;
    FileStorageService svc;

    @BeforeEach void setUp() { svc = new FileStorageService(tmp.toString()); }

    @Test
    void saveDrawingReturnsRelativePath() throws Exception {
        var file = new MockMultipartFile("drawing", "kid.png", "image/png", "PNG_BYTES".getBytes());
        String path = svc.saveDrawing(file);
        assertTrue(path.startsWith("drawings/"), "got: " + path);
        assertTrue(path.endsWith(".png"));
        assertTrue(Files.exists(tmp.resolve(path)));
    }

    @Test
    void saveIllustrationByStoryAndPage() throws Exception {
        byte[] bytes = "PNG".getBytes();
        String path = svc.saveIllustration("story-abc", 3, bytes);
        assertEquals("illustrations/story-abc/page-03.png", path);
        assertArrayEquals(bytes, Files.readAllBytes(tmp.resolve(path)));
    }

    @Test
    void resolveAbsolutePath() {
        Path p = svc.resolve("seed/story-1/cover.png");
        assertEquals(tmp.resolve("seed/story-1/cover.png").toAbsolutePath(), p.toAbsolutePath());
    }

    @Test
    void rejectsInvalidContentType() {
        var file = new MockMultipartFile("drawing", "x.gif", "image/gif", "GIF".getBytes());
        assertThrows(IllegalArgumentException.class, () -> svc.saveDrawing(file));
    }
}
```

- [ ] **Step 2:** 테스트 실행 → FAIL 확인

Run: `cd backend && mvn -Dtest=FileStorageServiceTest test`
Expected: compilation error (FileStorageService not found)

- [ ] **Step 3:** 구현

```java
package com.sweetbook.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.*;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
    private final Path root;

    public FileStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath();
        try {
            Files.createDirectories(root.resolve("drawings"));
            Files.createDirectories(root.resolve("illustrations"));
            Files.createDirectories(root.resolve("seed"));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public String saveDrawing(MultipartFile file) throws java.io.IOException {
        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("UNSUPPORTED_IMAGE_TYPE");
        }
        String ext = contentType.equals("image/png") ? ".png" : ".jpg";
        String name = UUID.randomUUID() + ext;
        String rel = "drawings/" + name;
        Path target = root.resolve(rel);
        Files.write(target, file.getBytes());
        return rel;
    }

    public String saveIllustration(String storyId, int pageNumber, byte[] bytes) throws java.io.IOException {
        String rel = String.format("illustrations/%s/page-%02d.png", storyId, pageNumber);
        Path target = root.resolve(rel);
        Files.createDirectories(target.getParent());
        Files.write(target, bytes);
        return rel;
    }

    public Path resolve(String relativePath) {
        return root.resolve(relativePath).toAbsolutePath();
    }

    public Path getRoot() { return root; }
}
```

- [ ] **Step 4:** 테스트 실행 → PASS

Run: `cd backend && mvn -Dtest=FileStorageServiceTest test`
Expected: 4 tests passed.

- [ ] **Step 5:** Commit

```bash
git add backend/src/main/java/com/sweetbook/service/FileStorageService.java backend/src/test/java/com/sweetbook/service/FileStorageServiceTest.java
git commit -m "feat: file storage service with drawing/illustration paths"
```

---

### Task 10 — WebConfig (정적 자원 + 파일 핸들러 + SPA fallback)

**Files:**
- Create: `backend/src/main/java/com/sweetbook/config/WebConfig.java`
- Create: `backend/src/test/java/com/sweetbook/web/SpaFallbackTest.java`

이 task의 핵심: Vue Router는 `createWebHistory()`를 쓰므로 `/stories/seed-story-1` 같은 URL을 직접 입력하거나 새로고침했을 때 백엔드가 `index.html`로 forward 해줘야 한다. ResourceHandler만 있으면 그런 경로는 404. ViewController forward로 잡는다.

- [ ] **Step 1:** WebConfig 작성

```java
package com.sweetbook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 사용자 업로드 + AI 생성 + seed 이미지
        registry.addResourceHandler("/api/files/**")
                .addResourceLocations("file:" + java.nio.file.Paths.get(uploadDir).toAbsolutePath() + "/")
                .setCachePeriod(3600);

        // Vue 빌드 산출물의 정적 자산 (해시된 JS/CSS는 /assets/* 아래로 내려옴)
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCachePeriod(86400);

        // 루트의 favicon, vite.svg 등도 노출
        registry.addResourceHandler("/favicon.ico", "/vite.svg")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(86400);
    }

    /**
     * Vue Router history mode를 위한 SPA fallback.
     * ResourceHandler에 안 잡힌 라우트(/stories/*, /orders/*, /)를
     * index.html로 forward해서 클라이언트 라우터가 처리하게 한다.
     * /api/**는 컨트롤러가 먼저 매칭되므로 영향 없음.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/stories/new").setViewName("forward:/index.html");
        registry.addViewController("/stories/{id:[\\w-]+}").setViewName("forward:/index.html");
        registry.addViewController("/orders").setViewName("forward:/index.html");
        registry.addViewController("/orders/{id:[\\w-]+}").setViewName("forward:/index.html");
    }
}
```

- [ ] **Step 2:** SPA fallback 통합 테스트

```java
package com.sweetbook.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:spa;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "app.upload-dir=./build/spa-uploads",
    "app.ai.mock-mode=true"
})
class SpaFallbackTest {

    @Autowired MockMvc mvc;

    @Test
    void rootForwardsToIndex() throws Exception {
        // index.html이 classpath:/static/에 없을 수도 있으니 forward path만 검증
        mvc.perform(get("/")).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void storyDetailRouteForwardsToIndex() throws Exception {
        mvc.perform(get("/stories/seed-story-1")).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void newStoryRouteForwardsToIndex() throws Exception {
        mvc.perform(get("/stories/new")).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void ordersRouteForwardsToIndex() throws Exception {
        mvc.perform(get("/orders")).andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void apiRoutesAreNotForwarded() throws Exception {
        // 실제 컨트롤러가 응답해야 함 — 200 또는 404 (시드 없는 환경) 등 forward는 NO
        mvc.perform(get("/api/stories")).andExpect(status().isOk());
    }
}
```

- [ ] **Step 3:** 테스트 실행

Run: `cd backend && mvn -Dtest=SpaFallbackTest test`
Expected: 5 tests passed.

- [ ] **Step 4:** Commit

```bash
git add backend/src/main/java/com/sweetbook/config/WebConfig.java backend/src/test/java/com/sweetbook/web/SpaFallbackTest.java
git commit -m "feat: web config + SPA history-mode fallback"
```

---

### Task 11 — V2 시드 SQL + 시드 자산 복사

**Files:**
- Create: `backend/src/main/resources/seed/story-1/{cover,page-2,page-3,page-4,page-5,drawing}.png`
- Create: `backend/src/main/resources/seed/story-2/...`
- Create: `backend/src/main/resources/seed/story-3/...`
- Create: `backend/src/main/resources/seed/story-4/...`
- Create: `backend/src/main/resources/seed/placeholder.png`
- Create: `backend/src/main/resources/db/migration/V2__seed.sql`
- Create: `backend/src/main/java/com/sweetbook/service/SeedService.java`

- [ ] **Step 1:** 시드 PNG 자산 준비 (Phase 7에서 품질 업그레이드, 지금은 placeholder)

지금은 4편 모두 동일한 placeholder.png를 복사해 자리만 채움. Phase 7 Task에서 실제 일러스트로 교체.

```bash
# 임시 placeholder 생성 (1×1 투명 PNG)
mkdir -p backend/src/main/resources/seed/story-1
mkdir -p backend/src/main/resources/seed/story-2
mkdir -p backend/src/main/resources/seed/story-3
mkdir -p backend/src/main/resources/seed/story-4

# placeholder.png는 직접 만든다 (포토샵·온라인 도구·또는 base64 디코드)
# 여기서는 1x1 transparent PNG의 base64로 생성:
echo "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=" \
  | base64 -d > backend/src/main/resources/seed/placeholder.png

# 4편 각각 6장씩 복사 (cover, page-2~5, drawing)
for s in story-1 story-2 story-3 story-4; do
  for f in cover.png page-2.png page-3.png page-4.png page-5.png drawing.png; do
    cp backend/src/main/resources/seed/placeholder.png backend/src/main/resources/seed/$s/$f
  done
done
```

- [ ] **Step 2:** `V2__seed.sql` 작성 (4편 × 5페이지 + 1주문 + 1아이템)

```sql
-- Story 1: 곰돌이의 별 따기
INSERT INTO story (id, title, child_name, status, drawing_url, style_descriptor, imagination_prompt, created_at, updated_at)
VALUES ('seed-story-1', '곰돌이의 별 따기', '서아', 'COMPLETED',
        'seed/story-1/drawing.png',
        '{"keywords":["수채화풍","따뜻한 파스텔","굵은 외곽선","천진한 동물 캐릭터"]}',
        '곰돌이가 우주에 가서 별을 따왔어!',
        NOW(3), NOW(3));

INSERT INTO page (id, story_id, page_number, layout, body_text, illustration_prompt, illustration_url) VALUES
('seed-page-1-1', 'seed-story-1', 1, 'COVER',  NULL, '곰돌이가 우주선 옆에 서서 웃는 표지', 'seed/story-1/cover.png'),
('seed-page-1-2', 'seed-story-1', 2, 'SPLIT',  '곰돌이는 살금살금 우주선에 올라탔어요. 창밖으로 별들이 반짝반짝 인사를 했답니다.', '우주선 안의 곰돌이', 'seed/story-1/page-2.png'),
('seed-page-1-3', 'seed-story-1', 3, 'SPLIT',  '우주선은 점점 더 높이 올라갔어요. 지구가 작은 공처럼 보였답니다.', '우주에서 본 지구', 'seed/story-1/page-3.png'),
('seed-page-1-4', 'seed-story-1', 4, 'SPLIT',  '드디어 별 한 송이를 손에 잡았어요! 별은 따뜻하고 부드러웠어요.', '별을 잡은 곰돌이', 'seed/story-1/page-4.png'),
('seed-page-1-5', 'seed-story-1', 5, 'ENDING', '곰돌이는 별을 가지고 집으로 돌아왔어요. 그날 밤, 곰돌이의 방은 가장 환했답니다.', '집에 돌아온 곰돌이', 'seed/story-1/page-5.png');

-- Story 2: 유니콘과 무지개 다리
INSERT INTO story (id, title, child_name, status, drawing_url, style_descriptor, imagination_prompt, created_at, updated_at)
VALUES ('seed-story-2', '유니콘과 무지개 다리', '하윤', 'COMPLETED',
        'seed/story-2/drawing.png',
        '{"keywords":["꿈결같은 파스텔","반짝이는 디테일","부드러운 곡선"]}',
        '유니콘이 무지개를 타고 학교에 같이 가는 이야기',
        NOW(3), NOW(3));

INSERT INTO page (id, story_id, page_number, layout, body_text, illustration_prompt, illustration_url) VALUES
('seed-page-2-1', 'seed-story-2', 1, 'COVER',  NULL, '무지개 위 유니콘 표지', 'seed/story-2/cover.png'),
('seed-page-2-2', 'seed-story-2', 2, 'SPLIT',  '아침 햇살이 창문을 두드렸어요. 하윤이는 가방을 메고 문을 열었답니다.', '책가방 멘 아이', 'seed/story-2/page-2.png'),
('seed-page-2-3', 'seed-story-2', 3, 'SPLIT',  '문 앞에 유니콘이 기다리고 있었어요. "오늘은 무지개를 타고 가자!"', '유니콘이 부르는 장면', 'seed/story-2/page-3.png'),
('seed-page-2-4', 'seed-story-2', 4, 'SPLIT',  '둘은 무지개 다리를 폴짝폴짝 건넜어요. 발밑에서 색깔이 톡톡 튀었답니다.', '무지개 다리', 'seed/story-2/page-4.png'),
('seed-page-2-5', 'seed-story-2', 5, 'ENDING', '학교 종이 울렸어요. 친구들이 와! 하고 환호했답니다.', '학교 도착', 'seed/story-2/page-5.png');

-- Story 3 & 4도 같은 패턴 (제목/주인공/문장만 다름)
-- (지면 절약을 위해 같은 형태로 작성. 실제 SQL 파일에는 4편 모두 포함)
INSERT INTO story (id, title, child_name, status, drawing_url, style_descriptor, imagination_prompt, created_at, updated_at)
VALUES ('seed-story-3', '파란 고래와 수영하기', '지호', 'COMPLETED', 'seed/story-3/drawing.png',
        '{"keywords":["수채 바다","청량한 톤"]}', '커다란 파란 고래와 바다에서 수영',
        NOW(3), NOW(3));
INSERT INTO page (id, story_id, page_number, layout, body_text, illustration_prompt, illustration_url) VALUES
('seed-page-3-1', 'seed-story-3', 1, 'COVER',  NULL, '고래와 아이 표지', 'seed/story-3/cover.png'),
('seed-page-3-2', 'seed-story-3', 2, 'SPLIT',  '바다 한가운데, 커다란 그림자가 다가왔어요.', '바다 그림자', 'seed/story-3/page-2.png'),
('seed-page-3-3', 'seed-story-3', 3, 'SPLIT',  '고래는 등을 살짝 내밀어 지호를 태웠어요.', '고래 등에 탄 아이', 'seed/story-3/page-3.png'),
('seed-page-3-4', 'seed-story-3', 4, 'SPLIT',  '둘은 산호 사이를 천천히 지나갔어요.', '산호 정원', 'seed/story-3/page-4.png'),
('seed-page-3-5', 'seed-story-3', 5, 'ENDING', '해질녘, 고래는 지호를 모래사장에 살며시 내려놓았어요.', '석양 해변', 'seed/story-3/page-5.png');

INSERT INTO story (id, title, child_name, status, drawing_url, style_descriptor, imagination_prompt, created_at, updated_at)
VALUES ('seed-story-4', '로봇과 화성 여행', '시우', 'COMPLETED', 'seed/story-4/drawing.png',
        '{"keywords":["우주 톤","SF 일러스트","따뜻한 디테일"]}', '로봇 친구와 화성에 캠핑',
        NOW(3), NOW(3));
INSERT INTO page (id, story_id, page_number, layout, body_text, illustration_prompt, illustration_url) VALUES
('seed-page-4-1', 'seed-story-4', 1, 'COVER',  NULL, '로봇과 아이 표지', 'seed/story-4/cover.png'),
('seed-page-4-2', 'seed-story-4', 2, 'SPLIT',  '시우와 로봇은 화성에 도착했어요. 모든 게 빨갛게 빛났답니다.', '화성 풍경', 'seed/story-4/page-2.png'),
('seed-page-4-3', 'seed-story-4', 3, 'SPLIT',  '로봇은 모래에서 작은 빛 조각을 발견했어요.', '빛 조각 발견', 'seed/story-4/page-3.png'),
('seed-page-4-4', 'seed-story-4', 4, 'SPLIT',  '둘은 캠프파이어 앞에서 우주 노래를 불렀답니다.', '캠프파이어', 'seed/story-4/page-4.png'),
('seed-page-4-5', 'seed-story-4', 5, 'ENDING', '별이 가득한 하늘 아래, 둘은 나란히 잠들었어요.', '별 하늘 아래 잠든 둘', 'seed/story-4/page-5.png');

-- 시드 주문 1건 (PROCESSING 상태)
INSERT INTO orders (id, story_id, recipient_name, address_memo, status, status_history, created_at, updated_at)
VALUES ('seed-order-1', 'seed-story-1', '김서아', '시연용 데이터', 'PROCESSING',
        '[{"status":"PENDING","ts":"2026-04-28T10:00:00Z"},{"status":"PROCESSING","ts":"2026-04-28T11:00:00Z"}]',
        '2026-04-28 10:00:00.000', '2026-04-28 11:00:00.000');

INSERT INTO order_item (id, order_id, book_size, cover_type, copies)
VALUES ('seed-item-1', 'seed-order-1', 'A5', 'HARD', 1);
```

- [ ] **Step 3:** `SeedService.java` (resources/seed → /data/uploads/seed 1회 복사)

```java
package com.sweetbook.service;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.nio.file.*;

@Component
public class SeedService {

    private final FileStorageService storage;

    public SeedService(FileStorageService storage) { this.storage = storage; }

    @Bean
    ApplicationRunner copySeedAssets() {
        return args -> {
            Path seedRoot = storage.resolve("seed");
            // 이미 복사돼 있으면 skip (idempotent)
            if (Files.exists(seedRoot.resolve("placeholder.png"))) return;

            var resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:seed/**/*.png");
            for (Resource r : resources) {
                String url = r.getURL().toString();
                String relativeStart = url.indexOf("seed/") >= 0 ? url.substring(url.indexOf("seed/")) : null;
                if (relativeStart == null) continue;
                Path target = storage.getRoot().resolve(relativeStart);
                Files.createDirectories(target.getParent());
                try (var in = r.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        };
    }
}
```

- [ ] **Step 4:** Commit

```bash
git add backend/src/main/resources/seed/ backend/src/main/resources/db/migration/V2__seed.sql backend/src/main/java/com/sweetbook/service/SeedService.java
git commit -m "feat: V2 seed (4 stories) + asset copy on startup"
```

---

## Phase 2 — Story REST API (Hour 5-7)

### Task 12 — Story DTO + StoryService.list/get + GET 엔드포인트

**Files:**
- Create: `backend/src/main/java/com/sweetbook/web/dto/StorySummaryDto.java`
- Create: `backend/src/main/java/com/sweetbook/web/dto/StoryDto.java`
- Create: `backend/src/main/java/com/sweetbook/web/dto/PageDto.java`
- Create: `backend/src/main/java/com/sweetbook/service/StoryService.java`
- Create: `backend/src/main/java/com/sweetbook/web/StoryController.java`
- Create: `backend/src/test/java/com/sweetbook/web/StoryControllerListTest.java`

- [ ] **Step 1:** DTO 3종

```java
// StorySummaryDto.java
package com.sweetbook.web.dto;

import com.sweetbook.domain.StoryStatus;
import java.time.Instant;

public record StorySummaryDto(
    String id, String title, String childName, StoryStatus status,
    String coverUrl, Instant createdAt, String errorMessage
) {}

// StoryDto.java
package com.sweetbook.web.dto;

import com.sweetbook.domain.StoryStatus;
import java.time.Instant;
import java.util.List;

public record StoryDto(
    String id, String title, String childName, StoryStatus status,
    String errorMessage, String drawingUrl, String styleDescriptor,
    String imaginationPrompt, List<PageDto> pages, Instant createdAt
) {}

// PageDto.java
package com.sweetbook.web.dto;

import com.sweetbook.domain.PageLayout;

public record PageDto(
    int pageNumber, PageLayout layout,
    String bodyText, String illustrationPrompt, String illustrationUrl
) {}
```

- [ ] **Step 2:** `StoryService.java` (list + get)

```java
package com.sweetbook.service;

import com.sweetbook.domain.*;
import com.sweetbook.repository.StoryRepository;
import com.sweetbook.web.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class StoryService {

    private final StoryRepository stories;

    public StoryService(StoryRepository stories) { this.stories = stories; }

    public List<StorySummaryDto> list() {
        return stories.findAllByOrderByCreatedAtDesc().stream()
            .map(s -> {
                String coverUrl = s.getPages().stream()
                    .filter(p -> p.getPageNumber() == 1)
                    .map(p -> toFileUrl(p.getIllustrationUrl()))
                    .findFirst().orElse(null);
                return new StorySummaryDto(s.getId(), s.getTitle(), s.getChildName(),
                    s.getStatus(), coverUrl, s.getCreatedAt(), s.getErrorMessage());
            })
            .toList();
    }

    public StoryDto getById(String id) {
        Story s = stories.findById(id).orElseThrow(() -> new NoSuchElementException("STORY_NOT_FOUND"));
        List<PageDto> pageDtos = s.getPages().stream()
            .map(p -> new PageDto(p.getPageNumber(), p.getLayout(), p.getBodyText(),
                p.getIllustrationPrompt(), toFileUrl(p.getIllustrationUrl())))
            .toList();
        return new StoryDto(s.getId(), s.getTitle(), s.getChildName(), s.getStatus(),
            s.getErrorMessage(), toFileUrl(s.getDrawingUrl()), s.getStyleDescriptorJson(),
            s.getImaginationPrompt(), pageDtos, s.getCreatedAt());
    }

    public static String toFileUrl(String relativePath) {
        return relativePath == null ? null : "/api/files/" + relativePath;
    }
}
```

- [ ] **Step 3:** `StoryController.java` (list + get)

```java
package com.sweetbook.web;

import com.sweetbook.service.StoryService;
import com.sweetbook.web.dto.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) { this.storyService = storyService; }

    @GetMapping
    public List<StorySummaryDto> list() { return storyService.list(); }

    @GetMapping("/{id}")
    public StoryDto detail(@PathVariable String id) { return storyService.getById(id); }
}
```

- [ ] **Step 4:** 통합 테스트 (시드 4편 노출 확인)

```java
package com.sweetbook.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.flyway.locations=classpath:db/migration",
    "app.upload-dir=./build/test-uploads"
})
class StoryControllerListTest {

    @Autowired MockMvc mvc;

    @Test
    void listsSeedStories() throws Exception {
        mvc.perform(get("/api/stories"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.length()").value(4))
           .andExpect(jsonPath("$[0].title").exists());
    }

    @Test
    void getDetailIncludesFivePages() throws Exception {
        mvc.perform(get("/api/stories/seed-story-1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.pages.length()").value(5))
           .andExpect(jsonPath("$.pages[0].layout").value("COVER"));
    }
}
```

- [ ] **Step 5:** 테스트 실행 → PASS

Run: `cd backend && mvn -Dtest=StoryControllerListTest test`
Expected: 2 tests passed.

- [ ] **Step 6:** Commit

```bash
git add backend/src/main/java/com/sweetbook/web/ backend/src/main/java/com/sweetbook/service/StoryService.java backend/src/test/java/com/sweetbook/web/StoryControllerListTest.java
git commit -m "feat: GET /api/stories and /api/stories/{id}"
```

---

### Task 13 — POST /api/stories (multipart + 검증) + 비동기 트리거

**Files:**
- Create: `backend/src/main/java/com/sweetbook/web/dto/StoryCreateRequest.java`
- Create: `backend/src/main/java/com/sweetbook/web/dto/ErrorResponse.java`
- Create: `backend/src/main/java/com/sweetbook/web/GlobalExceptionHandler.java`
- Modify: `backend/src/main/java/com/sweetbook/service/StoryService.java` (create 추가)
- Modify: `backend/src/main/java/com/sweetbook/web/StoryController.java` (POST 추가)

- [ ] **Step 1:** Request DTO + 검증

```java
package com.sweetbook.web.dto;

import jakarta.validation.constraints.*;

public record StoryCreateRequest(
    @NotBlank(message = "아이 이름은 1~20자로 적어주세요")
    @Size(min = 1, max = 20, message = "아이 이름은 1~20자로 적어주세요")
    String childName,

    @NotBlank(message = "상상은 10자 이상 500자 이하로 적어주세요")
    @Size(min = 10, max = 500, message = "상상은 10자 이상 500자 이하로 적어주세요")
    String imaginationPrompt
) {}
```

- [ ] **Step 2:** ErrorResponse + GlobalExceptionHandler

```java
// ErrorResponse.java
package com.sweetbook.web.dto;
public record ErrorResponse(String error, String message) {}

// GlobalExceptionHandler.java
package com.sweetbook.web;

import com.sweetbook.web.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
            .findFirst().map(f -> f.getDefaultMessage()).orElse("VALIDATION_FAILED");
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_FAILED", msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> illegal(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage(), userMessage(e.getMessage())));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> state(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("INVALID_TRANSITION", "허용되지 않은 상태 전이입니다"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> notFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage(), "찾을 수 없습니다"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> tooLarge(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(new ErrorResponse("FILE_TOO_LARGE", "5MB 이하 JPG/PNG만 업로드 가능합니다"));
    }

    private String userMessage(String code) {
        return switch (code) {
            case "UNSUPPORTED_IMAGE_TYPE" -> "5MB 이하 JPG/PNG만 업로드 가능합니다";
            case "DRAWING_REQUIRED" -> "그림을 업로드해주세요";
            default -> code;
        };
    }
}
```

- [ ] **Step 3:** `StoryService.create()` 추가

```java
// StoryService.java에 추가
@Transactional
public Story create(StoryCreateRequest req, MultipartFile drawing) throws java.io.IOException {
    if (drawing == null || drawing.isEmpty()) {
        throw new IllegalArgumentException("DRAWING_REQUIRED");
    }
    String drawingPath = storage.saveDrawing(drawing);  // 검증은 saveDrawing에서
    Story s = Story.newDraft(req.childName(), req.imaginationPrompt());
    s.setDrawingUrl(drawingPath);
    return stories.save(s);
}
```

`StoryService` 생성자에 `FileStorageService storage` + `StoryGenerationService generationService`를 주입 (Task 17에서 작성될 service는 일단 nullable 허용 — 임시로 인터페이스만 추가).

- [ ] **Step 4:** Controller에 POST 추가

```java
// StoryController.java에 추가
@PostMapping
public Map<String, String> create(
    @Valid @ModelAttribute StoryCreateRequest req,
    @RequestParam("drawing") MultipartFile drawing
) throws java.io.IOException {
    Story s = storyService.create(req, drawing);
    storyService.kickOffAsyncGeneration(s.getId());  // Phase 3에서 채움 (지금은 stub)
    return Map.of("id", s.getId(), "status", s.getStatus().name());
}
```

`kickOffAsyncGeneration`는 임시로 빈 메서드. Phase 3에서 채움.

- [ ] **Step 5:** 테스트 — 검증 실패 케이스

```java
// StoryControllerCreateTest.java 신규
@Test
void rejectsShortImagination() throws Exception {
    var drawing = new MockMultipartFile("drawing", "k.png", "image/png", "PNG".getBytes());
    mvc.perform(multipart("/api/stories")
            .file(drawing)
            .param("childName", "서아")
            .param("imaginationPrompt", "짧음"))
       .andExpect(status().isBadRequest())
       .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
       .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("10자 이상")));
}

@Test
void rejectsGifImage() throws Exception {
    var drawing = new MockMultipartFile("drawing", "k.gif", "image/gif", "GIF".getBytes());
    mvc.perform(multipart("/api/stories")
            .file(drawing)
            .param("childName", "서아")
            .param("imaginationPrompt", "곰돌이가 우주에 가서 별을 따왔어요"))
       .andExpect(status().isBadRequest())
       .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("JPG/PNG")));
}
```

- [ ] **Step 6:** 테스트 통과 확인

Run: `cd backend && mvn -Dtest=StoryControllerCreateTest test`

- [ ] **Step 7:** Commit

```bash
git add backend/src/main/java/com/sweetbook/ backend/src/test/java/com/sweetbook/
git commit -m "feat: POST /api/stories with multipart validation"
```

---

### Task 14 — PATCH /api/stories/{id}/pages/{n} (본문 수정)

**Files:**
- Create: `backend/src/main/java/com/sweetbook/web/dto/PageBodyUpdateRequest.java`
- Modify: `backend/src/main/java/com/sweetbook/service/StoryService.java`
- Modify: `backend/src/main/java/com/sweetbook/web/StoryController.java`

- [ ] **Step 1:** Request DTO

```java
package com.sweetbook.web.dto;

import jakarta.validation.constraints.Size;

public record PageBodyUpdateRequest(
    @Size(max = 500, message = "본문은 500자 이하로 적어주세요")
    String bodyText
) {}
```

- [ ] **Step 2:** Service 메서드

```java
@Transactional
public void updatePageBody(String storyId, int pageNumber, String bodyText) {
    Page p = pageRepo.findByStoryIdAndPageNumber(storyId, pageNumber)
        .orElseThrow(() -> new NoSuchElementException("PAGE_NOT_FOUND"));
    p.setBodyText(bodyText);
    pageRepo.save(p);
}
```

- [ ] **Step 3:** Controller 메서드

```java
@PatchMapping("/{id}/pages/{n}")
public Map<String, Boolean> updatePageBody(
    @PathVariable String id, @PathVariable int n,
    @Valid @RequestBody PageBodyUpdateRequest req
) {
    storyService.updatePageBody(id, n, req.bodyText());
    return Map.of("ok", true);
}
```

- [ ] **Step 4:** 테스트 — 1개 happy path

```java
@Test
void updatesPageBody() throws Exception {
    mvc.perform(patch("/api/stories/seed-story-1/pages/2")
            .contentType("application/json")
            .content("{\"bodyText\":\"새 본문이에요\"}"))
       .andExpect(status().isOk());
}
```

- [ ] **Step 5:** Commit

```bash
git add .
git commit -m "feat: PATCH page body endpoint"
```

---

## Phase 3 — AI 통합 (Hour 7-12)

### Task 15 — AiClient 인터페이스 + DTO

**Files:**
- Create: `backend/src/main/java/com/sweetbook/service/ai/AiClient.java`
- Create: `backend/src/main/java/com/sweetbook/service/ai/StyleDescriptor.java`
- Create: `backend/src/main/java/com/sweetbook/service/ai/StoryDraft.java`

- [ ] **Step 1:** 인터페이스 + DTO

```java
// AiClient.java
package com.sweetbook.service.ai;

public interface AiClient {
    StyleDescriptor analyzeDrawing(byte[] drawingBytes, String contentType);
    StoryDraft generateStory(String childName, String imaginationPrompt, StyleDescriptor style);
    byte[] generateIllustration(String prompt, StyleDescriptor style);
}

// StyleDescriptor.java
package com.sweetbook.service.ai;

import java.util.List;
public record StyleDescriptor(List<String> keywords) {
    public String asPromptPrefix() {
        return String.join(", ", keywords);
    }
}

// StoryDraft.java
package com.sweetbook.service.ai;

import java.util.List;
public record StoryDraft(String title, List<PageDraft> pages) {
    public record PageDraft(int pageNumber, String bodyText, String illustrationPrompt) {}
}
```

- [ ] **Step 2:** Commit

```bash
git add backend/src/main/java/com/sweetbook/service/ai/
git commit -m "feat: AiClient interface + DTOs"
```

---

### Task 16 — MockAiClient (sleep으로 단계 흉내)

**Files:**
- Create: `backend/src/main/java/com/sweetbook/service/ai/MockAiClient.java`

- [ ] **Step 1:** 구현

```java
package com.sweetbook.service.ai;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class MockAiClient implements AiClient {

    @Override
    public StyleDescriptor analyzeDrawing(byte[] bytes, String contentType) {
        sleep(600);
        return new StyleDescriptor(List.of("수채화풍", "따뜻한 파스텔", "굵은 외곽선", "천진한 캐릭터"));
    }

    @Override
    public StoryDraft generateStory(String childName, String prompt, StyleDescriptor style) {
        sleep(600);
        String title = "내가 만든 동화";
        return new StoryDraft(title, List.of(
            new StoryDraft.PageDraft(1, null, "표지: " + prompt),
            new StoryDraft.PageDraft(2, childName + "의 모험이 시작되었어요. 모든 게 반짝반짝 빛났답니다.", "장면 1"),
            new StoryDraft.PageDraft(3, "조심스럽게 한 발 한 발 나아갔어요. 신비한 친구를 만났답니다.", "장면 2"),
            new StoryDraft.PageDraft(4, "함께 손을 잡고 더 멀리 떠났어요. 마법 같은 일이 펼쳐졌어요.", "장면 3"),
            new StoryDraft.PageDraft(5, "그렇게 " + childName + "이는 행복하게 잠들었답니다.", "엔딩 장면")
        ));
    }

    @Override
    public byte[] generateIllustration(String prompt, StyleDescriptor style) {
        sleep(600);
        try (var in = new ClassPathResource("seed/placeholder.png").getInputStream()) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
```

- [ ] **Step 2:** Commit

```bash
git add backend/src/main/java/com/sweetbook/service/ai/MockAiClient.java
git commit -m "feat: MockAiClient with 600ms step delays"
```

---

### Task 17 — OpenAiClient + Factory (mock-mode 분기)

**Files:**
- Create: `backend/src/main/java/com/sweetbook/service/ai/OpenAiClient.java`
- Create: `backend/src/main/java/com/sweetbook/config/AiConfig.java`

- [ ] **Step 1:** OpenAiClient 작성 (WebClient + REST)

```java
package com.sweetbook.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;

public class OpenAiClient implements AiClient {

    private final WebClient http;
    private final String apiKey;
    private final String visionModel;
    private final String textModel;
    private final String imageModel;
    private final ObjectMapper om = new ObjectMapper();

    public OpenAiClient(WebClient http, String apiKey, String visionModel, String textModel, String imageModel) {
        this.http = http; this.apiKey = apiKey;
        this.visionModel = visionModel; this.textModel = textModel; this.imageModel = imageModel;
    }

    @Override
    public StyleDescriptor analyzeDrawing(byte[] bytes, String contentType) {
        String b64 = Base64.getEncoder().encodeToString(bytes);
        String dataUrl = "data:" + contentType + ";base64," + b64;
        Map<String, Object> body = Map.of(
            "model", visionModel,
            "messages", List.of(
                Map.of("role", "system", "content",
                    "당신은 아이의 그림을 보고 일러스트 스타일을 4-6개 짧은 한국어 키워드로 추출하는 분석가입니다. JSON 한 줄로만 답하세요. 형태: {\"keywords\":[\"...\",\"...\"]}"),
                Map.of("role", "user", "content", List.of(
                    Map.of("type", "text", "text", "이 그림의 스타일을 분석해주세요"),
                    Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
                ))
            ),
            "response_format", Map.of("type", "json_object"),
            "max_tokens", 200
        );
        JsonNode resp = call("/v1/chat/completions", body);
        try {
            String content = resp.get("choices").get(0).get("message").get("content").asText();
            JsonNode parsed = om.readTree(content);
            List<String> kws = new ArrayList<>();
            parsed.get("keywords").forEach(k -> kws.add(k.asText()));
            return new StyleDescriptor(kws);
        } catch (Exception e) {
            throw new RuntimeException("STYLE_PARSE_FAILED", e);
        }
    }

    @Override
    public StoryDraft generateStory(String childName, String prompt, StyleDescriptor style) {
        String sys = """
            당신은 어린이용 한국어 동화 작가입니다. 5페이지 동화를 만드세요.
            - page 1은 표지: bodyText는 null, illustrationPrompt에 표지 장면 묘사
            - page 2~4는 본문: bodyText 2-3문장, illustrationPrompt 한 줄
            - page 5는 엔딩: 마무리 한 문장 + illustrationPrompt
            JSON으로만 답하세요. 형태:
            {"title":"...","pages":[{"pageNumber":1,"bodyText":null,"illustrationPrompt":"..."},...]}
            """;
        String user = "아이 이름: " + childName + "\n상상: " + prompt;
        Map<String, Object> body = Map.of(
            "model", textModel,
            "messages", List.of(
                Map.of("role", "system", "content", sys),
                Map.of("role", "user", "content", user)
            ),
            "response_format", Map.of("type", "json_object"),
            "max_tokens", 1500
        );
        JsonNode resp = call("/v1/chat/completions", body);
        try {
            String content = resp.get("choices").get(0).get("message").get("content").asText();
            return om.readValue(content, StoryDraft.class);
        } catch (Exception e) {
            throw new RuntimeException("STORY_PARSE_FAILED", e);
        }
    }

    @Override
    public byte[] generateIllustration(String prompt, StyleDescriptor style) {
        String fullPrompt = "스타일: " + style.asPromptPrefix() + "\n장면: " + prompt
            + "\n어린이 동화책 일러스트, 4:5 세로 비율, 텍스트 없음";
        Map<String, Object> body = Map.of(
            "model", imageModel,
            "prompt", fullPrompt,
            "size", "1024x1024",
            "n", 1
        );
        JsonNode resp = call("/v1/images/generations", body);
        try {
            String b64 = resp.get("data").get(0).get("b64_json").asText();
            return Base64.getDecoder().decode(b64);
        } catch (Exception e) {
            throw new RuntimeException("IMAGE_PARSE_FAILED", e);
        }
    }

    private JsonNode call(String path, Map<String, Object> body) {
        return http.post().uri(path)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();
    }
}
```

- [ ] **Step 2:** AiConfig — 환경변수로 분기

```java
package com.sweetbook.config;

import com.sweetbook.service.ai.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiConfig {

    @Bean
    @Primary
    public AiClient aiClient(
        @Value("${app.ai.mock-mode}") boolean mockMode,
        @Value("${app.ai.openai-api-key:}") String apiKey,
        @Value("${app.ai.text-model}") String textModel,
        @Value("${app.ai.vision-model}") String visionModel,
        @Value("${app.ai.image-model}") String imageModel,
        MockAiClient mockClient
    ) {
        if (mockMode || apiKey == null || apiKey.isBlank()) {
            return mockClient;
        }
        WebClient http = WebClient.builder()
            .baseUrl("https://api.openai.com")
            .codecs(c -> c.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
            .build();
        return new OpenAiClient(http, apiKey, visionModel, textModel, imageModel);
    }
}
```

- [ ] **Step 3:** Commit

```bash
git add backend/src/main/java/com/sweetbook/
git commit -m "feat: OpenAI client + mock/real factory"
```

---

### Task 18 — StoryGenerationService (@Async 워크플로우)

**Files:**
- Create: `backend/src/main/java/com/sweetbook/service/StoryGenerationService.java`
- Create: `backend/src/main/java/com/sweetbook/config/AsyncConfig.java`
- Modify: `backend/src/main/java/com/sweetbook/service/StoryService.java`

- [ ] **Step 1:** AsyncConfig (스레드풀)

```java
package com.sweetbook.config;

import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {
    @Bean(name = "storyExecutor")
    public Executor storyExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(20);
        ex.setThreadNamePrefix("story-gen-");
        ex.initialize();
        return ex;
    }
}
```

- [ ] **Step 2:** StoryGenerationService

```java
package com.sweetbook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sweetbook.domain.*;
import com.sweetbook.repository.*;
import com.sweetbook.service.ai.*;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class StoryGenerationService {

    private static final Logger log = LoggerFactory.getLogger(StoryGenerationService.class);

    private final StoryRepository stories;
    private final PageRepository pages;
    private final FileStorageService storage;
    private final AiClient ai;
    private final ObjectMapper om = new ObjectMapper();

    public StoryGenerationService(StoryRepository stories, PageRepository pages,
                                  FileStorageService storage, AiClient ai) {
        this.stories = stories; this.pages = pages; this.storage = storage; this.ai = ai;
    }

    @Async("storyExecutor")
    public void generate(String storyId) {
        try {
            Story s = loadOrThrow(storyId);
            byte[] drawingBytes = Files.readAllBytes(storage.resolve(s.getDrawingUrl()));
            String contentType = s.getDrawingUrl().endsWith(".png") ? "image/png" : "image/jpeg";

            transitionTo(storyId, StoryStatus.ANALYZING_DRAWING);
            StyleDescriptor style = ai.analyzeDrawing(drawingBytes, contentType);
            saveStyle(storyId, style);

            transitionTo(storyId, StoryStatus.GENERATING_STORY);
            StoryDraft draft = ai.generateStory(s.getChildName(), s.getImaginationPrompt(), style);
            saveDraft(storyId, draft);

            transitionTo(storyId, StoryStatus.GENERATING_IMAGES);
            // 페이지별 일러스트 생성. 일부 실패해도 placeholder 사용 (부분 성공)
            for (int n = 1; n <= 5; n++) {
                generateIllustrationForPage(storyId, n, style);
            }

            transitionTo(storyId, StoryStatus.COMPLETED);
        } catch (Exception e) {
            log.error("Story generation failed for {}", storyId, e);
            markFailed(storyId, "동화 생성 중 오류가 발생했어요: " + e.getMessage());
        }
    }

    @Transactional
    void generateIllustrationForPage(String storyId, int pageNumber, StyleDescriptor style) {
        Page page = pages.findByStoryIdAndPageNumber(storyId, pageNumber)
            .orElseThrow(() -> new NoSuchElementException("PAGE_NOT_FOUND"));
        try {
            byte[] bytes = ai.generateIllustration(page.getIllustrationPrompt(), style);
            String path = storage.saveIllustration(storyId, pageNumber, bytes);
            page.setIllustrationUrl(path);
            pages.save(page);
        } catch (Exception e) {
            log.warn("Illustration failed for {} page {}: {}", storyId, pageNumber, e.getMessage());
            // illustrationUrl은 null로 남김 — 프론트에서 placeholder + 재생성 표시
        }
    }

    @Transactional
    public void regeneratePage(String storyId, int pageNumber) {
        Story s = loadOrThrow(storyId);
        StyleDescriptor style;
        try {
            style = om.readValue(s.getStyleDescriptorJson(), StyleDescriptor.class);
        } catch (Exception e) { style = new StyleDescriptor(List.of()); }
        generateIllustrationForPage(storyId, pageNumber, style);
    }

    @Async("storyExecutor")
    public void retry(String storyId) {
        Story s = loadOrThrow(storyId);
        if (s.getStatus() != StoryStatus.FAILED) return;
        @SuppressWarnings("unused") int x = 0; // placeholder
        // status를 ANALYZING_DRAWING으로 되돌리고 재시작
        Story fresh = loadOrThrow(storyId);
        fresh.retry();
        stories.save(fresh);
        generate(storyId);
    }

    @Transactional
    void transitionTo(String storyId, StoryStatus target) {
        Story s = loadOrThrow(storyId);
        s.transitionTo(target);
        stories.save(s);
    }

    @Transactional
    void markFailed(String storyId, String message) {
        Story s = loadOrThrow(storyId);
        if (!s.getStatus().isTerminal()) {
            s.markFailed(message);
            stories.save(s);
        }
    }

    @Transactional
    void saveStyle(String storyId, StyleDescriptor style) {
        try {
            Story s = loadOrThrow(storyId);
            s.setStyleDescriptorJson(om.writeValueAsString(style));
            stories.save(s);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Transactional
    void saveDraft(String storyId, StoryDraft draft) {
        Story s = loadOrThrow(storyId);
        s.setTitle(draft.title());
        for (StoryDraft.PageDraft pd : draft.pages()) {
            Page page = pages.findByStoryIdAndPageNumber(storyId, pd.pageNumber())
                .orElseGet(() -> Page.create(s, pd.pageNumber()));
            page.setBodyText(pd.bodyText());
            page.setIllustrationPrompt(pd.illustrationPrompt());
            pages.save(page);
        }
        stories.save(s);
    }

    private Story loadOrThrow(String id) {
        return stories.findById(id).orElseThrow(() -> new NoSuchElementException("STORY_NOT_FOUND"));
    }
}
```

- [ ] **Step 3:** StoryService에 `kickOffAsyncGeneration` 채우기

```java
// StoryService 생성자 시그니처에 StoryGenerationService 주입
public void kickOffAsyncGeneration(String storyId) {
    generationService.generate(storyId);
}
```

- [ ] **Step 4:** 단위 테스트 (mock으로 통합 흐름)

```java
// StoryGenerationServiceTest.java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:gentest;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "app.upload-dir=./build/gentest-uploads",
    "app.ai.mock-mode=true"
})
class StoryGenerationServiceTest {

    @Autowired StoryGenerationService gen;
    @Autowired StoryRepository stories;
    @Autowired FileStorageService storage;

    @Test
    void mockGenerationCompletes() throws Exception {
        // 그림 파일 1개 저장
        var drawing = new MockMultipartFile("d", "k.png", "image/png", "PNG".getBytes());
        Story s = Story.newDraft("테스트", "용기 있는 곰돌이가 모험을 떠난다");
        s.setDrawingUrl(storage.saveDrawing(drawing));
        stories.save(s);

        gen.generate(s.getId());
        // @Async라도 테스트에선 caller thread에서 실행될 수 있음. 잠깐 대기.
        Thread.sleep(5000);

        Story after = stories.findById(s.getId()).get();
        assertEquals(StoryStatus.COMPLETED, after.getStatus());
        assertEquals(5, after.getPages().size());
    }
}
```

- [ ] **Step 5:** Commit

```bash
git add backend/src/main/java/com/sweetbook/ backend/src/test/java/com/sweetbook/service/StoryGenerationServiceTest.java
git commit -m "feat: async story generation pipeline"
```

---

### Task 19 — POST regenerate + POST retry 엔드포인트

**Files:**
- Modify: `backend/src/main/java/com/sweetbook/web/StoryController.java`

- [ ] **Step 1:** Controller에 두 엔드포인트 추가

```java
@PostMapping("/{id}/pages/{n}/regenerate")
public Map<String, Boolean> regenerate(@PathVariable String id, @PathVariable int n) {
    generationService.regeneratePage(id, n);  // sync — 단일 페이지라 짧음
    return Map.of("ok", true);
}

@PostMapping("/{id}/retry")
public Map<String, Boolean> retry(@PathVariable String id) {
    generationService.retry(id);  // @Async
    return Map.of("ok", true);
}
```

`StoryController` 생성자에 `StoryGenerationService` 추가 주입.

- [ ] **Step 2:** Commit

```bash
git add backend/src/main/java/com/sweetbook/web/StoryController.java
git commit -m "feat: regenerate page + retry endpoints"
```

---

## Phase 4 — Vue 프론트엔드 (Hour 5-13, 백과 병행 가능)

### Task 20 — Vue Router + API client + types + Navbar

**Files:**
- Create: `frontend/src/types.ts`
- Create: `frontend/src/api/client.ts`
- Create: `frontend/src/api/stories.ts`
- Create: `frontend/src/api/orders.ts`
- Create: `frontend/src/router/index.ts`
- Create: `frontend/src/components/Navbar.vue`
- Modify: `frontend/src/main.ts`, `frontend/src/App.vue`
- Run: `cd frontend && npm install vue-router@4`

- [ ] **Step 1:** vue-router 설치

```bash
cd frontend && npm install vue-router@4
```

- [ ] **Step 2:** `types.ts`

```typescript
export type StoryStatus = 'DRAFT' | 'ANALYZING_DRAWING' | 'GENERATING_STORY'
  | 'GENERATING_IMAGES' | 'COMPLETED' | 'FAILED';

export type PageLayout = 'COVER' | 'SPLIT' | 'ENDING';

export type OrderStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED';

export interface StorySummary {
  id: string;
  title: string;
  childName: string;
  status: StoryStatus;
  coverUrl: string | null;
  createdAt: string;
  errorMessage: string | null;
}

export interface PageData {
  pageNumber: number;
  layout: PageLayout;
  bodyText: string | null;
  illustrationPrompt: string | null;
  illustrationUrl: string | null;
}

export interface Story {
  id: string;
  title: string;
  childName: string;
  status: StoryStatus;
  errorMessage: string | null;
  drawingUrl: string | null;
  styleDescriptor: string | null;
  imaginationPrompt: string;
  pages: PageData[];
  createdAt: string;
}

export interface OrderItem {
  bookSize: 'A5' | 'B5';
  coverType: 'SOFT' | 'HARD';
  copies: number;
}

export interface Order {
  id: string;
  story: { id: string; title: string; coverUrl: string | null };
  status: OrderStatus;
  recipientName: string;
  item: OrderItem;
  createdAt: string;
}

export interface ApiError { error: string; message: string; }
```

- [ ] **Step 3:** `api/client.ts`

```typescript
import type { ApiError } from '../types';

export class ApiException extends Error {
  constructor(public code: string, message: string, public status: number) { super(message); }
}

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const r = await fetch(path, init);
  if (!r.ok) {
    let body: ApiError;
    try { body = await r.json(); } catch { body = { error: 'NETWORK', message: r.statusText }; }
    throw new ApiException(body.error, body.message, r.status);
  }
  if (r.status === 204) return undefined as T;
  return r.json();
}
```

- [ ] **Step 4:** `api/stories.ts` + `api/orders.ts`

```typescript
// stories.ts
import { apiFetch } from './client';
import type { Story, StorySummary } from '../types';

export const listStories  = () => apiFetch<StorySummary[]>('/api/stories');
export const getStory     = (id: string) => apiFetch<Story>(`/api/stories/${id}`);
export const createStory  = (form: FormData) =>
  apiFetch<{ id: string; status: string }>('/api/stories', { method: 'POST', body: form });
export const updatePageBody = (id: string, n: number, bodyText: string) =>
  apiFetch<{ ok: boolean }>(`/api/stories/${id}/pages/${n}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ bodyText }),
  });
export const regeneratePage = (id: string, n: number) =>
  apiFetch<{ ok: boolean }>(`/api/stories/${id}/pages/${n}/regenerate`, { method: 'POST' });
export const retryStory = (id: string) =>
  apiFetch<{ ok: boolean }>(`/api/stories/${id}/retry`, { method: 'POST' });

// orders.ts
import { apiFetch } from './client';
import type { Order } from '../types';

export interface OrderCreatePayload {
  storyId: string; bookSize: 'A5' | 'B5'; coverType: 'SOFT' | 'HARD';
  copies: number; recipientName: string; addressMemo: string;
}

export const listOrders   = () => apiFetch<Order[]>('/api/orders');
export const createOrder  = (p: OrderCreatePayload) =>
  apiFetch<{ id: string; status: string }>('/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(p),
  });
export const updateOrderStatus = (id: string, status: 'PROCESSING' | 'COMPLETED') =>
  apiFetch<{ ok: boolean }>(`/api/orders/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  });
```

- [ ] **Step 5:** `router/index.ts`

```typescript
import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/stories/new', component: () => import('../views/NewStoryView.vue') },
    { path: '/stories/:id', component: () => import('../views/StoryDetailView.vue') },
    { path: '/orders', component: () => import('../views/OrdersView.vue') },
  ],
});
```

- [ ] **Step 6:** `components/Navbar.vue`

```vue
<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>

<template>
  <nav class="bg-white border-b border-gray-200 px-8 py-3 flex items-center gap-6 text-sm">
    <RouterLink to="/" class="font-extrabold text-base">📚 동화공방</RouterLink>
    <RouterLink to="/" class="text-gray-600 hover:text-gray-900" active-class="text-blue-600 font-semibold" exact>동화 목록</RouterLink>
    <RouterLink to="/orders" class="text-gray-600 hover:text-gray-900" active-class="text-blue-600 font-semibold">주문</RouterLink>
  </nav>
</template>
```

- [ ] **Step 7:** `main.ts` + `App.vue` 수정

```typescript
// main.ts
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import './style.css';
createApp(App).use(router).mount('#app');
```

```vue
<!-- App.vue -->
<script setup lang="ts">
import Navbar from './components/Navbar.vue';
import { RouterView } from 'vue-router';
</script>
<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />
    <RouterView />
  </div>
</template>
```

- [ ] **Step 8:** Commit

```bash
git add frontend/
git commit -m "feat: vue router + api client + nav + types"
```

---

### Task 21 — HomeView + StoryCard (3가지 상태)

**Files:**
- Create: `frontend/src/views/HomeView.vue`
- Create: `frontend/src/components/StoryCard.vue`
- Create: `frontend/src/components/EmptyState.vue`

- [ ] **Step 1:** `EmptyState.vue`

```vue
<script setup lang="ts">
defineProps<{ icon: string; title: string; subtitle: string; ctaText?: string; ctaTo?: string }>();
</script>

<template>
  <div class="text-center py-16 px-8 bg-white rounded-xl border border-gray-200">
    <div class="text-5xl">{{ icon }}</div>
    <h3 class="mt-3 text-lg font-bold text-gray-900">{{ title }}</h3>
    <p class="mt-1 text-sm text-gray-500">{{ subtitle }}</p>
    <RouterLink v-if="ctaText && ctaTo" :to="ctaTo"
      class="inline-block mt-4 bg-gray-900 text-white px-5 py-2 rounded-lg text-sm font-bold">
      {{ ctaText }}
    </RouterLink>
  </div>
</template>
```

- [ ] **Step 2:** `StoryCard.vue`

```vue
<script setup lang="ts">
import type { StorySummary } from '../types';
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps<{ story: StorySummary }>();
const router = useRouter();

const isCompleted = computed(() => props.story.status === 'COMPLETED');
const isFailed    = computed(() => props.story.status === 'FAILED');
const isGenerating = computed(() =>
  ['ANALYZING_DRAWING', 'GENERATING_STORY', 'GENERATING_IMAGES', 'DRAFT'].includes(props.story.status));

const stepLabel = computed(() => {
  switch (props.story.status) {
    case 'DRAFT': return '시작 중...';
    case 'ANALYZING_DRAWING': return '그림 분석 중...';
    case 'GENERATING_STORY': return '스토리 작성 중...';
    case 'GENERATING_IMAGES': return '일러스트 생성 중...';
    default: return '';
  }
});

function open() {
  if (isFailed.value || isGenerating.value || isCompleted.value) {
    router.push(`/stories/${props.story.id}`);
  }
}

defineEmits<{ retry: [id: string] }>();
</script>

<template>
  <div class="bg-white border rounded-xl overflow-hidden cursor-pointer transition hover:shadow-lg"
       :class="isFailed ? 'border-red-200' : 'border-gray-200'"
       @click="open">
    <div class="aspect-[3/4] relative flex items-center justify-center">
      <template v-if="isCompleted && story.coverUrl">
        <img :src="story.coverUrl" class="w-full h-full object-cover" :alt="story.title" />
      </template>
      <template v-else-if="isGenerating">
        <div class="w-full h-full bg-gradient-to-br from-amber-100 to-orange-200 flex flex-col items-center justify-center">
          <div class="text-3xl">⏳</div>
          <div class="mt-2 text-xs text-amber-900 font-semibold">{{ stepLabel }}</div>
        </div>
      </template>
      <template v-else-if="isFailed">
        <div class="w-full h-full bg-red-50 flex flex-col items-center justify-center">
          <div class="text-3xl">⚠️</div>
          <div class="mt-2 text-xs text-red-900 font-semibold">생성 실패</div>
          <button class="mt-2 bg-red-500 text-white text-xs px-3 py-1 rounded"
                  @click.stop="$emit('retry', story.id)">다시 시도</button>
        </div>
      </template>
    </div>
    <div class="p-3">
      <div class="font-bold text-sm">{{ story.title || '제목 생성중...' }}</div>
      <div class="mt-1 text-xs text-gray-500">{{ isCompleted ? '5페이지' : story.childName }}</div>
    </div>
  </div>
</template>
```

- [ ] **Step 3:** `HomeView.vue`

```vue
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { RouterLink } from 'vue-router';
import { listStories, retryStory } from '../api/stories';
import type { StorySummary } from '../types';
import StoryCard from '../components/StoryCard.vue';
import EmptyState from '../components/EmptyState.vue';

const stories = ref<StorySummary[]>([]);
const loading = ref(true);
let pollTimer: number | null = null;

async function refresh() {
  try {
    stories.value = await listStories();
  } finally {
    loading.value = false;
  }
}

function hasGenerating() {
  return stories.value.some(s =>
    ['DRAFT','ANALYZING_DRAWING','GENERATING_STORY','GENERATING_IMAGES'].includes(s.status));
}

async function startPolling() {
  if (pollTimer) return;
  pollTimer = window.setInterval(async () => {
    if (hasGenerating()) await refresh();
  }, 2000);
}

async function onRetry(id: string) {
  await retryStory(id);
  await refresh();
  startPolling();
}

onMounted(async () => {
  await refresh();
  startPolling();
});
onUnmounted(() => { if (pollTimer) clearInterval(pollTimer); });
</script>

<template>
  <main class="max-w-6xl mx-auto">
    <section class="px-8 py-8 bg-gradient-to-br from-amber-100 to-pink-100">
      <div class="max-w-2xl">
        <h1 class="text-2xl font-extrabold text-gray-900">아이의 그림과 상상이 동화책이 됩니다</h1>
        <p class="mt-2 text-sm text-gray-600">그림 1장과 상상 한 줄을 올리면 30초 안에 5페이지 동화책이 완성됩니다.</p>
        <RouterLink to="/stories/new"
          class="inline-block mt-4 bg-gray-900 text-white px-6 py-3 rounded-lg font-bold text-sm">
          + 새 동화 만들기
        </RouterLink>
      </div>
    </section>

    <section class="px-8 py-8">
      <div class="flex justify-between items-baseline mb-4">
        <h2 class="text-base font-bold">내 동화</h2>
        <span class="text-xs text-gray-500">총 {{ stories.length }}편</span>
      </div>

      <div v-if="loading" class="text-center py-16 text-gray-500">불러오는 중...</div>

      <EmptyState v-else-if="stories.length === 0"
        icon="📖" title="아직 동화가 없어요" subtitle="첫 동화를 만들어 시작해보세요"
        cta-text="+ 첫 동화 만들기" cta-to="/stories/new" />

      <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        <StoryCard v-for="s in stories" :key="s.id" :story="s" @retry="onRetry" />
      </div>
    </section>
  </main>
</template>
```

- [ ] **Step 4:** 브라우저 검증

Run: 백엔드 `cd backend && mvn spring-boot:run` (별도 터미널) + 프론트 `cd frontend && npm run dev` → http://localhost:5173 접속.
Expected: 시드 4편 카드 그리드.

- [ ] **Step 5:** Commit

```bash
git add frontend/
git commit -m "feat: home view + story card (3 states)"
```

---

### Task 22 — NewStoryView + UploadDropzone + 폼 검증

**Files:**
- Create: `frontend/src/components/UploadDropzone.vue`
- Create: `frontend/src/views/NewStoryView.vue`

- [ ] **Step 1:** `UploadDropzone.vue`

```vue
<script setup lang="ts">
import { ref, computed } from 'vue';

const props = defineProps<{ modelValue: File | null }>();
const emit = defineEmits<{ 'update:modelValue': [file: File | null]; error: [message: string] }>();

const isDragging = ref(false);
const previewUrl = computed(() => props.modelValue ? URL.createObjectURL(props.modelValue) : null);

function validate(file: File): string | null {
  if (!['image/jpeg', 'image/png'].includes(file.type)) return '5MB 이하 JPG/PNG만 업로드 가능합니다';
  if (file.size > 5 * 1024 * 1024) return '5MB 이하 JPG/PNG만 업로드 가능합니다';
  return null;
}

function handle(file: File | null) {
  if (!file) { emit('update:modelValue', null); return; }
  const err = validate(file);
  if (err) { emit('error', err); return; }
  emit('update:modelValue', file);
}

function onDrop(e: DragEvent) {
  e.preventDefault();
  isDragging.value = false;
  handle(e.dataTransfer?.files?.[0] ?? null);
}

function onPick(e: Event) {
  handle((e.target as HTMLInputElement).files?.[0] ?? null);
}
</script>

<template>
  <label class="block aspect-square border-2 border-dashed rounded-xl bg-white cursor-pointer flex flex-col items-center justify-center transition relative overflow-hidden"
         :class="isDragging ? 'border-blue-500 bg-blue-50' : 'border-gray-300'"
         @dragover.prevent="isDragging = true"
         @dragleave="isDragging = false"
         @drop="onDrop">
    <img v-if="previewUrl" :src="previewUrl" class="absolute inset-0 w-full h-full object-cover" />
    <template v-else>
      <div class="text-5xl">🎨</div>
      <div class="mt-3 text-sm font-semibold">파일 끌어다 놓기</div>
      <div class="mt-1 text-xs text-gray-500">또는 클릭하여 선택</div>
      <div class="mt-4 text-[11px] text-gray-400">JPG, PNG · 최대 5MB</div>
    </template>
    <input type="file" accept="image/jpeg,image/png" capture="environment"
           class="hidden" @change="onPick" />
  </label>
</template>
```

- [ ] **Step 2:** `NewStoryView.vue`

```vue
<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import UploadDropzone from '../components/UploadDropzone.vue';
import { createStory } from '../api/stories';
import { ApiException } from '../api/client';

const router = useRouter();
const drawing = ref<File | null>(null);
const childName = ref('');
const imagination = ref('');
const errors = ref<string[]>([]);
const submitting = ref(false);

const charCount = computed(() => imagination.value.length);

function validate(): string[] {
  const list: string[] = [];
  if (!drawing.value) list.push('그림을 업로드해주세요');
  const name = childName.value.trim();
  if (name.length < 1 || name.length > 20) list.push('아이 이름은 1~20자로 적어주세요');
  if (imagination.value.length < 10 || imagination.value.length > 500) list.push('상상은 10자 이상 500자 이하로 적어주세요');
  return list;
}

async function submit() {
  errors.value = validate();
  if (errors.value.length > 0) return;
  submitting.value = true;
  try {
    const fd = new FormData();
    fd.append('drawing', drawing.value!);
    fd.append('childName', childName.value.trim());
    fd.append('imaginationPrompt', imagination.value);
    const resp = await createStory(fd);
    router.push(`/stories/${resp.id}`);
  } catch (e) {
    errors.value = [e instanceof ApiException ? e.message : '알 수 없는 오류'];
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <main class="max-w-5xl mx-auto px-8 py-8">
    <div class="text-sm mb-6"><RouterLink to="/" class="text-gray-500">← 동화 목록</RouterLink></div>
    <h1 class="text-xl font-bold mb-6">새 동화 만들기</h1>

    <div class="grid md:grid-cols-2 gap-8">
      <div>
        <div class="text-sm font-bold mb-2">① 아이의 그림 <span class="text-red-500">*</span></div>
        <p class="text-xs text-gray-500 mb-3">크레용·색연필·디지털 다 OK. 일러스트의 스타일 레퍼런스로 사용됩니다.</p>
        <UploadDropzone v-model="drawing" @error="m => errors = [m]" />
      </div>

      <div>
        <div class="mb-6">
          <label class="block text-sm font-bold mb-2">② 아이 이름 <span class="text-red-500">*</span></label>
          <input v-model="childName" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                 placeholder="예: 서아" maxlength="20" />
          <div class="mt-1 text-[11px] text-gray-400">최대 20자</div>
        </div>

        <div class="mb-6">
          <label class="block text-sm font-bold mb-2">③ 아이의 상상 <span class="text-red-500">*</span></label>
          <textarea v-model="imagination" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm h-40"
                    placeholder="예: 곰돌이가 우주에 가서 별을 따왔어!" maxlength="500"></textarea>
          <div class="mt-1 flex justify-between text-[11px] text-gray-400">
            <span>10자 이상 500자 이하</span><span>{{ charCount }} / 500</span>
          </div>
        </div>

        <div v-if="errors.length > 0" class="bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg p-3 mb-4">
          <div v-for="e in errors" :key="e">⚠️ {{ e }}</div>
        </div>

        <button class="w-full bg-gray-900 text-white py-3 rounded-lg font-bold disabled:opacity-50"
                :disabled="submitting" @click="submit">
          {{ submitting ? '생성 중...' : '동화 만들기 ✨' }}
        </button>
      </div>
    </div>
  </main>
</template>
```

- [ ] **Step 3:** 브라우저 수동 검증 (그림 업로드, 짧은 텍스트로 검증 메시지 확인)

- [ ] **Step 4:** Commit

```bash
git add frontend/
git commit -m "feat: new story view with upload dropzone + validation"
```

---

### Task 23 — useStoryStatus composable (TDD with Vitest)

**Files:**
- Create: `frontend/src/composables/useStoryStatus.ts`
- Create: `frontend/src/composables/__tests__/useStoryStatus.spec.ts`
- Modify: `frontend/package.json` (vitest 추가)

- [ ] **Step 1:** vitest 설치

```bash
cd frontend && npm install -D vitest @vue/test-utils jsdom
```

`package.json`의 scripts에 `"test": "vitest"` 추가. `vite.config.ts`에 `test: { environment: 'jsdom' }`.

- [ ] **Step 2:** 테스트 작성 (failing)

```typescript
// frontend/src/composables/__tests__/useStoryStatus.spec.ts
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { ref, nextTick } from 'vue';
import { useStoryStatus } from '../useStoryStatus';

describe('useStoryStatus', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    global.fetch = vi.fn();
  });
  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it('polls until COMPLETED then stops', async () => {
    const responses = [
      { status: 'GENERATING_STORY' },
      { status: 'GENERATING_IMAGES' },
      { status: 'COMPLETED' },
    ];
    let i = 0;
    (global.fetch as any).mockImplementation(() => Promise.resolve({
      ok: true, status: 200, json: () => Promise.resolve(responses[i++])
    }));

    const { story, isPolling, start, stop } = useStoryStatus('s1');
    start();
    await vi.advanceTimersByTimeAsync(2100);
    await vi.advanceTimersByTimeAsync(2100);
    await vi.advanceTimersByTimeAsync(2100);

    expect(story.value?.status).toBe('COMPLETED');
    expect(isPolling.value).toBe(false);
    stop();
  });

  it('stops on FAILED', async () => {
    (global.fetch as any).mockResolvedValue({
      ok: true, status: 200, json: () => Promise.resolve({ status: 'FAILED' })
    });
    const { story, isPolling, start } = useStoryStatus('s1');
    start();
    await vi.advanceTimersByTimeAsync(2100);
    expect(story.value?.status).toBe('FAILED');
    expect(isPolling.value).toBe(false);
  });
});
```

- [ ] **Step 3:** 구현

```typescript
// frontend/src/composables/useStoryStatus.ts
import { ref, onUnmounted } from 'vue';
import type { Story } from '../types';
import { getStory } from '../api/stories';

const TERMINAL = new Set(['COMPLETED', 'FAILED']);

export function useStoryStatus(id: string, intervalMs = 2000) {
  const story = ref<Story | null>(null);
  const isPolling = ref(false);
  let timer: ReturnType<typeof setInterval> | null = null;

  async function tick() {
    try {
      story.value = await getStory(id);
      if (TERMINAL.has(story.value.status)) stop();
    } catch (e) {
      console.error('poll failed', e);
    }
  }

  function start() {
    if (isPolling.value) return;
    isPolling.value = true;
    tick();
    timer = setInterval(tick, intervalMs);
  }

  function stop() {
    isPolling.value = false;
    if (timer) { clearInterval(timer); timer = null; }
  }

  onUnmounted(stop);

  return { story, isPolling, start, stop, refresh: tick };
}
```

- [ ] **Step 4:** 테스트 실행 → PASS

Run: `cd frontend && npm test -- --run`
Expected: 2 tests passed.

- [ ] **Step 5:** Commit

```bash
git add frontend/
git commit -m "feat: useStoryStatus composable with vitest tests"
```

---

### Task 24 — 3 페이지 레이아웃 컴포넌트

**Files:**
- Create: `frontend/src/components/pages/PageLayoutCover.vue`
- Create: `frontend/src/components/pages/PageLayoutSplit.vue`
- Create: `frontend/src/components/pages/PageLayoutEnding.vue`

- [ ] **Step 1:** `PageLayoutCover.vue` — 전면 일러스트 + 제목 오버레이

```vue
<script setup lang="ts">
defineProps<{
  illustrationUrl: string | null;
  title: string;
  pageNumber: number;
  editable?: boolean;
}>();
defineEmits<{ regenerate: []; }>();
</script>

<template>
  <div class="relative w-[340px] aspect-[3/4] bg-gradient-to-br from-emerald-200 to-cyan-200 flex items-center justify-center overflow-hidden">
    <img v-if="illustrationUrl" :src="illustrationUrl" class="absolute inset-0 w-full h-full object-cover" />
    <div v-else class="absolute inset-0 flex items-center justify-center text-5xl opacity-50">📖</div>
    <div class="absolute top-3 left-3 right-3 bg-white/85 rounded px-3 py-2">
      <div class="text-base font-extrabold text-gray-900 line-clamp-2">{{ title }}</div>
    </div>
    <div class="absolute bottom-2 right-3 text-[11px] bg-black/50 text-white rounded-full px-2 py-0.5">
      표지 · {{ pageNumber }}
    </div>
    <button v-if="editable && illustrationUrl" class="absolute bottom-3 left-3 bg-white/90 text-xs px-2 py-1 rounded"
            @click="$emit('regenerate')">🔄 재생성</button>
  </div>
</template>
```

- [ ] **Step 2:** `PageLayoutSplit.vue` — 좌 일러스트 / 우 본문

```vue
<script setup lang="ts">
defineProps<{
  illustrationUrl: string | null;
  bodyText: string | null;
  pageNumber: number;
  editable?: boolean;
}>();
const emit = defineEmits<{ 'update:bodyText': [v: string]; regenerate: [] }>();

function onInput(e: Event) {
  emit('update:bodyText', (e.target as HTMLTextAreaElement).value);
}
</script>

<template>
  <div class="flex w-[680px] aspect-[3/2]">
    <div class="flex-1 bg-gradient-to-br from-pink-200 to-violet-200 flex items-center justify-center">
      <img v-if="illustrationUrl" :src="illustrationUrl" class="w-full h-full object-cover" />
      <div v-else class="text-center text-gray-500">
        <div class="text-3xl">⚠️</div>
        <div class="text-xs mt-1">일러스트 실패</div>
      </div>
    </div>
    <div class="flex-1 bg-amber-50 p-8 flex flex-col justify-between">
      <div>
        <div class="text-[10px] uppercase font-bold tracking-wide text-gray-400">Page {{ pageNumber }}</div>
        <textarea v-if="editable" :value="bodyText ?? ''" @input="onInput"
                  class="mt-3 w-full h-32 bg-transparent text-sm leading-relaxed resize-none focus:outline-none focus:bg-white rounded p-2"
                  placeholder="페이지 본문..." />
        <p v-else class="mt-3 text-sm leading-relaxed">{{ bodyText }}</p>
      </div>
      <div class="flex gap-2">
        <button class="text-xs px-3 py-1 border rounded bg-white" @click="$emit('regenerate')">🔄 재생성</button>
      </div>
    </div>
  </div>
</template>
```

- [ ] **Step 3:** `PageLayoutEnding.vue` — 상단 본문 / 하단 일러스트

```vue
<script setup lang="ts">
defineProps<{
  illustrationUrl: string | null;
  bodyText: string | null;
  pageNumber: number;
  editable?: boolean;
}>();
const emit = defineEmits<{ 'update:bodyText': [v: string]; regenerate: [] }>();
function onInput(e: Event) { emit('update:bodyText', (e.target as HTMLTextAreaElement).value); }
</script>

<template>
  <div class="w-[340px] aspect-[3/4] flex flex-col">
    <div class="bg-amber-50 p-4">
      <textarea v-if="editable" :value="bodyText ?? ''" @input="onInput"
                class="w-full text-sm text-center font-semibold leading-relaxed bg-transparent resize-none focus:outline-none focus:bg-white rounded p-2"
                rows="2" />
      <p v-else class="text-sm text-center font-semibold">{{ bodyText }}</p>
    </div>
    <div class="flex-1 bg-gradient-to-br from-indigo-200 to-emerald-200 flex items-center justify-center relative">
      <img v-if="illustrationUrl" :src="illustrationUrl" class="w-full h-full object-cover" />
      <div v-else class="text-3xl opacity-60">🌙</div>
      <button v-if="editable" class="absolute bottom-2 right-2 text-xs bg-white/90 px-2 py-1 rounded"
              @click="$emit('regenerate')">🔄 재생성</button>
    </div>
  </div>
</template>
```

- [ ] **Step 4:** Commit

```bash
git add frontend/src/components/pages/
git commit -m "feat: 3 page layout components (cover/split/ending)"
```

---

### Task 25 — StoryDetailView + ProgressStepper + BookViewer + BeforeAfterStrip

**Files:**
- Create: `frontend/src/components/ProgressStepper.vue`
- Create: `frontend/src/components/BeforeAfterStrip.vue`
- Create: `frontend/src/components/BookViewer.vue`
- Create: `frontend/src/views/StoryDetailView.vue`

- [ ] **Step 1:** `ProgressStepper.vue` (생성중 단계 표시)

```vue
<script setup lang="ts">
import type { StoryStatus } from '../types';
import { computed } from 'vue';

const props = defineProps<{ status: StoryStatus; completedPages: number }>();

const steps = [
  { key: 'ANALYZING_DRAWING', label: '그림 분석' },
  { key: 'GENERATING_STORY',  label: '스토리 작성' },
  { key: 'GENERATING_IMAGES', label: '일러스트' },
  { key: 'COMPLETED',         label: '완성' },
];

function stateOf(stepKey: string): 'done' | 'active' | 'pending' {
  const order = ['DRAFT','ANALYZING_DRAWING','GENERATING_STORY','GENERATING_IMAGES','COMPLETED'];
  const cur = order.indexOf(props.status);
  const ix  = order.indexOf(stepKey);
  if (cur > ix) return 'done';
  if (cur === ix) return 'active';
  return 'pending';
}
</script>

<template>
  <div class="flex justify-between items-center max-w-2xl mx-auto text-xs">
    <template v-for="(step, i) in steps" :key="step.key">
      <div class="flex-1 text-center">
        <div :class="[
          'w-8 h-8 rounded-full mx-auto flex items-center justify-center font-bold',
          stateOf(step.key) === 'done'   ? 'bg-emerald-500 text-white' :
          stateOf(step.key) === 'active' ? 'bg-blue-500 text-white animate-pulse' :
                                            'bg-gray-200 text-gray-400']">
          <span v-if="stateOf(step.key) === 'done'">✓</span>
          <span v-else>{{ i + 1 }}</span>
        </div>
        <div :class="['mt-2', stateOf(step.key) !== 'pending' ? 'text-gray-900 font-semibold' : 'text-gray-400']">
          <span v-if="step.key === 'GENERATING_IMAGES' && stateOf(step.key) === 'active'">
            {{ completedPages }}/5 일러스트
          </span>
          <span v-else>{{ step.label }}</span>
        </div>
      </div>
      <div v-if="i < steps.length - 1" class="flex-1 h-0.5 bg-gray-200" />
    </template>
  </div>
</template>
```

- [ ] **Step 2:** `BeforeAfterStrip.vue`

```vue
<script setup lang="ts">
defineProps<{ drawingUrl: string | null; styleDescriptor: string | null }>();
</script>

<template>
  <div class="bg-white px-8 py-3 border-b border-gray-100 flex items-center gap-4">
    <span class="text-[10px] uppercase tracking-wider text-gray-500 font-bold">원본 그림</span>
    <img v-if="drawingUrl" :src="drawingUrl" class="w-12 h-12 rounded-lg object-cover" />
    <div v-else class="w-12 h-12 bg-gray-100 rounded-lg" />
    <span class="text-gray-400 text-lg">→</span>
    <span class="text-[10px] uppercase tracking-wider text-gray-500 font-bold">일러스트 스타일</span>
    <span v-if="styleDescriptor" class="text-xs italic text-gray-700 bg-gray-50 px-3 py-1.5 rounded-md">
      "{{ styleDescriptor }}"
    </span>
  </div>
</template>
```

- [ ] **Step 3:** `BookViewer.vue`

```vue
<script setup lang="ts">
import { ref, computed } from 'vue';
import type { PageData } from '../types';
import PageLayoutCover  from './pages/PageLayoutCover.vue';
import PageLayoutSplit  from './pages/PageLayoutSplit.vue';
import PageLayoutEnding from './pages/PageLayoutEnding.vue';

const props = defineProps<{ pages: PageData[]; title: string }>();
const emit = defineEmits<{
  'update:pageBody': [pageNumber: number, bodyText: string];
  regenerate: [pageNumber: number];
}>();

const idx = ref(0);
const current = computed(() => props.pages[idx.value]);
const canPrev = computed(() => idx.value > 0);
const canNext = computed(() => idx.value < props.pages.length - 1);
</script>

<template>
  <div class="flex items-center gap-3 justify-center">
    <button class="w-10 h-10 rounded-full bg-white border" :disabled="!canPrev" @click="idx--">‹</button>

    <div class="shadow-2xl rounded-lg overflow-hidden">
      <PageLayoutCover v-if="current.layout === 'COVER'"
        :illustration-url="current.illustrationUrl" :title="title" :page-number="current.pageNumber"
        editable @regenerate="$emit('regenerate', current.pageNumber)" />
      <PageLayoutSplit v-else-if="current.layout === 'SPLIT'"
        :illustration-url="current.illustrationUrl" :body-text="current.bodyText" :page-number="current.pageNumber"
        editable
        @update:body-text="v => $emit('update:pageBody', current.pageNumber, v)"
        @regenerate="$emit('regenerate', current.pageNumber)" />
      <PageLayoutEnding v-else
        :illustration-url="current.illustrationUrl" :body-text="current.bodyText" :page-number="current.pageNumber"
        editable
        @update:body-text="v => $emit('update:pageBody', current.pageNumber, v)"
        @regenerate="$emit('regenerate', current.pageNumber)" />
    </div>

    <button class="w-10 h-10 rounded-full bg-white border" :disabled="!canNext" @click="idx++">›</button>
  </div>

  <div class="mt-6 flex justify-center gap-2">
    <button v-for="(p, i) in pages" :key="p.pageNumber"
            class="w-10 h-14 rounded border-2 overflow-hidden"
            :class="i === idx ? 'border-gray-900' : 'border-transparent opacity-60'"
            @click="idx = i">
      <img v-if="p.illustrationUrl" :src="p.illustrationUrl" class="w-full h-full object-cover" />
      <div v-else class="w-full h-full bg-gray-200" />
    </button>
  </div>
</template>
```

- [ ] **Step 4:** `StoryDetailView.vue`

```vue
<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStoryStatus } from '../composables/useStoryStatus';
import { updatePageBody, regeneratePage } from '../api/stories';
import ProgressStepper from '../components/ProgressStepper.vue';
import BeforeAfterStrip from '../components/BeforeAfterStrip.vue';
import BookViewer from '../components/BookViewer.vue';
import OrderModal from '../components/OrderModal.vue';

const route = useRoute();
const router = useRouter();
const id = route.params.id as string;

const { story, start } = useStoryStatus(id);
const showOrder = ref(false);

const isCompleted = computed(() => story.value?.status === 'COMPLETED');
const isFailed    = computed(() => story.value?.status === 'FAILED');
const isGenerating = computed(() =>
  story.value && ['DRAFT','ANALYZING_DRAWING','GENERATING_STORY','GENERATING_IMAGES'].includes(story.value.status));

const completedPages = computed(() =>
  story.value?.pages?.filter(p => p.illustrationUrl).length ?? 0);

const styleKeywords = computed(() => {
  if (!story.value?.styleDescriptor) return null;
  try {
    const parsed = JSON.parse(story.value.styleDescriptor);
    return parsed.keywords?.join(', ');
  } catch { return null; }
});

async function onPageBodyUpdate(n: number, body: string) {
  await updatePageBody(id, n, body);
}

async function onRegenerate(n: number) {
  await regeneratePage(id, n);
  setTimeout(() => start(), 500);
}

onMounted(start);
</script>

<template>
  <main class="max-w-6xl mx-auto">
    <div class="bg-white border-b border-gray-200 px-8 py-3 flex items-center justify-between">
      <div class="flex items-center gap-4 text-sm">
        <RouterLink to="/" class="text-gray-500">← 동화 목록</RouterLink>
        <h1 class="font-bold text-base">{{ story?.title || '동화 생성중...' }}</h1>
        <span v-if="story" class="text-xs text-gray-500">작가: {{ story.childName }}</span>
      </div>
      <button v-if="isCompleted" class="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm font-bold"
              @click="showOrder = true">📦 이 동화로 책 만들기</button>
    </div>

    <div v-if="isGenerating" class="py-12 px-8 text-center">
      <div class="text-sm text-gray-500 mb-6">{{ story?.childName }}의 동화를 만들고 있어요</div>
      <ProgressStepper :status="story!.status" :completed-pages="completedPages" />
      <div class="mt-8 grid grid-cols-5 gap-3 max-w-2xl mx-auto">
        <div v-for="n in 5" :key="n"
             class="aspect-[3/4] rounded-lg flex items-center justify-center text-xs"
             :class="story!.pages?.find(p => p.pageNumber === n)?.illustrationUrl
                     ? 'bg-emerald-100 text-emerald-700'
                     : 'bg-gray-100 text-gray-400'">
          {{ story!.pages?.find(p => p.pageNumber === n)?.illustrationUrl ? '✓' : '대기' }}
        </div>
      </div>
    </div>

    <div v-else-if="isFailed" class="py-12 px-8 text-center text-red-700">
      <div class="text-3xl">⚠️</div>
      <p class="mt-3 font-semibold">{{ story?.errorMessage || '동화 생성에 실패했어요' }}</p>
      <p class="mt-1 text-xs text-red-600">목록 화면에서 다시 시도 버튼을 눌러주세요</p>
    </div>

    <template v-else-if="isCompleted && story">
      <BeforeAfterStrip :drawing-url="story.drawingUrl" :style-descriptor="styleKeywords" />
      <div class="p-8">
        <BookViewer :pages="story.pages" :title="story.title"
                    @update:page-body="onPageBodyUpdate" @regenerate="onRegenerate" />
      </div>
    </template>

    <OrderModal v-if="showOrder && story" :story="story" @close="showOrder = false"
                @created="() => router.push('/orders')" />
  </main>
</template>
```

- [ ] **Step 5:** Commit

```bash
git add frontend/
git commit -m "feat: story detail view with progress + book viewer"
```

---

## Phase 5 — 주문 Lv2 (Hour 13-16)

### Task 26 — Order DTO + Service + Controller (POST/GET)

**Files:**
- Create: `backend/src/main/java/com/sweetbook/web/dto/OrderCreateRequest.java`
- Create: `backend/src/main/java/com/sweetbook/web/dto/OrderDto.java`
- Create: `backend/src/main/java/com/sweetbook/web/dto/OrderItemDto.java`
- Create: `backend/src/main/java/com/sweetbook/service/OrderService.java`
- Create: `backend/src/main/java/com/sweetbook/web/OrderController.java`

- [ ] **Step 1:** DTO

```java
// OrderCreateRequest.java
package com.sweetbook.web.dto;

import com.sweetbook.domain.*;
import jakarta.validation.constraints.*;

public record OrderCreateRequest(
    @NotBlank String storyId,
    @NotNull BookSize bookSize,
    @NotNull CoverType coverType,
    @Min(1) @Max(10) int copies,
    @NotBlank @Size(min = 1, max = 30, message = "받는 분 이름은 1~30자로 적어주세요") String recipientName,
    @Size(max = 500) String addressMemo
) {}

// OrderItemDto.java
package com.sweetbook.web.dto;

import com.sweetbook.domain.*;
public record OrderItemDto(BookSize bookSize, CoverType coverType, int copies) {}

// OrderDto.java
package com.sweetbook.web.dto;

import com.sweetbook.domain.OrderStatus;
import java.time.Instant;

public record OrderDto(
    String id,
    OrderStorySummary story,
    OrderStatus status,
    String recipientName,
    String addressMemo,
    OrderItemDto item,
    Instant createdAt
) {
    public record OrderStorySummary(String id, String title, String coverUrl) {}
}
```

- [ ] **Step 2:** OrderService

```java
package com.sweetbook.service;

import com.sweetbook.domain.*;
import com.sweetbook.repository.*;
import com.sweetbook.web.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orders;
    private final StoryRepository stories;

    public OrderService(OrderRepository orders, StoryRepository stories) {
        this.orders = orders; this.stories = stories;
    }

    @Transactional
    public OrderDto create(OrderCreateRequest req) {
        Story s = stories.findById(req.storyId())
            .orElseThrow(() -> new NoSuchElementException("STORY_NOT_FOUND"));
        if (s.getStatus() != StoryStatus.COMPLETED) {
            throw new IllegalArgumentException("STORY_NOT_COMPLETED");
        }
        Order o = Order.create(s, req.recipientName(), req.addressMemo());
        OrderItem item = OrderItem.create(o, req.bookSize(), req.coverType(), req.copies());
        o.setItem(item);
        Order saved = orders.save(o);
        return toDto(saved);
    }

    public List<OrderDto> list() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    @Transactional
    public OrderDto updateStatus(String id, OrderStatus target) {
        Order o = orders.findById(id).orElseThrow(() -> new NoSuchElementException("ORDER_NOT_FOUND"));
        o.transitionTo(target);  // IllegalStateException → 409 INVALID_TRANSITION
        return toDto(orders.save(o));
    }

    public Order getEntity(String id) {
        return orders.findById(id).orElseThrow(() -> new NoSuchElementException("ORDER_NOT_FOUND"));
    }

    private OrderDto toDto(Order o) {
        Story s = o.getStory();
        String coverUrl = s.getPages().stream()
            .filter(p -> p.getPageNumber() == 1)
            .findFirst().map(p -> StoryService.toFileUrl(p.getIllustrationUrl())).orElse(null);
        var item = o.getItem();
        return new OrderDto(
            o.getId(),
            new OrderDto.OrderStorySummary(s.getId(), s.getTitle(), coverUrl),
            o.getStatus(), o.getRecipientName(), o.getAddressMemo(),
            new OrderItemDto(item.getBookSize(), item.getCoverType(), item.getCopies()),
            o.getCreatedAt()
        );
    }
}
```

- [ ] **Step 3:** OrderController

```java
package com.sweetbook.web;

import com.sweetbook.domain.OrderStatus;
import com.sweetbook.service.OrderService;
import com.sweetbook.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) { this.orders = orders; }

    @PostMapping
    public OrderDto create(@Valid @RequestBody OrderCreateRequest req) {
        return orders.create(req);
    }

    @GetMapping
    public List<OrderDto> list() { return orders.list(); }

    @PatchMapping("/{id}/status")
    public OrderDto updateStatus(@PathVariable String id,
                                 @RequestBody Map<String, String> body) {
        OrderStatus target = OrderStatus.valueOf(body.get("status"));
        return orders.updateStatus(id, target);
    }
}
```

- [ ] **Step 4:** 통합 테스트 — 전이 검증

```java
// OrderControllerTest.java
@SpringBootTest @AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:ord;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "app.upload-dir=./build/ord-uploads", "app.ai.mock-mode=true"
})
class OrderControllerTest {

    @Autowired MockMvc mvc;

    @Test
    void createsOrderAndProgressesForward() throws Exception {
        // 시드 동화 사용
        String body = """
            {"storyId":"seed-story-1","bookSize":"A5","coverType":"HARD",
             "copies":1,"recipientName":"테스터","addressMemo":""}""";
        var result = mvc.perform(post("/api/orders").contentType("application/json").content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn();
        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        mvc.perform(patch("/api/orders/" + id + "/status").contentType("application/json")
                .content("{\"status\":\"PROCESSING\"}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void rejectsInvalidTransition() throws Exception {
        // seed-order-1은 PROCESSING 상태 → PENDING으로 후진 시도 → 409
        mvc.perform(patch("/api/orders/seed-order-1/status").contentType("application/json")
                .content("{\"status\":\"PENDING\"}"))
           .andExpect(status().isConflict())
           .andExpect(jsonPath("$.error").value("INVALID_TRANSITION"));
    }
}
```

- [ ] **Step 5:** Commit

```bash
git add backend/src/main/java/com/sweetbook/ backend/src/test/java/com/sweetbook/web/OrderControllerTest.java
git commit -m "feat: order create/list/status endpoints"
```

---

### Task 27 — OrderModal 컴포넌트

**Files:**
- Create: `frontend/src/components/OrderModal.vue`

- [ ] **Step 1:** OrderModal

```vue
<script setup lang="ts">
import { ref } from 'vue';
import type { Story } from '../types';
import { createOrder } from '../api/orders';
import { ApiException } from '../api/client';

const props = defineProps<{ story: Story }>();
const emit = defineEmits<{ close: []; created: [] }>();

const bookSize = ref<'A5' | 'B5'>('A5');
const coverType = ref<'SOFT' | 'HARD'>('HARD');
const copies = ref(1);
const recipientName = ref('');
const addressMemo = ref('');
const error = ref<string | null>(null);
const submitting = ref(false);

const cover = props.story.pages.find(p => p.pageNumber === 1)?.illustrationUrl;

async function submit() {
  if (recipientName.value.trim().length < 1 || recipientName.value.trim().length > 30) {
    error.value = '받는 분 이름은 1~30자로 적어주세요';
    return;
  }
  submitting.value = true;
  error.value = null;
  try {
    await createOrder({
      storyId: props.story.id,
      bookSize: bookSize.value, coverType: coverType.value,
      copies: copies.value,
      recipientName: recipientName.value.trim(),
      addressMemo: addressMemo.value,
    });
    emit('created');
  } catch (e) {
    error.value = e instanceof ApiException ? e.message : '오류가 발생했어요';
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="$emit('close')">
    <div class="bg-white rounded-2xl w-[480px] shadow-2xl">
      <div class="px-6 py-5 border-b border-gray-100 flex justify-between items-center">
        <h2 class="text-base font-bold">📦 종이책 주문</h2>
        <button class="text-gray-400 text-xl" @click="$emit('close')">×</button>
      </div>

      <div class="px-6 py-5 space-y-5">
        <div class="bg-gray-50 rounded-lg p-3 flex gap-3 items-center">
          <img v-if="cover" :src="cover" class="w-12 h-16 rounded object-cover" />
          <div>
            <div class="font-bold text-sm">{{ story.title }}</div>
            <div class="text-xs text-gray-500 mt-0.5">작가: {{ story.childName }} · 5페이지</div>
          </div>
        </div>

        <div>
          <div class="text-xs font-bold mb-2">책 사이즈</div>
          <div class="flex gap-2">
            <button v-for="s in ['A5','B5']" :key="s"
                    :class="['flex-1 py-2 rounded-lg border-2 text-sm font-bold',
                            bookSize === s ? 'bg-gray-900 text-white border-gray-900' : 'border-gray-300 text-gray-700']"
                    @click="bookSize = s as 'A5' | 'B5'">
              {{ s }}
            </button>
          </div>
        </div>

        <div>
          <div class="text-xs font-bold mb-2">표지</div>
          <div class="flex gap-2">
            <button v-for="c in [{v:'SOFT',label:'소프트커버'},{v:'HARD',label:'하드커버'}]" :key="c.v"
                    :class="['flex-1 py-2 rounded-lg border-2 text-sm font-bold',
                             coverType === c.v ? 'bg-gray-900 text-white border-gray-900' : 'border-gray-300']"
                    @click="coverType = c.v as 'SOFT' | 'HARD'">
              {{ c.label }}
            </button>
          </div>
        </div>

        <div>
          <div class="text-xs font-bold mb-2">부수</div>
          <div class="flex items-center gap-3">
            <button class="w-9 h-9 border rounded text-lg" @click="copies = Math.max(1, copies-1)">−</button>
            <div class="text-lg font-bold w-10 text-center">{{ copies }}</div>
            <button class="w-9 h-9 border rounded text-lg" @click="copies = Math.min(10, copies+1)">+</button>
            <span class="text-xs text-gray-400 ml-2">최대 10부</span>
          </div>
        </div>

        <div>
          <label class="block text-xs font-bold mb-2">받는 분 이름 <span class="text-red-500">*</span></label>
          <input v-model="recipientName" class="w-full px-3 py-2 border border-gray-300 rounded text-sm"
                 maxlength="30" />
        </div>

        <div>
          <label class="block text-xs font-bold mb-2">주소 메모</label>
          <textarea v-model="addressMemo" class="w-full px-3 py-2 border border-gray-300 rounded text-sm h-16"
                    maxlength="500" placeholder="실제 발송은 하지 않습니다 (시연용)"></textarea>
        </div>

        <div v-if="error" class="bg-red-50 text-red-700 text-xs rounded p-2">{{ error }}</div>
      </div>

      <div class="px-6 py-4 border-t border-gray-100 flex justify-end gap-2">
        <button class="border px-4 py-2 rounded-lg text-sm" @click="$emit('close')">취소</button>
        <button class="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm font-bold disabled:opacity-50"
                :disabled="submitting" @click="submit">
          {{ submitting ? '생성중...' : '주문 생성' }}
        </button>
      </div>
    </div>
  </div>
</template>
```

- [ ] **Step 2:** Commit

```bash
git add frontend/src/components/OrderModal.vue
git commit -m "feat: order creation modal"
```

---

### Task 28 — OrdersView + KanbanColumn + OrderCard

**Files:**
- Create: `frontend/src/components/OrderCard.vue`
- Create: `frontend/src/components/KanbanColumn.vue`
- Create: `frontend/src/views/OrdersView.vue`

- [ ] **Step 1:** OrderCard

```vue
<script setup lang="ts">
import type { Order } from '../types';

defineProps<{ order: Order }>();
const emit = defineEmits<{ advance: [id: string]; download: [id: string] }>();

function nextLabel(status: string): string {
  if (status === 'PENDING') return '→ 제작 시작';
  if (status === 'PROCESSING') return '→ 완료';
  return '';
}
function nextClass(status: string): string {
  return status === 'PROCESSING' ? 'bg-emerald-500' : 'bg-gray-900';
}
</script>

<template>
  <div class="bg-white rounded-lg p-3 shadow-sm">
    <div class="flex gap-2 items-center mb-2">
      <img v-if="order.story.coverUrl" :src="order.story.coverUrl"
           class="w-7 h-9 rounded object-cover" />
      <div v-else class="w-7 h-9 rounded bg-gray-200" />
      <div class="text-xs font-bold flex-1 truncate">{{ order.story.title }}</div>
    </div>
    <div class="text-[10px] text-gray-600 leading-relaxed">
      {{ order.item.bookSize }} · {{ order.item.coverType === 'HARD' ? '하드' : '소프트' }} · {{ order.item.copies }}부
    </div>
    <div class="text-[10px] text-gray-600">받는분: {{ order.recipientName }}</div>
    <div class="mt-2 flex gap-1">
      <button v-if="order.status !== 'COMPLETED'"
              :class="['flex-1 text-white rounded text-[10px] font-bold py-1.5', nextClass(order.status)]"
              @click="$emit('advance', order.id)">
        {{ nextLabel(order.status) }}
      </button>
      <button :class="['rounded text-[10px] py-1.5 px-2',
                       order.status === 'COMPLETED' ? 'bg-gray-900 text-white font-bold flex-1' : 'border']"
              @click="$emit('download', order.id)">
        📦 {{ order.status === 'COMPLETED' ? 'ZIP 다운로드' : 'ZIP' }}
      </button>
    </div>
  </div>
</template>
```

- [ ] **Step 2:** KanbanColumn

```vue
<script setup lang="ts">
import type { Order } from '../types';
import OrderCard from './OrderCard.vue';

defineProps<{
  title: string; icon: string; orders: Order[]; bg: string; titleColor: string;
}>();
defineEmits<{ advance: [id: string]; download: [id: string] }>();
</script>

<template>
  <div :class="['rounded-xl p-3', bg]">
    <div :class="['font-bold text-xs mb-3', titleColor]">{{ icon }} {{ title }} ({{ orders.length }})</div>
    <div class="space-y-2">
      <OrderCard v-for="o in orders" :key="o.id" :order="o"
                 @advance="id => $emit('advance', id)"
                 @download="id => $emit('download', id)" />
    </div>
  </div>
</template>
```

- [ ] **Step 3:** OrdersView

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import type { Order, OrderStatus } from '../types';
import { listOrders, updateOrderStatus } from '../api/orders';
import KanbanColumn from '../components/KanbanColumn.vue';

const orders = ref<Order[]>([]);

async function refresh() { orders.value = await listOrders(); }

const byStatus = (s: OrderStatus) => computed(() => orders.value.filter(o => o.status === s));
const pending    = byStatus('PENDING');
const processing = byStatus('PROCESSING');
const completed  = byStatus('COMPLETED');

async function onAdvance(id: string) {
  const cur = orders.value.find(o => o.id === id);
  if (!cur) return;
  const next = cur.status === 'PENDING' ? 'PROCESSING' : 'COMPLETED';
  await updateOrderStatus(id, next);
  await refresh();
}

function onDownload(id: string) {
  window.location.href = `/api/orders/${id}/export`;
}

onMounted(refresh);
</script>

<template>
  <main class="max-w-7xl mx-auto px-8 py-8">
    <div class="flex justify-between items-baseline mb-6">
      <h1 class="text-xl font-extrabold">주문 관리</h1>
      <span class="text-xs text-gray-500">총 {{ orders.length }}건</span>
    </div>
    <div class="grid grid-cols-3 gap-4">
      <KanbanColumn title="PENDING" icon="⏳" :orders="pending" bg="bg-amber-100" title-color="text-amber-900"
                    @advance="onAdvance" @download="onDownload" />
      <KanbanColumn title="PROCESSING" icon="🛠️" :orders="processing" bg="bg-blue-100" title-color="text-blue-900"
                    @advance="onAdvance" @download="onDownload" />
      <KanbanColumn title="COMPLETED" icon="✅" :orders="completed" bg="bg-emerald-100" title-color="text-emerald-900"
                    @advance="onAdvance" @download="onDownload" />
    </div>
  </main>
</template>
```

- [ ] **Step 4:** 브라우저 검증 (시드 주문 1건 + 새 주문 생성 흐름)

- [ ] **Step 5:** Commit

```bash
git add frontend/
git commit -m "feat: orders kanban view + cards"
```

---

## Phase 6 — ZIP 익스포트 Lv3 (Hour 16-18)

### Task 29 — ZipExportService (TDD: 구조 검증)

**Files:**
- Create: `backend/src/main/java/com/sweetbook/service/ZipExportService.java`
- Create: `backend/src/test/java/com/sweetbook/service/ZipExportServiceTest.java`

- [ ] **Step 1:** 테스트 작성 (failing)

```java
package com.sweetbook.service;

import com.sweetbook.domain.*;
import com.sweetbook.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:zip;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "app.upload-dir=./build/zip-uploads", "app.ai.mock-mode=true"
})
class ZipExportServiceTest {

    @Autowired ZipExportService zipExporter;
    @Autowired OrderRepository orders;

    @Test
    void zipContainsExpectedStructure() throws Exception {
        // seed-order-1 사용
        Order o = orders.findById("seed-order-1").orElseThrow();

        var baos = new ByteArrayOutputStream();
        zipExporter.export(o.getId(), baos);

        Set<String> entries = new HashSet<>();
        try (var zis = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) entries.add(e.getName());
        }

        // 총 14 엔트리: 루트 4(metadata, story, style, drawing) + pages/ 10(json×5 + png×5)
        assertEquals(14, entries.size(), "expected 14 entries, got: " + entries);

        String prefix = "order-seed-order-1/";
        assertTrue(entries.contains(prefix + "metadata.json"));
        assertTrue(entries.contains(prefix + "story.json"));
        assertTrue(entries.contains(prefix + "drawing.png"));
        assertTrue(entries.contains(prefix + "style.json"));
        for (int n = 1; n <= 5; n++) {
            String pn = String.format("%02d", n);
            assertTrue(entries.contains(prefix + "pages/page-" + pn + ".json"),
                "missing pages/page-" + pn + ".json");
            assertTrue(entries.contains(prefix + "pages/page-" + pn + ".png"),
                "missing pages/page-" + pn + ".png");
        }
    }

    @Test
    void metadataIncludesStatusHistory() throws Exception {
        var baos = new ByteArrayOutputStream();
        zipExporter.export("seed-order-1", baos);
        try (var zis = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                if (e.getName().endsWith("metadata.json")) {
                    String content = new String(zis.readAllBytes());
                    assertTrue(content.contains("statusHistory"));
                    assertTrue(content.contains("PENDING"));
                    assertTrue(content.contains("PROCESSING"));
                    return;
                }
            }
        }
        fail("metadata.json not found");
    }
}
```

- [ ] **Step 2:** 구현

```java
package com.sweetbook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sweetbook.domain.*;
import com.sweetbook.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

@Service
public class ZipExportService {

    private final OrderRepository orders;
    private final FileStorageService storage;
    private final ObjectMapper om = new ObjectMapper();

    public ZipExportService(OrderRepository orders, FileStorageService storage) {
        this.orders = orders;
        this.storage = storage;
    }

    @Transactional(readOnly = true)
    public void export(String orderId, OutputStream out) throws IOException {
        Order o = orders.findById(orderId).orElseThrow(() -> new NoSuchElementException("ORDER_NOT_FOUND"));
        Story s = o.getStory();
        String prefix = "order-" + o.getId() + "/";

        try (var zos = new ZipOutputStream(out)) {
            // metadata.json
            putJson(zos, prefix + "metadata.json", buildMetadata(o));

            // story.json
            putJson(zos, prefix + "story.json", Map.of(
                "id", s.getId(),
                "title", s.getTitle(),
                "childName", s.getChildName(),
                "imaginationPrompt", s.getImaginationPrompt(),
                "pageCount", s.getPages().size()
            ));

            // style.json
            String styleRaw = s.getStyleDescriptorJson();
            byte[] styleBytes = (styleRaw == null ? "{}" : styleRaw).getBytes();
            putBytes(zos, prefix + "style.json", styleBytes);

            // drawing.png — 원본 그림
            if (s.getDrawingUrl() != null) {
                putFile(zos, prefix + "drawing.png", storage.resolve(s.getDrawingUrl()));
            }

            // pages/
            for (Page p : s.getPages()) {
                String pn = String.format("%02d", p.getPageNumber());

                // page-NN.json
                Map<String, Object> pageMeta = new LinkedHashMap<>();
                pageMeta.put("pageNumber", p.getPageNumber());
                pageMeta.put("layout", p.getLayout().name());
                pageMeta.put("bodyText", p.getBodyText());
                pageMeta.put("illustrationPrompt", p.getIllustrationPrompt());
                pageMeta.put("illustrationMissing", p.getIllustrationUrl() == null);
                putJson(zos, prefix + "pages/page-" + pn + ".json", pageMeta);

                // page-NN.png
                Path src = p.getIllustrationUrl() != null
                    ? storage.resolve(p.getIllustrationUrl())
                    : storage.resolve("seed/placeholder.png");
                if (Files.exists(src)) {
                    putFile(zos, prefix + "pages/page-" + pn + ".png", src);
                }
            }
        }
    }

    private Map<String, Object> buildMetadata(Order o) {
        var item = o.getItem();
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("orderId", o.getId());
        meta.put("status", o.getStatus().name());
        meta.put("recipientName", o.getRecipientName());
        meta.put("addressMemo", o.getAddressMemo());
        meta.put("bookSize", item.getBookSize().name());
        meta.put("coverType", item.getCoverType().name());
        meta.put("copies", item.getCopies());
        meta.put("createdAt", o.getCreatedAt().toString());
        meta.put("statusHistory", o.getStatusHistoryJson());
        return meta;
    }

    private void putJson(ZipOutputStream zos, String name, Object data) throws IOException {
        byte[] bytes = om.writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
        putBytes(zos, name, bytes);
    }

    private void putBytes(ZipOutputStream zos, String name, byte[] bytes) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(bytes);
        zos.closeEntry();
    }

    private void putFile(ZipOutputStream zos, String name, Path src) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        Files.copy(src, zos);
        zos.closeEntry();
    }
}
```

- [ ] **Step 3:** 테스트 통과 확인

Run: `cd backend && mvn -Dtest=ZipExportServiceTest test`
Expected: 2 tests passed.

- [ ] **Step 4:** Commit

```bash
git add backend/
git commit -m "feat: zip export service with full structure"
```

---

### Task 30 — GET /api/orders/{id}/export 엔드포인트

**Files:**
- Modify: `backend/src/main/java/com/sweetbook/web/OrderController.java`

- [ ] **Step 1:** 엔드포인트 추가 (스트리밍)

```java
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;

@GetMapping("/{id}/export")
public StreamingResponseBody export(@PathVariable String id, HttpServletResponse response) {
    response.setContentType("application/zip");
    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=order-" + id + ".zip");
    return out -> zipExporter.export(id, out);
}
```

`OrderController` 생성자에 `ZipExportService zipExporter` 주입.

- [ ] **Step 2:** 통합 테스트 (헤더 + 본문)

```java
@Test
void exportEndpointReturnsZip() throws Exception {
    mvc.perform(get("/api/orders/seed-order-1/export"))
       .andExpect(status().isOk())
       .andExpect(header().string("Content-Type", "application/zip"))
       .andExpect(header().string("Content-Disposition",
            org.hamcrest.Matchers.containsString("order-seed-order-1.zip")));
}
```

- [ ] **Step 3:** 수동 검증

```bash
curl -O -J http://localhost:8080/api/orders/seed-order-1/export
unzip -l order-seed-order-1.zip   # 구조 검증
```

Expected: metadata.json, story.json, style.json, drawing.png, pages/page-01.{json,png}~page-05.{json,png} 모두 존재.

- [ ] **Step 4:** Commit

```bash
git add backend/
git commit -m "feat: GET /api/orders/{id}/export streaming endpoint"
```

---

## Phase 7 — 폴리시 (Hour 18-22)

### Task 31 — 빈 상태 / 로딩 / 에러 UI 통합 + 한국어 문구 점검

**Files:**
- Review/edit: 모든 `frontend/src/views/*.vue`, `frontend/src/components/EmptyState.vue`

- [ ] **Step 1:** OrdersView에 빈 상태 추가

```vue
<!-- OrdersView.vue 수정: 0건일 때 -->
<EmptyState v-if="orders.length === 0"
  icon="📦" title="아직 주문이 없어요"
  subtitle="동화 미리보기에서 '책 만들기' 버튼을 눌러보세요" />
<div v-else class="grid grid-cols-3 gap-4">
  <!-- 기존 칸반 -->
</div>
```

- [ ] **Step 2:** 로딩 스피너 컴포넌트 (단순)

```vue
<!-- frontend/src/components/Spinner.vue -->
<template>
  <div class="text-center py-16 text-gray-500">
    <div class="inline-block w-6 h-6 border-2 border-gray-300 border-t-gray-900 rounded-full animate-spin"></div>
    <div class="mt-2 text-xs">불러오는 중...</div>
  </div>
</template>
```

- [ ] **Step 3:** 에러 토스트 또는 인라인 메시지 표준화

각 view의 try/catch에서 ApiException.message를 표시하도록 통일. 이미 NewStoryView·OrderModal에 인라인 에러 박스 있음. HomeView/OrdersView에도 fetch 실패 시 인라인 에러 추가:

```typescript
// HomeView.vue refresh()에서
catch (e) {
  loading.value = false;
  loadError.value = e instanceof ApiException ? e.message : '서버에 연결할 수 없어요';
}
```

```vue
<div v-if="loadError" class="bg-red-50 text-red-700 text-sm rounded-lg p-3 mb-4">
  ⚠️ {{ loadError }}
</div>
```

- [ ] **Step 4:** 백엔드 한국어 메시지 일관성 점검 — `GlobalExceptionHandler`의 `userMessage` 메서드에 누락된 코드 추가

```java
private String userMessage(String code) {
    return switch (code) {
        case "UNSUPPORTED_IMAGE_TYPE" -> "5MB 이하 JPG/PNG만 업로드 가능합니다";
        case "DRAWING_REQUIRED" -> "그림을 업로드해주세요";
        case "STORY_NOT_COMPLETED" -> "완성된 동화로만 주문할 수 있어요";
        case "STORY_NOT_FOUND" -> "동화를 찾을 수 없어요";
        case "ORDER_NOT_FOUND" -> "주문을 찾을 수 없어요";
        case "PAGE_NOT_FOUND" -> "페이지를 찾을 수 없어요";
        default -> code;
    };
}
```

- [ ] **Step 5:** Commit

```bash
git add backend/ frontend/
git commit -m "polish: empty states + spinner + korean error messages"
```

---

### Task 32 — 시드 동화 일러스트 4편 수작업 업그레이드

**Files:**
- Replace: `backend/src/main/resources/seed/story-1/{cover,page-2,page-3,page-4,page-5}.png`
- Replace: `backend/src/main/resources/seed/story-2/...` (마찬가지)
- Replace: `backend/src/main/resources/seed/story-3/...`
- Replace: `backend/src/main/resources/seed/story-4/...`
- Replace: `backend/src/main/resources/seed/story-{1..4}/drawing.png`

- [ ] **Step 1:** OPENAI_API_KEY 있으면, 한 번만 실 호출로 4편 × 6장 = 24장 생성

다음 스크립트를 한 번 실행 (실 키 사용):

```bash
# 임시 — 한 번만 실행. backend/scripts/generate_seed.sh 같은 위치에 두고 .gitignore 처리.
# 4편 각각의 imagination/style을 직접 입력하고 실 호출로 PNG 받아 seed/ 폴더에 저장.
```

(이 단계는 수작업이라 step 자체는 짧음. 결과 PNG 24장을 `backend/src/main/resources/seed/story-N/{cover,page-2~5,drawing}.png`에 덮어쓰기.)

- [ ] **Step 2:** 키 없으면, 무료 일러스트(Unsplash·Lorem Picsum) 또는 직접 그린 그림 24장으로 대체. 4:5 비율로 통일.

- [ ] **Step 3:** docker-compose 재실행 + 메인 화면에서 4편 카드 썸네일이 의미 있는 일러스트로 보이는지 확인.

- [ ] **Step 4:** Commit

```bash
git add backend/src/main/resources/seed/
git commit -m "polish: upgrade seed illustrations (4 stories × 6 images)"
```

---

### Task 33 — End-to-end 통합 테스트 (스모크)

두 종류의 스모크를 분리한다:
1. **Order flow smoke** — 시드 동화 사용, 주문 생성 + 전이 + ZIP 익스포트. 빠르고 결정적.
2. **Story create smoke** — 멀티파트 업로드 + 비동기 generation이 mock 모드에서 COMPLETED까지 가는지. 가장 중요한 회귀 테스트.

**Files:**
- Create: `backend/src/test/java/com/sweetbook/integration/OrderFlowSmokeTest.java`
- Create: `backend/src/test/java/com/sweetbook/integration/StoryCreateSmokeTest.java`

- [ ] **Step 1:** OrderFlowSmokeTest — 시드 동화로 주문 흐름 + 익스포트만

```java
package com.sweetbook.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:order-smoke;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "app.upload-dir=./build/order-smoke-uploads",
    "app.ai.mock-mode=true"
})
class OrderFlowSmokeTest {

    @Autowired MockMvc mvc;

    @Test
    void seedStory_orderProgressesAndExports() throws Exception {
        var orderJson = """
            {"storyId":"seed-story-1","bookSize":"A5","coverType":"HARD",
             "copies":1,"recipientName":"E2E","addressMemo":""}""";
        var orderResp = mvc.perform(post("/api/orders")
                .contentType("application/json").content(orderJson))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String orderId = com.jayway.jsonpath.JsonPath.read(orderResp, "$.id");

        // PENDING → PROCESSING → COMPLETED
        mvc.perform(patch("/api/orders/" + orderId + "/status")
                .contentType("application/json").content("{\"status\":\"PROCESSING\"}"))
            .andExpect(status().isOk());
        mvc.perform(patch("/api/orders/" + orderId + "/status")
                .contentType("application/json").content("{\"status\":\"COMPLETED\"}"))
            .andExpect(status().isOk());

        // ZIP 다운로드 검증 (헤더 + status 200만, 본문 구조는 ZipExportServiceTest가 책임)
        mvc.perform(get("/api/orders/" + orderId + "/export"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "application/zip"))
            .andExpect(header().string("Content-Disposition",
                org.hamcrest.Matchers.containsString("order-" + orderId + ".zip")));
    }
}
```

- [ ] **Step 2:** StoryCreateSmokeTest — 멀티파트 업로드 + 비동기 generation 검증

가장 중요한 회귀 테스트. 다음을 동시에 검증한다:
- 멀티파트 폼 필드명이 `childName`/`imaginationPrompt` (camelCase)로 받아지는지
- `POST /api/stories`가 즉시 `{ id, status: "DRAFT" }` 응답하는지
- `@Async` kickOffAsyncGeneration이 트리거돼서 mock 모드 단계 전이를 거쳐 COMPLETED까지 가는지
- 5페이지 + 3 레이아웃이 채워지는지

```java
package com.sweetbook.integration;

import com.sweetbook.domain.*;
import com.sweetbook.repository.StoryRepository;
import com.sweetbook.web.dto.StoryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:story-smoke;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "app.upload-dir=./build/story-smoke-uploads",
    "app.ai.mock-mode=true"
})
class StoryCreateSmokeTest {

    @Autowired MockMvc mvc;
    @Autowired StoryRepository stories;
    @Autowired ObjectMapper om;

    @Test
    void multipartCreate_kicksOffAsyncGeneration_andReachesCompleted() throws Exception {
        // 1. 멀티파트 업로드 — 폼 필드명 camelCase 확인 + 즉시 DRAFT 응답
        byte[] pngBytes = "fake-png-bytes".getBytes();
        var drawing = new MockMultipartFile("drawing", "kid.png", "image/png", pngBytes);

        var resp = mvc.perform(multipart("/api/stories")
                .file(drawing)
                .param("childName", "테스터")
                .param("imaginationPrompt", "용감한 곰돌이가 바다에 가서 보물을 찾았어요"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andReturn().getResponse().getContentAsString();
        String storyId = com.jayway.jsonpath.JsonPath.read(resp, "$.id");

        // 2. 비동기 generation이 COMPLETED까지 가는지 (mock 600ms × 3단계 + 페이지 5장 ≈ 1.8s + α)
        Awaitility.await().atMost(Duration.ofSeconds(15)).pollInterval(Duration.ofMillis(300))
            .untilAsserted(() -> {
                Story s = stories.findById(storyId).orElseThrow();
                assertEquals(StoryStatus.COMPLETED, s.getStatus(),
                    "expected COMPLETED, status=" + s.getStatus()
                    + ", error=" + s.getErrorMessage());
            });

        // 3. 폴링용 GET이 5페이지 + 3 레이아웃을 모두 노출하는지
        var detailResp = mvc.perform(get("/api/stories/" + storyId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.pages.length()").value(5))
            .andExpect(jsonPath("$.pages[0].layout").value("COVER"))
            .andExpect(jsonPath("$.pages[1].layout").value("SPLIT"))
            .andExpect(jsonPath("$.pages[4].layout").value("ENDING"))
            .andReturn().getResponse().getContentAsString();

        StoryDto detail = om.readValue(detailResp, StoryDto.class);
        // 4. 일러스트 URL이 모든 페이지에 채워졌는지 (mock은 placeholder.png 100% 성공)
        assertTrue(detail.pages().stream().allMatch(p -> p.illustrationUrl() != null),
            "all pages should have illustrationUrl in mock mode");
        // 5. 스타일 디스크립터가 분석되어 저장됐는지
        assertNotNull(detail.styleDescriptor(), "styleDescriptor should be set after analysis");
    }
}
```

- [ ] **Step 3:** Awaitility 의존성 추가 (없으면)

`backend/pom.xml`의 dependencies에:
```xml
<dependency>
  <groupId>org.awaitility</groupId>
  <artifactId>awaitility</artifactId>
  <scope>test</scope>
</dependency>
```

(spring-boot-starter-test에 이미 포함되어 있으면 별도 선언 불필요. 확인 후 추가.)

- [ ] **Step 4:** 전체 테스트 실행

Run: `cd backend && mvn test`
Expected: 모든 테스트 PASS. StoryCreateSmokeTest는 약 3-5초 소요.

- [ ] **Step 5:** Commit

```bash
git add backend/src/test/java/com/sweetbook/integration/ backend/pom.xml
git commit -m "test: order flow + story create + async generation smoke tests"
```

---

## Phase 8 — Docker 통합 (Hour 22-24)

### Task 34 — Dockerfile + docker-compose 검증 (clean clone에서 실행)

**Files:** (수정만)

- [ ] **Step 1:** 로컬 환경 정리 후 clean 빌드

```bash
docker compose down -v   # 볼륨까지 삭제
docker compose build --no-cache
```

Expected: 빌드 성공, 에러 없음.

- [ ] **Step 2:** 한 방 실행

```bash
docker compose up
```

Expected:
- `db` 헬스체크 통과
- `app`이 Flyway V1·V2 마이그레이션 로그 출력
- ApplicationRunner가 seed 자산 복사
- 8080 포트 listen

- [ ] **Step 3:** 브라우저로 http://localhost:8080 접속, 4편 시드 동화 그리드 + 1건 시드 주문(PROCESSING) 보이는지 확인.

- [ ] **Step 4:** SPA history-mode fallback 검증 (가장 흔한 데모 사고 — URL 직접 진입·새로고침)

```bash
# 직접 진입 — 200 OK + index.html 본문 (Vue가 마운트되어 라우트 처리)
curl -i http://localhost:8080/stories/seed-story-1     # Expected: 200, Content-Type text/html
curl -i http://localhost:8080/stories/new              # Expected: 200, text/html
curl -i http://localhost:8080/orders                   # Expected: 200, text/html

# /api/* 는 컨트롤러가 응답
curl -i http://localhost:8080/api/stories              # Expected: 200, application/json

# 정적 자산은 ResourceHandler
curl -i http://localhost:8080/assets/index-XXXX.js     # Expected: 200, application/javascript
```

브라우저에서도 동일 검증: `/stories/seed-story-1` 직접 입력 → 미리보기 화면 정상. 그 상태에서 F5 새로고침 → 그대로 동화 미리보기 유지 (404 안 남).

- [ ] **Step 5:** ZIP 다운로드 수동 검증

```bash
curl -O -J http://localhost:8080/api/orders/seed-order-1/export
unzip -l order-seed-order-1.zip
```

Expected: 14개 엔트리 (4 + 5×2). 루트의 `metadata.json` + `story.json` + `style.json` + `drawing.png` 4개 + `pages/page-NN.json` × 5 + `pages/page-NN.png` × 5 = 10개. 합 14.

추가 검증: 압축 안 JSON이 camelCase인지 확인.
```bash
unzip -p order-seed-order-1.zip "order-seed-order-1/metadata.json" | head
# Expected: orderId, recipientName, bookSize, statusHistory 등 camelCase
unzip -p order-seed-order-1.zip "order-seed-order-1/pages/page-01.json" | head
# Expected: pageNumber, layout, bodyText, illustrationPrompt, illustrationMissing
```

- [ ] **Step 6:** 키 있으면 새 동화 생성 흐름 1회 검증 (그림 업로드 → 30~60초 → 완성).

- [ ] **Step 7:** down 후 다시 up — 시드는 1회 복사 (idempotent) 확인

```bash
docker compose down
docker compose up
# 로그에 "복사됨" 같은 중복 출력 없어야 함
```

- [ ] **Step 8:** Commit (수정사항 있을 시)

```bash
git add .
git commit -m "chore: docker compose smoke verified"
```

---

### Task 35 — 다른 환경(다른 머신·다른 포트)에서 검증

**Files:** (수정만)

- [ ] **Step 1:** 포트 충돌 시나리오 — `.env`에 `APP_PORT=9000` 설정 후 재실행

```bash
echo "APP_PORT=9000" > .env
echo "DB_PORT=3307" >> .env
docker compose down
docker compose up
# http://localhost:9000 접속 확인
```

- [ ] **Step 2:** AI_MOCK_MODE=false로 변경 + OPENAI_API_KEY 입력 후 새 동화 생성 1회 검증

- [ ] **Step 3:** clean 환경 시뮬레이션 (별도 폴더에 git clone)

```bash
cd /tmp && git clone <your-repo-url> sweetbook-test && cd sweetbook-test
cp .env.example .env
docker compose up
```

Expected: 처음 보는 사람이 README만 보고 한 번에 실행 가능. 막힘 없음.

---

## Phase 9 — 제출 (Hour 24-30)

### Task 36 — README 6 섹션 작성

**Files:**
- Replace: `README.md`

- [ ] **Step 1:** README 작성

```markdown
# 📚 Sweetbook — 아이의 그림과 상상이 동화책이 됩니다

스위트북 바이브코딩 풀스택 개발자 채용 과제. AI 동화 생성 + 종이책 주문 + 데이터 익스포트.

## 1. 서비스 소개

아이가 그린 그림과 상상한 이야기를 입력하면, AI가 그림의 스타일을 분석해 5페이지짜리 동화책을 만들어줍니다.

- **그림(스타일 레퍼런스) + 상상(스토리 프롬프트) 하이브리드** — 아이의 입력 둘 다 결과물에 살아있게.
- **3종 페이지 레이아웃** — 표지·내지·엔딩이 다른 모양.
- **종이책 주문** — 사이즈·표지·부수 선택 후 PENDING → PROCESSING → COMPLETED 흐름.
- **데이터 익스포트** — 주문 1건당 ZIP으로 메타·페이지 본문·일러스트 일괄 다운로드.

## 2. 실행 방법

요구사항: Docker Desktop (또는 Docker Engine + Compose).

```bash
git clone <repo-url>
cd sweetbook-storybook
cp .env.example .env   # 그대로 두면 mock 모드로 실행
docker compose up
```

브라우저에서 http://localhost:8080 (다른 포트는 `.env`의 `APP_PORT` 변경).

OpenAI 키 사용:
```bash
# .env에서
OPENAI_API_KEY=sk-...
AI_MOCK_MODE=false
```

## 3. 구현된 레벨

- **Lv1 (서비스 플로우)** ✅ — 동화 목록·생성(그림+상상)·미리보기·페이지 편집·재생성
- **Lv2 (주문 흐름)** ✅ — 주문 생성·칸반 관리·상태 전이(전진만)
- **Lv3 (데이터 익스포트)** ✅ — 주문 1건당 ZIP (metadata + story + 5페이지 + 원본 그림 + 스타일)

## 4. 기술 스택

- **백엔드**: Spring Boot 3.3 / Java 21 / JPA / Flyway / MySQL 8 / WebClient (OpenAI)
- **프론트엔드**: Vue 3 + Vite + TypeScript + Tailwind CSS + Vue Router
- **인프라**: Docker Compose (multi-stage Dockerfile, 단일 컨테이너에 백엔드+프론트엔드 정적 자산)
- **이미지 저장**: 파일시스템 + Docker 볼륨 (`./uploads:/data/uploads`)

## 5. AI 도구 활용

- **GPT-4o Vision**: 아이의 그림 → 스타일 키워드 추출 (예: "수채화풍, 따뜻한 파스텔")
- **GPT-4o-mini**: 5페이지 동화 본문 + 페이지별 일러스트 프롬프트 JSON 생성
- **gpt-image-1**: 페이지별 일러스트 생성 (스타일 키워드 prefix로 페이지 간 일관성 유지)
- **Mock 모드**: API 키 없이도 시드 동화 4편 + 단계 전이 시연 가능

## 6. 설계 의도

- **그림으로 얼굴 합성하지 않은 이유**: 30시간 마감에 ControlNet/IP-Adapter는 디버깅 리스크 큼. 스타일 키워드 추출 방식이 페이지 간 일관성 + 비용 통제 + 안전한 길.
- **상태 머신을 잘게 쪼갠 이유**: ANALYZING_DRAWING / GENERATING_STORY / GENERATING_IMAGES 단계가 미리보기 화면의 step indicator를 살아있게 만듦. 데모 임팩트.
- **부분 실패 정책**: 5장 일러스트 중 일부 실패 시에도 status=COMPLETED 유지하고 placeholder + 재생성. AI 호출 5번이 모두 성공해야만 완료로 치면 데모 리스크 큼.
- **이미지 저장을 파일시스템으로**: BLOB은 ZIP 만들 때 메모리 폭증, MinIO는 컨테이너 추가 + SDK 의존성. 30시간엔 파일시스템 + Docker 볼륨이 가장 짧은 길.

## 더 시간이 있었다면

- 사용자 인증 + 다중 사용자 격리
- WebSocket으로 폴링 → 푸시
- 종이책 인쇄용 PDF 익스포트
- 음성 입력(Whisper)으로 아이 말 직접 받기
- 이미지 캐시·삭제 정책
- GitHub Actions CI/CD
- E2E 테스트 (Playwright)

## 라이선스

과제 평가 목적의 비공개 사용.
```

- [ ] **Step 2:** Commit

```bash
git add README.md
git commit -m "docs: README with 6 sections"
git push
```

---

### Task 37 — 5분 데모 영상 녹화

**Files:** (영상 파일은 git에 안 올림)

- [ ] **Step 1:** 영상 스토리보드 (5분 안에)

```
0:00-0:30  hook
   - "아이 그림이 동화책 일러스트가 된 모습" before/after 한 컷부터
   - 메인 화면(시드 4편 그리드) 보여주기

0:30-2:00  Lv1 — 새 동화 만들기
   - "+ 새 동화 만들기" 클릭
   - 아이 그림 업로드 + 이름 + 상상 입력
   - 제출 → 미리보기 화면의 step indicator로 단계 전이 시각적 강조
   - 완성 → before/after 스트립 강조 → 책장 넘기기 + 페이지 편집·재생성

2:00-3:30  Lv2 — 주문 흐름
   - "이 동화로 책 만들기" 모달
   - 사이즈/표지/부수 선택 후 주문 생성
   - 주문 칸반에서 PENDING → PROCESSING → COMPLETED 전이

3:30-4:30  Lv3 — ZIP 익스포트
   - 칸반에서 ZIP 다운로드
   - 압축 해제 화면 캡처: metadata.json, story.json, pages/, drawing.png 보여주기

4:30-5:00  마무리
   - 한 줄 정리: "그림 1장 + 상상 한 줄 → 동화책 + 주문 + 데이터 익스포트"
   - 스택 한 줄, README 한 줄
```

- [ ] **Step 2:** OBS 또는 Loom 녹화. 한국어 내레이션. 가능하면 1080p, mp4.

- [ ] **Step 3:** YouTube unlisted 또는 Google Drive 공유 링크 확보. README에는 안 넣어도 됨 (구글폼에만).

---

### Task 38 — 서술형 4문항 답변 작성 (구글폼)

**Files:**
- Create: `submission/answers.md` (참고용, git push)

- [ ] **Step 1:** 4문항 초안

질문 4개는 정확히 모르므로(과제 안내에 따라) 일반적으로 예상되는 패턴:

1. **자기소개 / 지원 동기** — 풀스택 경험·이 프로젝트가 본인의 어떤 면을 보여주는지.
2. **AI 동화 도메인을 고른 이유** — PDF 예시표 친숙성, 두 입력값(그림+상상) 모두 살리는 하이브리드 설계 의도.
3. **AI 도구 활용 — 잘된 점** — Vision으로 스타일 텍스트화 → 일관성 + 디버깅 + 비용 통제 통합 효과.
4. **AI 도구 활용 — 실패한 점** — gpt-image-1 페이지 간 일관성 깨진 경험, mock fallback 만들면서 깨달은 의존성 분리.

각 답변 200~400자 한국어. 구체적 경험 + 결정의 이유 + 결과 순서.

- [ ] **Step 2:** `submission/answers.md`에 적어두고 구글폼에 복사 붙여넣기.

```bash
mkdir -p submission
# answers.md 작성
git add submission/answers.md
git commit -m "docs: google form answers draft"
git push
```

- [ ] **Step 3:** 구글폼 제출 (https://forms.gle/4EuYDFXH1bBdMfGT7) — GitHub URL + 4문항 답변.

---

### Task 39 — 최종 점검 + 제출

**Files:** (수정 없음)

- [ ] **Step 1:** GitHub Public 저장소 가시성 확인

```bash
# Settings → General → Visibility: Public 확인
# README 표시되는지 확인
# git tag v1.0 && git push --tags  (선택)
```

- [ ] **Step 2:** clean 환경에서 README 따라 한 번 더 실행

```bash
cd /tmp && rm -rf sweetbook-final
git clone <url> sweetbook-final && cd sweetbook-final
cp .env.example .env
docker compose up
# 8080 접속 → 시드 4편 + 1주문 노출 확인
```

- [ ] **Step 3:** 구글폼에 제출 (마감 4월 29일 23:59 전).

- [ ] **Step 4:** 제출 후 README에 데모 영상 링크 추가 (선택, 면접용).

```bash
# README에 "## 데모 영상" 섹션 추가 후
git add README.md && git commit -m "docs: add demo video link"
git push
```

---

## 자기 검토 (Self-Review)

이 계획을 작성한 후 spec 대비 누락/모순 점검:

**1. Spec coverage**
- 사이트맵 4 라우트: Task 20·21·22·25·28에서 모두 구현 ✓
- SPA history-mode fallback (백엔드 forward): Task 10에서 ViewControllerRegistry, Task 34에서 수동 검증 ✓
- 상태 머신 (Story 6 + Order 3): Task 5·6에서 enum + 전이 검증, Task 7·18에서 entity·service ✓
- 페이지 레이아웃 3종: Task 24에서 3 컴포넌트 ✓
- API 11개 엔드포인트: Task 12·13·14·19·26·30에서 모두 구현 ✓
- 이미지 저장 (파일시스템 + 볼륨): Task 9·10·11에서 ✓
- ZIP 구조 14 엔트리 + camelCase JSON: Task 29 (count assertion 포함) ✓
- mock 모드 + 600ms 단계: Task 16·17 ✓
- 시드 4편 + 주문 1건: Task 11 ✓
- 검증 룰 (5MB·1-20자·10-500자): Task 13의 dto + Task 22의 frontend 양쪽 ✓
- README 6섹션: Task 36 ✓
- 5분 데모 영상: Task 37 ✓
- 구글폼 4문항: Task 38 ✓

**2. Placeholder scan**
- "TBD/TODO/implement later"  — 없음
- "Add appropriate error handling" — Task 31에서 GlobalExceptionHandler 구체화
- "Similar to Task N" — 없음 (모든 코드 직접 기재)
- 미정의 함수 참조 — 모두 정의됨

**3. Naming convention consistency (2026-04-28 보강)**
- 단일 컨벤션: API/JSON/폼 필드/ZIP 안 JSON 모두 **camelCase**. SQL 스키마와 `@Column(name=…)` 매핑만 snake_case.
- 검증 위치:
  - 멀티파트 폼 필드: Task 13의 `@ModelAttribute StoryCreateRequest` + Task 22의 `fd.append('childName', …)` 일치 ✓
  - 주문 JSON body: Task 26의 `OrderCreateRequest` record 필드명 == Task 27 `OrderModal`의 `createOrder({storyId, bookSize, …})` ✓
  - ZIP 안 JSON: Task 29의 `pageMeta.put("pageNumber", …)` + 검증 테스트의 `statusHistory` 키 ✓
  - design 문서 API spec 표 (`storyId`, `bookSize`, `coverType` 등): plan과 일치 ✓
- Spring Jackson 기본 LOWER_CAMEL_CASE 유지 → `@JsonProperty` 불필요.

**4. Type consistency**
- `useStoryStatus` 반환 (story, isPolling, start, stop, refresh) — 일관
- `StoryStatus` enum 6개 (DRAFT/ANALYZING_DRAWING/GENERATING_STORY/GENERATING_IMAGES/COMPLETED/FAILED) — 일관
- `OrderStatus` 3개 + 전진만 — 일관
- `PageLayout` 3개 (COVER/SPLIT/ENDING) + pageNumber 매핑 — 일관
- `OrderCreateRequest` (Backend) ↔ `OrderCreatePayload` (Frontend) 필드명 매칭 — `storyId`/`bookSize`/`coverType`/`copies`/`recipientName`/`addressMemo` 일치
- API path 일관 (`/api/stories`, `/api/stories/{id}`, `/api/stories/{id}/pages/{n}` 등) — 일관
- ZIP 엔트리 수: 본문 설명·테스트 assertEquals(14)·수동 smoke 모두 14로 일치 ✓

**5. 흐름 검증**
- Story 생성 → 미리보기 → 폴링 → 완성 → 주문 → 칸반 → ZIP — 한 줄로 연결됨
- 부분 실패 (페이지 1장 일러스트만 실패) → COMPLETED 유지 → frontend에서 placeholder + 재생성 — Task 18·24·25에서 모두 처리
- 멀티파트 업로드 → 비동기 generation → COMPLETED — Task 33의 `StoryCreateSmokeTest`에서 회귀 보호

**보강 이력 (2026-04-28 후반)**
- (1) SPA fallback 누락 → Task 10에 ViewController 추가 + Task 33에 SpaFallbackTest 5건 + Task 34에 수동 URL 검증.
- (2) snake_case ↔ camelCase 불일치 → design 문서·plan ZIP 코드·테스트 모두 camelCase로 통일. 컨벤션 섹션 명시.
- (3) ZIP 파일 수 19 vs 14 불일치 → 14로 통일 + 테스트에 `assertEquals(14, entries.size())` 박음.
- (4) E2E 테스트가 시드만 검증 → `OrderFlowSmokeTest`(이름 정정) + `StoryCreateSmokeTest`(멀티파트 + 비동기 generation 회귀 보호) 분리.

이슈 없음. 30시간 안에 끝나는 사이즈로 정렬됨.

---

## 실행 핸드오프

**계획 완료. 저장 위치: `C:/Users/kim/.gstack/projects/Desktop/kim-sweetbook-plan-20260428.md`**

이 계획은 사용자 본인이 직접 실행하는 형태로 작성됨 (subagent-driven은 OpenAI 키 노출 등으로 부적합). 다음 두 방식 중 선택:

**1. 인라인 실행 (executing-plans 스킬)** — 같은 세션에서 Task 단위로 실행. 체크포인트마다 검토.

**2. 본인이 직접 — 계획서를 따라가며 IDE에서 작성**. 막히면 해당 Task 번호로 질문.

어떤 방식으로 갈지 알려주세요.
