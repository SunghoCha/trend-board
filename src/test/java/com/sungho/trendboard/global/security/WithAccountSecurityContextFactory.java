package com.sungho.trendboard.global.security;

import com.sungho.trendboard.global.domain.CurrentUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {

    @Override
    public SecurityContext createSecurityContext(WithAccount annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        CurrentUser principal = new CurrentUser(annotation.memberId(), annotation.role());
        String authority = "ROLE_" + annotation.role().name();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority(authority))
        );

        context.setAuthentication(authentication);
        return context;
    }
}
