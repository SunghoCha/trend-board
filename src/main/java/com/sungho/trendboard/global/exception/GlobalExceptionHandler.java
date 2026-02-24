package com.sungho.trendboard.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Domain/application layer의 의도된 예외는 공통 포맷으로 변환한다.
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex,
                                                           HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        String path = request.getRequestURI();
        ApiErrorResponse body = ApiErrorResponse.fromErrorCode(
                errorCode,
                List.of(),
                path
        );

        logBusinessException(errorCode, path, ex);

        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

    // @RequestBody + Bean Validation 실패(@Valid) 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
        List<ValidationErrorItem> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ValidationErrorItem(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        ApiErrorResponse body = ApiErrorResponse.fromErrorCode(
                errorCode,
                errors,
                request.getRequestURI()
        );
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

    // @Validated 기반 파라미터(@RequestParam/@PathVariable 등) 검증 실패 처리
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest request) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
        List<ValidationErrorItem> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> new ValidationErrorItem(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();

        ApiErrorResponse body = ApiErrorResponse.fromErrorCode(
                errorCode,
                errors,
                request.getRequestURI()
        );
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

    // 타입 변환 실패(ex: /posts/not-a-number)도 400 검증 오류로 정규화한다.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest request) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;
        List<ValidationErrorItem> errors = List.of(new ValidationErrorItem(
                ex.getName(),
                "요청 파라미터 타입이 올바르지 않습니다."
        ));

        ApiErrorResponse body = ApiErrorResponse.fromErrorCode(
                errorCode,
                errors,
                request.getRequestURI()
        );
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

    // 존재하지 않는 URL 요청(404) — 5xx 오해를 막기 위해 명시 처리
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex,
                                                                  HttpServletRequest request) {
        ErrorCode errorCode = CommonErrorCode.NOT_FOUND;
        String path = request.getRequestURI();
        log.info("[HandledException] type=NoResourceFoundException, path={}", path);

        ApiErrorResponse body = ApiErrorResponse.fromErrorCode(errorCode, List.of(), path);
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

    // JSON 파싱 실패를 500이 아닌 400으로 명시 처리한다.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT;

        ApiErrorResponse body = ApiErrorResponse.fromErrorCode(
                errorCode,
                List.of(),
                request.getRequestURI()
        );
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

    // 분기되지 않은 예외는 내부 오류로 표준화하고 상세 원인은 로그로만 남긴다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex,
                                                             HttpServletRequest request) {
        ErrorCode errorCode = CommonErrorCode.INTERNAL_ERROR;
        String path = request.getRequestURI();
        log.error("[UnhandledException] path={}", path, ex);

        ApiErrorResponse body = ApiErrorResponse.fromErrorCode(
                errorCode,
                List.of(),
                path
        );
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

    // 운영 로그 노이즈를 줄이기 위해 4xx는 info, 5xx는 error로 기록한다.
    private void logBusinessException(ErrorCode errorCode, String path, BusinessException ex) {
        if (errorCode.getStatus().is5xxServerError()) {
            log.error("[HandledException] type=BusinessException, code={}, status={}, path={}",
                    errorCode.getCode(),
                    errorCode.getStatus().value(),
                    path,
                    ex);
            return;
        }

        log.info("[HandledException] type=BusinessException, code={}, status={}, path={}",
                errorCode.getCode(),
                errorCode.getStatus().value(),
                path);
    }
}
