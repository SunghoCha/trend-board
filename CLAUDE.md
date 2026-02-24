# DealSpot 백엔드

광고주가 홍보 게시글과 쿠폰을 등록하고, 유저가 커뮤니티 활동으로 포인트를 적립해 쿠폰을 획득하는 홍보 커뮤니티 플랫폼.

## 핵심 문서

- **PRD**: `back-prd-output.md` — 기능 명세, API, DB 스키마, 서비스 계층 설계
- **로드맵**: `ROADMAP.md` — Phase별 Task 구성 및 진행 현황

## 기술 스택

- **Java 17** + **Spring Boot 4.0.1**
- **Spring Data JPA** + **QueryDSL 5.0.0 (jakarta)**
- **MySQL 8.0** — 메인 DB
- **Flyway** — DB 마이그레이션 (`src/main/resources/db/migration/`)
- **Redis** — 세션 저장소 (Spring Session), 선착순 쿠폰 재고, 추첨 응모자 Set
- **Kafka** — 선착순 쿠폰 발급 요청 버퍼링
- **Docker Compose** — 로컬 인프라 (`docker-compose.yml`)
- **Testcontainers** — 통합 테스트
- **Springdoc OpenAPI 3.0.1** — Swagger UI

## 패키지 구조

```
com.sungho.trendboard/
├── api/controller/         # REST 컨트롤러
├── application/            # 서비스 계층 (비즈니스 로직)
├── domain/                 # JPA 엔티티, enum
├── global/
│   ├── config/             # QueryDslConfig, SchedulingConfig, RedisConfig(Phase 3), KafkaConfig(Phase 3)
│   ├── exception/          # ErrorCode, BusinessException, GlobalExceptionHandler
│   ├── security/           # SecurityConfig, JwtAuthenticationFilter (Phase 3)
│   ├── util/
│   └── web/                # LoginUser 어노테이션, CurrentUser record, CurrentUserArgumentResolver
└── infra/
    ├── repository/         # JpaRepository, QueryDSL Custom
    ├── redis/              # CouponStockRedisRepository, RaffleApplicantRedisRepository (Phase 3)
    └── kafka/              # CouponIssueProducer, CouponIssueConsumer (Phase 3)
```

## 개발 방법론

### 기반 우선 접근법 (Foundation-First)

DB 스키마 → JPA 엔티티 → Repository → Service → Controller 순서로 구축.

### 유연한 설계 원칙 (Flexible Design)

인프라는 도메인 로직이 검증된 후 단계적으로 도입한다.

- **Phase 2**: Spring Security / Redis / Kafka 없이 순수 DB 기반으로 전체 도메인 로직 동작 검증
- **Phase 3**: Spring Security + Redis(세션 저장소 + 쿠폰 재고) + Kafka 순서로 도입
- 인프라 교체 시 **도메인 서비스와 컨트롤러는 수정하지 않는다**

### 인증 경계

- Phase 2: `CurrentUserArgumentResolver`가 `X-Member-Id`, `X-Member-Role` 헤더를 직접 읽어 `CurrentUser` 주입 (테스트 편의)
- Phase 3: Spring Security + Redis 세션 방식으로 교체 (`CurrentUserArgumentResolver`만 수정, 컨트롤러 무변경)

## 테스트 전략

- **컨트롤러 단위**: `@WebMvcTest` + MockMvc
- **서비스/통합**: `@SpringBootTest` + Testcontainers MySQL
- **외부 시스템**: Testcontainers Redis / Kafka (Phase 3)
- **동시성**: `ExecutorService` + `CountDownLatch`

## 에러 처리 규칙

### ErrorCode 정의

- 도메인별 enum 파일로 분리한다 (`PostErrorCode`, `MemberErrorCode` 등)
- 모두 `ErrorCode` 인터페이스를 구현한다
- code 문자열 규칙: 공통은 `COMMON-{의미}`, 도메인은 `{접두사}-{3자리 순번}` (예: `P-001`, `M-001`)
- 공통 에러는 `CommonErrorCode`에만 정의한다

### 예외 throw

- `BusinessException`을 직접 throw한다. 구체 예외 클래스(`XxxNotFoundException` 등)를 만들지 않는다
  ```java
  throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
  ```
- throw 전에 필요한 context는 서비스에서 직접 로그로 남긴다
  ```java
  log.warn("게시글 없음: postId={}", postId);
  throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
  ```

### API 에러 응답

- 클라이언트 응답에 내부 context(DB ID, 상태값 등)를 포함하지 않는다
- `errors`는 항상 포함한다. 비즈니스 에러는 빈 리스트, validation 에러는 필드별 목록
- 응답 필드 고정: `status`, `code`, `message`, `errors`, `path`, `timestamp`

### 로그 (ELK 준비)

- context를 `Map<String, Object>`로 묶어서 찍지 않는다 (ELK nested object 검색 불가)
- 개별 인자로 찍는다. 추후 `StructuredArguments.kv()`로 전환 예정
  ```java
  // 금지
  log.info("..., details={}", map);
  // 권장
  log.info("...: postId={}, userId={}", postId, userId);
  ```
- 4xx는 `log.info`, 5xx는 `log.error`
- MDC는 Filter 전용 영역이다. 비즈니스/서비스 코드에서 `MDC.put()`을 직접 호출하지 않는다

## 환경 프로파일

| 프로파일 | 용도 |
|---------|------|
| `local` | 로컬 개발 (Docker Compose DB 연결) |
| `dev` | 개발 서버 |
| `prod` | 운영 서버 |
| `test` | Testcontainers 자동 실행 |
