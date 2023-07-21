/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ticket;

import com.liferay.portal.search.rest.client.dto.v1_0.Suggestion;
import com.liferay.portal.search.rest.client.dto.v1_0.SuggestionsContributorConfiguration;
import com.liferay.portal.search.rest.client.dto.v1_0.SuggestionsContributorResults;
import com.liferay.portal.search.rest.client.pagination.Page;
import com.liferay.portal.search.rest.client.resource.v1_0.SuggestionResource;

import java.time.Duration;

import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

import reactor.util.retry.Retry;

/**
 * @author Raymond Augé
 * @author Gregory Amerson
 * @author Allen Ziegenfus
 */
@RestController
public class TicketRestController extends BaseRestController {

	public static final String SUGGESTION_HOST = "learn.liferay.com";

	public static final int SUGGESTION_PORT = 443;

	public static final String SUGGESTION_SCHEME = "https";

	public TicketRestController() {
		_initResourceBuilders();
	}

	@PostMapping("/ticket/object/action/documentation/referral")
	public ResponseEntity<String> postTicketObjectAction1(
		@AuthenticationPrincipal Jwt jwt, @RequestBody String json) {

		log(jwt, _log, json);

		try {
			JSONObject jsonObject = new JSONObject(json);

			_addDocumentationReferralAndQueue(
				_lxcDXPServerProtocol, _lxcDXPMainDomain, jwt.getTokenValue(),
				jsonObject);
		}
		catch (Exception exception) {
			_log.error("JSON: " + json, exception);
		}

		return new ResponseEntity<>(json, HttpStatus.CREATED);
	}

	private void _addDocumentationReferralAndQueue(
		String lxcDXPServerProtocol, String lxcDXPMainDomain, String jwtToken,
		JSONObject jsonObject) {

		Objects.requireNonNull(jsonObject);

		JSONObject objectEntryDTOTicketJSONObject = jsonObject.getJSONObject(
			"objectEntryDTOTicket");

		JSONObject propertiesJSONObject =
			objectEntryDTOTicketJSONObject.getJSONObject("properties");

		JSONObject ticketStatusJSONObject = propertiesJSONObject.getJSONObject(
			"ticketStatus");

		String subject = propertiesJSONObject.getString("subject");

		ticketStatusJSONObject.remove("name");
		ticketStatusJSONObject.put("key", "queued");
		propertiesJSONObject.put("suggestions", _getSuggestionsJSON(subject));

		if (_log.isInfoEnabled()) {
			_log.info(
				"JSON OUTPUT: \n\n" + propertiesJSONObject.toString(4) + "\n");
		}

		WebClient.Builder builder = WebClient.builder();

		WebClient webClient = builder.baseUrl(
			lxcDXPServerProtocol + "://" + lxcDXPMainDomain
		).defaultHeader(
			HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE
		).defaultHeader(
			HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
		).build();

		webClient.patch(
		).uri(
			"/o/c/tickets/{ticketId}",
			objectEntryDTOTicketJSONObject.getLong("id")
		).bodyValue(
			propertiesJSONObject.toString()
		).header(
			HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken
		).exchangeToMono(
			clientResponse -> {
				HttpStatus httpStatus = clientResponse.statusCode();

				if (httpStatus.is2xxSuccessful()) {
					return clientResponse.bodyToMono(String.class);
				}
				else if (httpStatus.is4xxClientError()) {
					if (_log.isInfoEnabled()) {
						_log.info("Output: " + httpStatus.getReasonPhrase());
					}
				}

				Mono<WebClientResponseException> mono =
					clientResponse.createException();

				return mono.flatMap(Mono::error);
			}
		).retryWhen(
			Retry.backoff(
				3, Duration.ofSeconds(1)
			).doAfterRetry(
				retrySignal -> _log.info("Retrying request")
			)
		).doOnNext(
			output -> {
				if (_log.isInfoEnabled()) {
					_log.info("Output: " + output);
				}
			}
		).subscribe();
	}

	private String _getSuggestionsJSON(String subject) {
		JSONArray suggestionsJSONArray = new JSONArray();

		SuggestionsContributorConfiguration
			suggestionsContributorConfiguration =
				new SuggestionsContributorConfiguration();

		suggestionsContributorConfiguration.setContributorName("sxpBlueprint");

		suggestionsContributorConfiguration.setDisplayGroupName(
			"Public Nav Search Recommendations");

		suggestionsContributorConfiguration.setSize(3);

		JSONObject attributesJSONObject = new JSONObject();

		attributesJSONObject.put(
			"includeAssetSearchSummary", true
		).put(
			"includeassetURL", true
		).put(
			"sxpBlueprintId", 3628599
		);

		suggestionsContributorConfiguration.setAttributes(attributesJSONObject);

		try {
			Page<SuggestionsContributorResults>
				suggestionsContributorResultsPage =
					_suggestionResource.postSuggestionsPage(
						SUGGESTION_SCHEME + "://" + SUGGESTION_HOST, "/search",
						3190049L, "", 1434L, "this-site", subject,
						new SuggestionsContributorConfiguration[] {
							suggestionsContributorConfiguration
						});

			for (SuggestionsContributorResults suggestionsContributorResults :
					suggestionsContributorResultsPage.getItems()) {

				Suggestion[] suggestions =
					suggestionsContributorResults.getSuggestions();

				for (Suggestion suggestion : suggestions) {
					String text = suggestion.getText();

					JSONObject suggestionAttributesJSONObject = new JSONObject(
						String.valueOf(suggestion.getAttributes()));

					String assetURL =
						(String)suggestionAttributesJSONObject.get("assetURL");

					JSONObject suggestionJSONObject = new JSONObject();

					suggestionJSONObject.put(
						"assetURL",
						SUGGESTION_SCHEME + "://" + SUGGESTION_HOST + assetURL
					).put(
						"text", text
					);
					suggestionsJSONArray.put(suggestionJSONObject);
				}
			}
		}
		catch (Exception exception) {
			_log.error("Could not retrieve search suggestions", exception);

			// Always return something for the purposes of the workshop

			JSONObject suggestionJSONObject = new JSONObject();

			suggestionJSONObject.put(
				"assetURL", SUGGESTION_SCHEME + "://" + SUGGESTION_HOST
			).put(
				"text", "learn.liferay.com"
			);

			suggestionsJSONArray.put(suggestionJSONObject);
		}

		return suggestionsJSONArray.toString();
	}

	private void _initResourceBuilders() {
		SuggestionResource.Builder dataDefinitionResourceBuilder =
			SuggestionResource.builder();

		_suggestionResource = dataDefinitionResourceBuilder.header(
			HttpHeaders.USER_AGENT, TicketRestController.class.getName()
		).header(
			HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE
		).header(
			HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
		).endpoint(
			SUGGESTION_HOST, SUGGESTION_PORT, SUGGESTION_SCHEME
		).build();
	}

	private static final Log _log = LogFactory.getLog(
		TicketRestController.class);

	@Value("${com.liferay.lxc.dxp.mainDomain}")
	private String _lxcDXPMainDomain;

	@Value("${com.liferay.lxc.dxp.server.protocol}")
	private String _lxcDXPServerProtocol;

	private SuggestionResource _suggestionResource;

}