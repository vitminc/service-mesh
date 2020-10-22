package org.istio.sts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@SpringBootApplication
@RestController
@EnableAuthorizationServer
@EnableResourceServer
public class StsApplication {

	@RequestMapping("/user")
	public Principal user(Principal user) {
		return user;
	}


	public static void main(String[] args) {
		SpringApplication.run(StsApplication.class, args);
	}

}
