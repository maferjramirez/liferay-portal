/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.oauth2.provider.scope.internal.liferay;

import com.liferay.oauth2.provider.scope.liferay.ScopeContext;
import com.liferay.petra.string.StringPool;

import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;

/**
 * @author Carlos Sierra Andrés
 */
@Component(service = ScopeContext.class)
public class ThreadLocalScopeContext implements ScopeContext {

	@Override
	public void clear() {
		accessTokenThreadLocal.remove();
		applicationNameThreadLocal.remove();
		bundleSymbolicNameThreadLocal.remove();
		companyIdThreadLocal.remove();
	}

	@Override
	public void setAccessToken(String accessToken) {
		accessTokenThreadLocal.set(accessToken);
	}

	@Override
	public void setApplicationName(String applicationName) {
		applicationNameThreadLocal.set(applicationName);
	}

	@Override
	public void setBundle(Bundle bundle) {
		String symbolicName = null;

		if (bundle != null) {
			symbolicName = bundle.getSymbolicName();
		}

		bundleSymbolicNameThreadLocal.set(symbolicName);
	}

	@Override
	public void setCompanyId(long companyId) {
		companyIdThreadLocal.set(companyId);
	}

	protected final ThreadLocal<String> accessTokenThreadLocal =
		ThreadLocal.withInitial(() -> StringPool.BLANK);
	protected final ThreadLocal<String> applicationNameThreadLocal =
		ThreadLocal.withInitial(() -> StringPool.BLANK);
	protected final ThreadLocal<String> bundleSymbolicNameThreadLocal =
		ThreadLocal.withInitial(() -> StringPool.BLANK);
	protected final ThreadLocal<Long> companyIdThreadLocal =
		ThreadLocal.withInitial(() -> 0L);

}