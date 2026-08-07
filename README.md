# whybuychat — AI 채팅 서비스

## 1. 프로젝트 개요

로컬/오픈소스 기반으로 구축한 멀티 대화방 AI 채팅 서비스다. 사용자가 여러 개의 대화방을 만들어 AI와 대화할 수 있으며, 각 대화방은 이전 맥락을 기억하고 대화 내용이 DB에 영구 저장된다. 답변은 스트리밍으로 실시간 출력되고, 프론트는 모바일 메신저 형태의 채팅 UI를 제공한다.

핵심 기능:
- AI 대화 (멀티 provider 구조로 모델을 용도별로 교체 가능하게 설계)
- 대화 맥락 기억 (conversationId 단위)
- 스트리밍 응답 (SSE)
- 대화방 CRUD (생성 / 목록 / 이름 변경 / 삭제)
- 대화방 CRUD (생성 / 목록 / 이름 변경 / 삭제)
- 대화방별 지난 대화 복원
- 대화 내용 DB 영구 저장

개발은 기능을 한 번에 만들지 않고 단위별로 쌓아 올리며 각 단계를 검증하고 커밋을 나누는 방식으로 진행했다.

## 2. 기술 스택

- 언어 / 런타임: Java 21
- 프레임워크: Spring Boot 4.1, Spring MVC
- AI 연동: Spring AI 2.0, Ollama (llama3.1, 로컬 실행)
- 데이터 접근: Spring Data JPA (Hibernate)
- 데이터베이스: PostgreSQL (운영), H2 (학습·테스트)
- 컨테이너: Docker, docker-compose
- CI: GitHub Actions
- 테스트: JUnit 5, Mockito, MockMvc
- 프론트엔드: HTML/CSS/JavaScript (SSE EventSource 기반 채팅 UI)
- 형상관리: Git / GitHub

## 3. 기술 스택 선택 이유

**Spring Boot 4 + Java 21**
최신 버전을 선택해 최신 스펙을 경험하고, 이후 버전 마이그레이션 이슈를 직접 다뤄보기 위함. 최신이라 참고 자료가 적어 공식 문서 기반으로 해결하는 훈련이 됨.

**Spring AI**
Spring 공식 AI 추상화 계층. ChatClient / ChatMemory 같은 추상화를 제공해 특정 AI provider에 종속되지 않고, provider를 Bean으로 분리해 용도별(추론/연산)로 교체·라우팅할 수 있는 구조를 잡기 위해 선택.

**Ollama (로컬 LLM)**
개발·학습 단계에서 API 비용 없이 실제 AI 응답을 받기 위해 선택. 유료 API(Claude 등)는 클라우드 배포 시점에 붙이는 것으로 분리.

**JPA**
SQL을 직접 작성하지 않고 자바 객체로 DB를 다루기 위해 선택. 엔티티·리포지토리 설계와 메서드 이름 기반 쿼리(파생 쿼리)를 직접 구성.

**H2 → PostgreSQL (단계적 도입)**
먼저 설치가 필요 없는 H2로 "DB 저장" 개념과 흐름을 익힌 뒤, 실무용 RDBMS인 PostgreSQL로 전환. 저장소를 교체해도 상위 코드가 바뀌지 않도록 추상화(ChatMemory)를 활용해, DB 종류를 설정만으로 바꿀 수 있게 함.

**Docker / docker-compose**
DB를 로컬에 직접 설치하지 않고 컨테이너로 관리하기 위해 도입. 이후 앱 자체도 이미지화해 "어디서 실행해도 동일하게 동작"하도록 만들어 클라우드 배포의 전제를 갖춤. compose로 앱·DB를 묶어 컨테이너 간 통신을 구성.

**GitHub Actions (CI)**
이미 사용 중인 GitHub에 별도 서비스 없이 바로 얹을 수 있어 선택. push 시 자동 빌드·테스트로 코드 품질을 지속적으로 검증.

**Spring MVC (WebFlux 아님)**
구조가 단순한 MVC로 시작하되, 스트리밍이 필요한 엔드포인트만 Flux 반환으로 처리. 전체 스택을 리액티브로 전환하지 않고 필요한 지점만 스트리밍을 적용하는 방식.

## 4. 특이점

**멀티 provider 라우팅 구조**
AI 모델을 하나로 고정하지 않고, ChatModel Bean을 provider별로 분리해 용도(추론/빠른 연산)에 따라 다른 모델을 쓸 수 있도록 설계. 자동 주입이 모호해지는 문제를 @Qualifier로 이름을 붙여 해결.(💡작업예정)

