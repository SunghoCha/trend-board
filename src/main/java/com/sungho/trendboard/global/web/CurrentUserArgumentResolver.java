package com.sungho.trendboard.global.web;

import com.sungho.trendboard.domain.MemberRole;
import com.sungho.trendboard.global.domain.CurrentUser;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginUser.class);
        boolean isSupportedType = CurrentUser.class.equals(parameter.getParameterType());
        return hasAnnotation && isSupportedType;
    }

    @Override
    public @Nullable Object resolveArgument(MethodParameter parameter,
                                            @Nullable ModelAndViewContainer mavContainer,
                                            NativeWebRequest webRequest,
                                            @Nullable WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUser currentUser) {
            return currentUser;
        }

        // Phase 3에서 Spring Security + Redis 세션 방식으로 교체 예정
        boolean required = parameter.getParameterAnnotation(LoginUser.class).required();
        if (!required) {
            return null;
        }
        return new CurrentUser(1L, MemberRole.ADVERTISER);
    }
}
