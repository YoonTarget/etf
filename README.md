# 진격의 ETF

💻 **Spring Boot + JPA 기반 ETF 추천 및 비교 웹 플랫폼**

이 프로젝트는 국내 상장 미국 ETF 데이터를 효율적으로 조회·비교할 수 있도록 설계된 **백엔드 중심 애플리케이션**입니다.  

공공데이터포털 API를 통해 데이터를 수집하고, Spring Batch로 자동화된 데이터 적재를 수행합니다.

단순 데이터 제공을 넘어 **커뮤니티 + 게임적 요소**를 접목하여 투자자들이 재미있게 소통할 수 있는 플랫폼을 지향합니다.

---

## 🛠 기술스택

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

## 📂 프로젝트 구성도
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

## ✨ 주요 기능
- 📊 **ETF 데이터 조회 및 비교**
  - 공공데이터포털 금융상품 시세 API 활용
  - 전일 종가, 거래량 등 기본 데이터 제공
  - 종가·거래량 추이 시각화 및 ETF 간 비교

- 💬 **투자자 커뮤니티** (개발 예정)
  - 종목별 토론 게시판
  - 사용자 간 의견 교환 및 정보 공유

- 🏆 **게임적 요소 (Gamification)** (개발 예정)
  - ETF 수익률 예상 투표 기능
  - 정답자에게 포인트 지급 및 랭크 시스템 제공
  - 포인트 기반 명예 시스템 (투자 밈 활용)
  - 추후 제휴 혜택(쿠폰 등)과 연계 가능

---

## ⚡ 차별성
- 증권사 플랫폼처럼 **실시간 데이터 경쟁**에 집중하지 않음
- **기본 데이터 + 비교 분석 + 커뮤니티 + 재미 요소** 결합
- 투자 경험을 단순한 정보 소비가 아닌 **참여형 경험**으로 확장

---

## 🌱 향후 확장 방향
- 🤖 **AI 에이전트**를 통한 개인화된 ETF 추천
- 📊 투자자 성향 분석 및 맞춤형 콘텐츠 제공
- 🏛️ “ETF 투자자들의 놀이터” 같은 커뮤니티 플랫폼으로 성장

---

## 🚀 기술적 특성  

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

## 📡 API 사용 예시

### 가장 최근 날짜의 모든 ETF 데이터 조회
```http
GET /etf/recent

[
  {
      "basDt": "20250924",
      "srtnCd": "379800",
      "isinCd": "KR7379800006",
      "itmsNm": "KODEX 미국S&P500",
      "clpr": "21265",
      "vs": "-5",
      "fltRt": "-.02",
      "nav": "21231.91",
      "mkp": "21190",
      "hipr": "21265",
      "lopr": "21165",
      "trqu": "2413104",
      "trPrc": "51171879986",
      "mrktTotAmt": "5382171500000",
      "stLstgCnt": "253100000",
      "bssIdxIdxNm": "S&P 500",
      "bssIdxClpr": "6637.97",
      "nPptTotAmt": "5339825832851"
  },
  {
      "basDt": "20250924",
      "srtnCd": "379810",
      "isinCd": "KR7379810005",
      "itmsNm": "KODEX 미국나스닥100",
      "clpr": "22940",
      "vs": "-60",
      "fltRt": "-.26",
      "nav": "22901.68",
      "mkp": "22855",
      "hipr": "22940",
      "lopr": "22830",
      "trqu": "1288200",
      "trPrc": "29467738544",
      "mrktTotAmt": "3350387000000",
      "stLstgCnt": "146050000",
      "bssIdxIdxNm": "NASDAQ 100",
      "bssIdxClpr": "24503.57",
      "nPptTotAmt": "3340209415021"
  },
  ...
]
```
