package com.example.hallyuclass.auth.model;

import java.io.Serializable;

public enum Roles implements Serializable {
    USER(new String[] { "USER" }),
    ADMIN(new String[] { "USER", "ADMIN" });

    private final String[] authorities;

    Roles(String[] authorities) {
        this.authorities = authorities;
    }

    public String[] getAuthorities() {
        return authorities;
    }
}
