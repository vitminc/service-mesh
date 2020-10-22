package org.istio.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * This Live Test requires: - the Authorization Server and the Resource Server
 * to be running
 *
 */
public class CustomClaimLiveTest {

	private final String redirectUrl = "http://localhost:8084/";
	private final String authorizeUrlPattern = "http://localhost:8083/auth/realms/testapp/protocol/openid-connect/auth?response_type=code&client_id=jwtClient&scope=%s&redirect_uri="
			+ redirectUrl;
	private final String tokenUrl = "http://localhost:8083/auth/realms/testapp/protocol/openid-connect/token";
	private final String userInfoResourceUrl = "http://localhost:8081/jwt-resource-server/user/info/custom";

	@Test
	public void givenUserWithReadScope_whenGetUserInformationResource_thenSuccess() {
		String accessToken = obtainAccessToken("read");

		// Access resources using access token
		Response response = RestAssured.given().header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.get(userInfoResourceUrl);
		assertThat(response.as(Map.class)).containsEntry("DOB", "1984-07-01");
	}

	private String obtainAccessToken(String scopes) {
		// obtain authentication url with custom codes
		Response response = RestAssured.given().redirects().follow(false)
				.get(String.format(authorizeUrlPattern, scopes));
		String authSessionId = response.getCookie("AUTH_SESSION_ID");
		String kcPostAuthenticationUrl = response.asString().split("action=\"")[1].split("\"")[0].replace("&amp;", "&");

		// obtain authentication code and state
		response = RestAssured.given().redirects().follow(false).cookie("AUTH_SESSION_ID", authSessionId)
				.formParams("username", "john@test.com", "password", "123", "credentialId", "")
				.post(kcPostAuthenticationUrl);
		assertThat(HttpStatus.FOUND.value()).isEqualTo(response.getStatusCode());

		// extract authorization code
		String location = response.getHeader(HttpHeaders.LOCATION);
		String code = location.split("code=")[1].split("&")[0];

		// get access token
		Map<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "authorization_code");
		params.put("code", code);
		params.put("client_id", "jwtClient");
		params.put("redirect_uri", redirectUrl);
		params.put("client_secret", "jwtClientSecret");
		response = RestAssured.given().formParams(params).post(tokenUrl);
		return response.jsonPath().getString("access_token");
	}

}