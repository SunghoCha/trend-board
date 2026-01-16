package com.sungho.trendboard.global.domain;

import lombok.Getter;

@Getter
public class CurrentUser {
    private final Long id;
    private final String name;
    private final String email;

    public CurrentUser(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}