package com.sungho.trendboard.global.domain;

import com.sungho.trendboard.domain.MemberRole;

public record CurrentUser(Long memberId, MemberRole role) {
}
