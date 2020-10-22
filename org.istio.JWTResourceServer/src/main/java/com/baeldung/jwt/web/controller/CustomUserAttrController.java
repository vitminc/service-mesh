package com.baeldung.jwt.web.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomUserAttrController {

	@GetMapping("/user/info/custom")
	public Map<String, Object> getUserInfo(@AuthenticationPrincipal Jwt principal) {
		return Collections.singletonMap("DOB", principal.getClaimAsString("DOB"));
	}
}