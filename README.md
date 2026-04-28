# 📚 Sweetbook

스위트북 채용 과제 — AI 동화 서비스. 자세한 내용은 Phase 9에서 README가 보강됩니다.

## 빠른 실행 (Docker)

```bash
cp .env.example .env
docker compose up
```

http://localhost:8080

## 개발 환경

- 백엔드: `cd backend && ./mvnw spring-boot:run`
- 프론트엔드: `cd frontend && npm install && npm run dev` → http://localhost:5173

## 설계 / 계획 문서

- 설계: [`docs/kim-sweetbook-design-20260428.md`](docs/kim-sweetbook-design-20260428.md)
- 구현 계획: [`docs/kim-sweetbook-plan-20260428.md`](docs/kim-sweetbook-plan-20260428.md)
