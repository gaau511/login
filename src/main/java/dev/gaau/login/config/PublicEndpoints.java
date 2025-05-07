package dev.gaau.login.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PublicEndpoints {
    public static final List<String> ENDPOINTS = new ArrayList<>(Arrays.asList(
            "/", "/error",
            "/*/auth/signUp", "/*/auth/login", "/*/auth/refreshToken",
            "/swagger-ui/**", "/v3/api-docs/**"
    ));
}
