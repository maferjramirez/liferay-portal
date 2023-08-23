/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.delectable.bonsai;

import com.liferay.petra.string.StringBundler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONObject;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * @author Raymond Augé
 * @author Gregory Amerson
 * @author Brian Wing Shun Chan
 */
@RequestMapping("/object/action/accountsetup")
@RestController
public class ObjectActionAccountSetupRestController extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(
			@AuthenticationPrincipal Jwt jwt, @RequestBody String json)
		throws Exception {

		log(jwt, _log, json);

		JSONObject jsonObject = new JSONObject(json);

		JSONObject objectEntryDTODistributorApplicationJSONObject = jsonObject.getJSONObject(
			"objectEntryDTODistributorApplication");

		JSONObject propertiesJSONObject = objectEntryDTODistributorApplicationJSONObject.getJSONObject(
			"properties");

		String accountName = propertiesJSONObject.getString("businessName");

		String accountERC =
			"ACCOUNT_" +
				accountName.toUpperCase(
				).replace(
					" ", "_"
				);

		String email = propertiesJSONObject.getString("applicantEmail");

		WebClient.Builder builder = WebClient.builder();

		WebClient webClient = builder.baseUrl(
			lxcDXPServerProtocol + "://" + lxcDXPMainDomain
		).defaultHeader(
			HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE
		).defaultHeader(
			HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue()
		).defaultHeader(
			HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
		).build();

		webClient.post(
		).uri(
			"o/headless-admin-user/v1.0/accounts"
		).bodyValue(
			"{\"externalReferenceCode\": \"" + accountERC + "\", \"name\": \"" +
				accountName + "\", \"type\": \"business\"}"
		).retrieve(
		).toEntity(
			String.class
		).flatMap(
			responseEntity -> _transform(responseEntity)
		).doOnSuccess(
			responseEntity -> _log(
				"Created account: " + responseEntity.getBody())
		).then(
			webClient.post(
			).uri(
				StringBundler.concat(
					"o/headless-admin-user/v1.0/accounts",
					"/by-external-reference-code/", accountERC,
					"/user-accounts/by-email-address/", email)
			).retrieve(
			).toEntity(
				String.class
			).flatMap(
				responseEntity -> _transform(responseEntity)
			)
		).doOnSuccess(
			responseEntity -> _log("Assigned user: " + responseEntity.getBody())
		).then(
			webClient.get(
			).uri(
				uriBuilder -> uriBuilder.path(
					StringBundler.concat(
						"o/headless-admin-user/v1.0/accounts",
						"/by-external-reference-code/", accountERC,
						"/account-roles")
				).queryParam(
					"filter", "name eq 'Account Administrator'"
				).build()
			).retrieve(
			).bodyToMono(
				String.class
			).map(
				pageJSON -> new JSONObject(
					pageJSON
				).getJSONArray(
					"items"
				).getJSONObject(
					0
				).getInt(
					"id"
				)
			)
		).flatMap(
			accountRoleId -> {
				return webClient.post(
				).uri(
					StringBundler.concat(
						"o/headless-admin-user/v1.0/accounts",
						"/by-external-reference-code/", accountERC,
						"/account-roles/", accountRoleId,
						"/user-accounts/by-email-address/", email)
				).retrieve(
				).toEntity(
					String.class
				).flatMap(
					responseEntity -> _transform(responseEntity)
				).doOnSuccess(
					responseEntity -> _log(
						"Assigned role: " + responseEntity.getBody())
				);
			}
		).subscribe();

		return new ResponseEntity<>(json, HttpStatus.OK);
	}

	private void _log(String message) {
		if (_log.isInfoEnabled()) {
			_log.info(message);
		}
	}

	private Mono<ResponseEntity<String>> _transform(
		ResponseEntity<String> responseEntity) {

		HttpStatus httpStatus = responseEntity.getStatusCode();

		if (httpStatus.is2xxSuccessful()) {
			return Mono.just(responseEntity);
		}

		return Mono.error(new RuntimeException(httpStatus.getReasonPhrase()));
	}

	private static final Log _log = LogFactory.getLog(
		ObjectActionAccountSetupRestController.class);

}