/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.saml.opensaml.integration.internal.metadata;

import com.liferay.portal.kernel.servlet.HttpMethods;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.saml.opensaml.integration.internal.BaseSamlTestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Tomas Polesovsky
 */
public class MetadataManagerImplTest extends BaseSamlTestCase {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testGetRequestPath() {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest(
				HttpMethods.GET,
				"/c/portal/login;jsessionid=ACD311312312323BF.worker1");

		Assert.assertEquals(
			"/c/portal/login",
			_samlHttpRequestUtilImpl.getRequestPath(mockHttpServletRequest));
	}

	@Test
	public void testGetRequestPathWithContext() {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest(
				HttpMethods.GET,
				"/portal/c/portal/login;jsessionid=ACD311312312323BF.worker1");

		mockHttpServletRequest.setContextPath("/portal");

		Assert.assertEquals(
			"/c/portal/login",
			_samlHttpRequestUtilImpl.getRequestPath(mockHttpServletRequest));
	}

	@Test
	public void testGetRequestPathWithoutJsessionId() {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest(HttpMethods.GET, "/c/portal/login");

		Assert.assertEquals(
			"/c/portal/login",
			_samlHttpRequestUtilImpl.getRequestPath(mockHttpServletRequest));
	}

	private final SamlHttpRequestUtilImpl _samlHttpRequestUtilImpl =
		new SamlHttpRequestUtilImpl();

}