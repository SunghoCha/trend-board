package com.sungho.trendboard.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sungho.trendboard.api.controller.PostController;
import com.sungho.trendboard.application.PostService;
import com.sungho.trendboard.domain.exception.PostErrorCode;
import com.sungho.trendboard.global.exception.BusinessException;
import com.sungho.trendboard.global.web.CurrentUserArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private PostService postService;

    @BeforeEach
    void setUp() {
        // Controller 단위에서 예외 매핑만 검증하기 위해 서비스는 목으로 고정한다.
        postService = Mockito.mock(PostService.class);
        PostController postController = new PostController(postService);

        // @Valid 검증이 동작하도록 standalone MockMvc에 Validator를 명시 등록한다.
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        // 실제 전역 예외 처리기와 커스텀 아규먼트 리졸버를 함께 연결해 API 레이어 동작을 재현한다.
        mockMvc = MockMvcBuilders.standaloneSetup(postController)
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void 게시글이_없으면_404_에러_응답을_반환한다() throws Exception {
        Mockito.when(postService.getPost(anyLong(), anyLong()))
                .thenThrow(new BusinessException(PostErrorCode.POST_NOT_FOUND));

        mockMvc.perform(get("/api/v1/posts/123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("P-001"))
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.path").value("/api/v1/posts/123"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void 유효하지_않은_요청이면_400_필드에러를_반환한다() throws Exception {
        String body = objectMapper.writeValueAsString(new CreatePostRequestFixture("", "content"));

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-VALIDATION"))
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].rejectedValue").doesNotExist())
                .andExpect(jsonPath("$.errors[0].reason").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/posts"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void 잘못된_JSON_요청이면_400_에러를_반환한다() throws Exception {
        // 닫히지 않은 JSON으로 파싱 실패를 유도한다.
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"a\","))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID-JSON"))
                .andExpect(jsonPath("$.message").value("요청 본문 JSON 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.path").value("/api/v1/posts"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void 경로변수_타입이_잘못되면_400_필드에러를_반환한다() throws Exception {
        // Long postId 자리에 문자열을 넣어 타입 변환 실패를 유도한다.
        mockMvc.perform(get("/api/v1/posts/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-VALIDATION"))
                .andExpect(jsonPath("$.errors[0].field").value("postId"))
                .andExpect(jsonPath("$.errors[0].reason").value("요청 파라미터 타입이 올바르지 않습니다."))
                .andExpect(jsonPath("$.path").value("/api/v1/posts/not-a-number"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void 예상하지_못한_예외면_500_공통메시지를_반환한다() throws Exception {
        Mockito.when(postService.getPost(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/v1/posts/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("COMMON-500"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.path").value("/api/v1/posts/1"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void 도메인에러의_fieldErrors는_빈배열이다() throws Exception {
        Mockito.when(postService.getPost(anyLong(), anyLong()))
                .thenThrow(new BusinessException(PostErrorCode.POST_NOT_FOUND));

        mockMvc.perform(get("/api/v1/posts/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    record CreatePostRequestFixture(String title, String content) {
    }
}
