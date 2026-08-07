## 트러블슈팅: Spring Boot 4에서 @WebMvcTest + Spring Security 인증 주입 실패

### 배경
대화방에 사용자별 데이터 격리를 도입하면서 컨트롤러가 `Authentication`
파라미터로 현재 로그인 사용자의 email을 받도록 변경했다. 이에 맞춰
컨트롤러 테스트(@WebMvcTest)도 인증된 사용자를 흉내내도록 수정하는
과정에서 여러 단계의 문제를 겪었다. Spring Boot 4 + Spring Security
조합에서 테스트 슬라이스의 동작이 이전 버전과 달라진 것이 원인이었다.

### 증상
컨트롤러 테스트 실행 시 다음 예외로 실패했다.

    NullPointerException: Cannot invoke
    "Authentication.getName()" because "authentication" is null

컨트롤러의 `Authentication` 파라미터가 null로 들어와, `getName()` 호출에서
터졌다. 인증을 흉내내는 설정이 컨트롤러까지 전달되지 않은 것이다.

### 원인 추적 (시도와 실패의 연속)

1. `@WithMockUser` 사용
    - 어노테이션으로 인증을 흉내냈으나 `authentication`이 여전히 null.
    - Spring Boot 4의 @WebMvcTest가 커스텀 SecurityConfig를 자동으로
      로드하지 않아, Security 필터 체인이 요청에 적용되지 않았다.
      (필터 로그에 Spring Security 관련 필터가 없었다.)

2. `.with(user(...))` 요청 포스트프로세서 사용
    - SecurityContext에 사용자를 직접 심는 방식으로 바꿨으나 동일하게 null.
    - 역시 Security 필터가 태워지지 않아, 심은 SecurityContext가 컨트롤러
      파라미터로 연결되지 않았다.

3. `@Import(SecurityConfig.class)` 추가
    - SecurityConfig를 명시적으로 로드했더니 이번엔 컨텍스트 로딩 자체가 실패.
      `securityFilterChain(HttpSecurity http)`가 요구하는 HttpSecurity Bean이
      테스트 컨텍스트에 없었다. (@WebMvcTest가 Security 자동설정을 로드하지
      않으므로 HttpSecurity가 생성되지 않음.)
    - SecurityConfig를 빼면 인증 주입이 안 되고, 넣으면 HttpSecurity가 없어
      실패하는 순환 상태.

4. `@AutoConfigureMockMvc(addFilters = false)` 추가
    - 필터를 꺼서 컨텍스트 로딩은 통과했으나 다시 `authentication`이 null.
    - 필터를 끄면 SecurityContext를 요청 파이프라인에 연결하는 필터도 함께
      꺼져, `.with(user(...))`가 심은 인증이 컨트롤러로 전달되지 않는다.
      즉 `addFilters=false`와 `.with(user())`는 상성이 맞지 않는다.

### 최종 해결: @SpringBootTest + @AutoConfigureMockMvc
슬라이스 테스트(@WebMvcTest)에서 Security 통합을 매끄럽게 구성하는 대신,
전체 애플리케이션 컨텍스트를 로드하는 방식으로 전환했다.

    @SpringBootTest
    @AutoConfigureMockMvc
    class ConversationControllerTest {
        @Autowired MockMvc mockMvc;
        @MockitoBean ConversationService conversationService;
        @MockitoBean ChatMemory chatMemory;
        // 각 요청에 .with(user("..."))로 인증 주입
        // 변경 요청(POST/DELETE/PATCH)에는 .with(csrf()) 추가
    }

- 전체 컨텍스트를 로드하므로 SecurityConfig, JWT 필터, HttpSecurity가 모두
  정상 구성된다. 따라서 `.with(user(...))`가 심은 인증이 실제 Security
  필터를 거쳐 컨트롤러의 `Authentication` 파라미터로 전달된다.
- 외부 의존(DB 접근, AI 호출)은 Service와 ChatMemory를 @MockitoBean으로
  대체해 차단했다.
- 슬라이스 테스트의 가벼움은 포기했지만, 인증까지 실제 필터로 검증하는
  통합 테스트가 되어 오히려 격리 로직 검증에 부합한다.

### 교훈
- Spring Boot 4에서 @WebMvcTest는 커스텀 SecurityConfig를 자동 로드하지
  않는다. 인증이 얽힌 컨트롤러 테스트는 @SpringBootTest +
  @AutoConfigureMockMvc가 더 안정적이다.
- `.with(user(...))`(또는 @WithMockUser)는 Security 필터 체인이 살아 있어야
  동작한다. `addFilters=false`로 필터를 끄면 함께 무력화된다.
- 참고: 이 과정에서 사용한 테스트 슬라이스/오버라이드 API는 Spring Boot 4에서
  패키지가 재배치되었다 (@MockitoBean =
  org.springframework.test.context.bean.override.mockito.MockitoBean,
  @WebMvcTest / @AutoConfigureMockMvc =
  org.springframework.boot.webmvc.test.autoconfigure.*).