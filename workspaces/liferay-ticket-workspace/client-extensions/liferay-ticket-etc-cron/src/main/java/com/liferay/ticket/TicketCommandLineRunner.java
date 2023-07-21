/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ticket;

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Gregory Amerson
 */
@Component
public class TicketCommandLineRunner implements CommandLineRunner {

	@Override
	public void run(String... args) throws Exception {
		TicketsResponse ticketsResponse = WebClient.create(
			_lxcDXPServerProtocol + "://" + _lxcDXPMainDomain
		).get(
		).uri(
			"/o/c/tickets"
		).accept(
			MediaType.APPLICATION_JSON
		).header(
			HttpHeaders.AUTHORIZATION,
			"Bearer " + _oAuth2AccessToken.getTokenValue()
		).retrieve(
		).bodyToMono(
			TicketsResponse.class
		).block();

		if (_log.isInfoEnabled()) {
			_log.info("Amount of tickets: " + ticketsResponse.items.length);
		}

		Arrays.stream(
			ticketsResponse.items
		).filter(
			ticket ->
				(ticket.resolution != null) &&
				(Objects.equals(ticket.resolution.key, "duplicate") ||
				 Objects.equals(ticket.resolution.key, "done"))
		).map(
			ticket -> ticket.id
		).forEach(
			ticketId -> {
				try {
					if (_log.isInfoEnabled()) {
						_log.info("Deleting ticket: " + ticketId);
					}

					WebClient.create(
						_lxcDXPServerProtocol + "://" + _lxcDXPMainDomain
					).delete(
					).uri(
						"/o/c/tickets/{ticketId}", ticketId
					).accept(
						MediaType.APPLICATION_JSON
					).header(
						HttpHeaders.AUTHORIZATION,
						"Bearer " + _oAuth2AccessToken.getTokenValue()
					).retrieve(
					).toEntity(
						Void.class
					).block();
				}
				catch (Exception exception) {
					_log.error(exception);
				}
			}
		);
	}

	private static final Log _log = LogFactory.getLog(
		TicketCommandLineRunner.class);

	@Autowired
	private AuthorizedClientServiceOAuth2AuthorizedClientManager
		_authorizedClientServiceOAuth2AuthorizedClientManager;

	@Value("${com.liferay.lxc.dxp.mainDomain}")
	private String _lxcDXPMainDomain;

	@Value("${com.liferay.lxc.dxp.server.protocol}")
	private String _lxcDXPServerProtocol;

	@Autowired
	private OAuth2AccessToken _oAuth2AccessToken;

	private static class Resolution {

		public String key;

	}

	private static class Ticket {

		public String id;
		public Resolution resolution;
		public String subject;

	}

	private static class TicketsResponse {

		public Ticket[] items;

	}

}