**저장소 추상화를 활용한 무중단 DB 전환**
대화 기억을 InMemory → JDBC(H2) → PostgreSQL로 바꾸는 과정에서, ChatMemory 추상화 덕분에 저장소만 교체하고 상위 로직(컨트롤러·어드바이저)은 손대지 않음. DB 전환을 설정 변경만으로 처리.

**계층별 테스트 전략**
Service는 단위 테스트(Mockito), Controller는 웹 계층 테스트(@WebMvcTest + MockMvc), Repository는 DB 테스트(@DataJpaTest + H2)로 각 계층을 알맞은 방식으로 검증. CI에 연결해 회귀를 자동으로 방지.

**MVC 위에서의 SSE 스트리밍**
전체를 WebFlux로 바꾸지 않고, MVC에서 Flux 반환으로 특정 엔드포인트만 스트리밍 처리. 클라이언트는 EventSource로 조각을 누적해 렌더링.

**점진적 개발 + 기능 단위 커밋**
한 번에 완성하지 않고 기능을 작은 단위로 쌓으며 매 단계 동작을 확인하고 커밋을 분리. 커밋 메시지에 타입 접두사(feat/fix/refactor/test/ci/build/chore)를 적용해 히스토리를 정리.

## 5. 만들면서 마주한 문제와 해결

**멀티 provider Bean 충돌**
여러 AI provider starter를 함께 두자 ChatModel Bean이 여러 개가 되어 ChatClient 자동 구성이 어떤 Bean을 쓸지 모호해져 앱이 시작되지 않음.
→ 자동 구성에 의존하지 않고, ChatModel을 @Qualifier로 지정해 provider별 ChatClient Bean을 명시적으로 구성.

**로컬 GPU에서 모델 로딩 실패 (CUDA 오류)**
Ollama가 GPU를 잡으려 했으나, 그래픽 카드의 VRAM 부족과 드라이버-툴체인 불일치로 모델 로딩 중 프로세스가 종료됨.
→ 스프링이 아닌 로컬 환경 문제임을 격리해 확인(터미널에서 직접 실행해도 동일 오류)한 뒤, GPU 사용을 끄고 CPU 모드로 강제하도록 환경변수를 시스템에 영구 등록해 해결.

**스트리밍 응답의 한글 인코딩 / 공백 처리**
브라우저 주소창에서 SSE 스트림을 직접 열면 한글 멀티바이트 조각이 깨져 보이고, SSE 규격상 data 뒤 첫 공백이 잘려 단어가 붙어 보임.
→ 서버 응답에 UTF-8 charset을 명시하고, 실제 사용 방식대로 클라이언트(EventSource)에서 조각을 누적(answer += data)해 렌더링하도록 처리. 브라우저 raw 확인이 아닌 실제 프론트로 검증.

**컨테이너 간 네트워크 (localhost 문제)**
앱을 컨테이너로 띄우자 localhost:5432로 PostgreSQL을 찾지 못함. 컨테이너 안의 localhost는 자기 자신을 가리키기 때문.
→ docker-compose로 앱·DB를 묶고, DB 주소를 localhost가 아닌 서비스 이름(postgres)으로 지정해 컨테이너 간 통신을 구성. 호스트에서 실행 중인 Ollama는 host.docker.internal로 연결.

**데이터 정합성 — 고아 데이터**
대화방 삭제 기능을 나중에 보강하기 전에 삭제했던 방들의 메시지가 DB에 남아, 방(부모)은 없는데 메시지(자식)만 존재하는 상태가 발생.
→ DB를 직접 조회해 원인을 확인하고, 부모가 없는 메시지만 골라 정리하는 쿼리로 해결. 이후 삭제 로직에 @Transactional을 적용해 방·메시지 삭제가 하나의 트랜잭션으로 묶여 함께 성공하거나 함께 롤백되도록 개선.

**Spring Boot 4 마이그레이션 이슈**
최신 버전이라 대부분의 자료(3 기준)와 API가 달라 여러 지점에서 컴파일 실패.
→ 공식 문서를 근거로 다음을 해결:
- 테스트 Mock 어노테이션 @MockBean 제거 → @MockitoBean으로 대체
- 테스트 슬라이스가 별도 스타터로 모듈화됨 → spring-boot-starter-webmvc-test, spring-boot-starter-data-jpa-test를 추가
- 테스트 슬라이스 패키지 재배치 → WebMvcTest, DataJpaTest의 import 경로를 신규 위치로 변경