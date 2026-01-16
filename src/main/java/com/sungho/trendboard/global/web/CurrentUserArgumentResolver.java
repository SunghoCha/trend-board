package com.sungho.trendboard.global.web;

import com.sungho.trendboard.global.domain.CurrentUser;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginUser.class);
        boolean isSupportedType  = CurrentUser.class.equals(parameter.getParameterType());
        return hasAnnotation && isSupportedType;
    }

    @Override
    public @Nullable Object resolveArgument(MethodParameter parameter,
                                            @Nullable ModelAndViewContainer mavContainer,
                                            NativeWebRequest webRequest,
                                            @Nullable WebDataBinderFactory binderFactory) throws Exception {
        // TODO: 시큐리티 추가하면 바뀔 로직
        // required == true인데 없을때 예외 반환
        boolean required = parameter.getParameterAnnotation(LoginUser.class).required();
        return new CurrentUser(1L, "test", "test@example.com");
    }
}
