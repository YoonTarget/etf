# 진격의 ETF

💻 **Spring Boot + JPA 기반 ETF 추천 및 비교 웹 플랫폼**

이 프로젝트는 투자자가 다양한 ETF 데이터를 효율적으로 조회·비교할 수 있도록 설계된 **백엔드 중심 애플리케이션**입니다.  
공공데이터포털 API를 활용해 ETF 데이터를 수집하고, Spring Batch로 자동화된 데이터 적재를 수행합니다.

---

## 🛠 Tech Stack

- **Language**: Java 17 (추후 Kotlin 마이그레이션 예정)
- **Framework**: Spring Boot
- **Database**: PostgreSQL
- **Persistence Layer**:
    - Spring Data JPA (`JpaRepository` 기반 CRUD)
    - `EntityManager` + JPQL 직접 제어 (성능 최적화 및 복잡 쿼리 처리)
- **Batch**: Spring Batch (공공데이터포털 ETF API → DB 저장)
- **Build Tool**: Gradle
- **Testing**: JUnit 5

---

## 📂 Project Architecture  
```plaintext
src/
├─ main/
│   ├─ java/com/newProject/etf/
│   │    ├─ batch/                  # Spring Batch 관련 처리 (데이터 적재, 정기 작업 등)
│   │    ├─ config/                 # 프로젝트 전반의 환경 설정 (Batch, JPA, Scheduler, Security 등)
│   │    ├─ controller/             # REST API 엔드포인트 (요청/응답 처리)
│   │    ├─ dto/                    # 데이터 전송 객체 (계층 간 데이터 교환)
│   │    ├─ entity/                 # JPA 엔티티 클래스 (DB 매핑)
│   │    ├─ listener/               # 엔티티 리스너 (엔티티 상태 변화 감지 및 처리)
│   │    ├─ repository/             # JPA Repository (CRUD 및 쿼리 메소드)
│   │    ├─ scheduler/              # 스케줄러 (정해진 주기 작업 실행)
│   │    └─ service/                # 서비스 계층 (비즈니스 로직 처리)
│   └─ resources/
│        ├─ docs/                   # API 및 프로젝트 문서
│        ├─ templates/              # Thymeleaf 등 서버 사이드 렌더링 템플릿
│        ├─ static/                 # 정적 리소스 (CSS, JS, 이미지 등)
│        └─ application.properties  # 환경 설정 파일
└─ test/                            # 단위 및 통합 테스트 (JUnit5)

```

---

## 🚀 Key Features (Technical)  

- **RESTful API 설계**  
  - ETF 리스트 조회, 상세 조회, 다중 비교 API  
  - 직관적이고 확장성 있는 URI 설계  

- **Database Integration**  
  - PostgreSQL 기반 ETF 가격·정보 저장소 구축  
  - Spring Data JPA를 활용한 표준 CRUD 처리  
  - `EntityManager` & JPQL을 통한 **성능 최적화/복잡한 조건 조회**  

- **Batch Processing**  
  - Spring Batch 기반 **청크 단위 처리**  
  - 공공데이터포털 API로부터 일별 ETF 종가 수집 및 저장  
  - 중복 키 예외 처리 → 스킵 처리 전략 적용  

- **Persistence 전략**  
  - 단순 조회/CRUD → `JpaRepository`  
  - 대용량 데이터 조회/특정 조건 최적화 → `EntityManager` + JPQL  
  - 필요 시 Native Query 활용  

- **테스트 자동화**  
  - JUnit 5 + Spring Boot Test  
  - Repository 레벨 & Service 레벨 단위 테스트  

---

## 📡 Example API Usage  

### 가장 최근 날짜의 모든 ETF 데이터 조회
```http
GET /etf/recent

[
  { "ticker": "SPY", "name": "SPDR S&P 500 ETF Trust", "closePrice": 512.34 },
  { "ticker": "QQQ", "name": "Invesco QQQ Trust", "closePrice": 427.55 }
]
```
