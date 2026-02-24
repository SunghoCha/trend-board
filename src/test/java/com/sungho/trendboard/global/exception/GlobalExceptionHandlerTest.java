package com.sungho.trendboard.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sungho.trendboard.global.web.CurrentUserArgumentResolver;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        // 예외 매핑만 검증하기 위한 가짜 컨트롤러를 사용한다.
        FakeController fakeController = new FakeController();

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(fakeController)
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void BusinessException이면_해당_상태코드와_에러코드를_반환한다() throws Exception {
        mockMvc.perform(get("/fake/business-error"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("COMMON-NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/fake/business-error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void 유효하지_않은_요청이면_400_필드에러를_반환한다() throws Exception {
        String body = objectMapper.writeValueAsString(new FakeRequest(""));

        mockMvc.perform(post("/fake/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].rejectedValue").doesNotExist())
                .andExpect(jsonPath("$.errors[0].reason").exists())
                .andExpect(jsonPath("$.path").value("/fake/validate"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void 잘못된_JSON_요청이면_400_에러를_반환한다() throws Exception {
        mockMvc.perform(post("/fake/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"a\","))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.path").value("/fake/validate"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void 경로변수_타입이_잘못되면_400_필드에러를_반환한다() throws Exception {
        mockMvc.perform(get("/fake/items/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("COMMON-INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("id"))
                .andExpect(jsonPath("$.errors[0].reason").value("요청 파라미터 타입이 올바르지 않습니다."))
                .andExpect(jsonPath("$.path").value("/fake/items/not-a-number"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void 예상하지_못한_예외면_500_공통메시지를_반환한다() throws Exception {
        mockMvc.perform(get("/fake/unexpected-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("COMMON-INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."))
                .andExpect(jsonPath("$.path").value("/fake/unexpected-error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void 도메인에러의_errors는_빈배열이다() throws Exception {
        mockMvc.perform(get("/fake/business-error"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    // 테스트 전용 가짜 컨트롤러
    @RestController
    @RequestMapping("/fake")
    static class FakeController {

        @GetMapping("/business-error")
        ResponseEntity<Void> businessError() {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }

        @PostMapping("/validate")
        ResponseEntity<Void> validate(@RequestBody @jakarta.validation.Valid FakeRequest request) {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/items/{id}")
        ResponseEntity<Void> getItem(@PathVariable Long id) {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/unexpected-error")
        ResponseEntity<Void> unexpectedError() {
            throw new RuntimeException("boom");
        }
    }

    record FakeRequest(@NotBlank String title) {
    }
}
