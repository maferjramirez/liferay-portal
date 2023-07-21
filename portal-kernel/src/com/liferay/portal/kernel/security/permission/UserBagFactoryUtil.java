/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.kernel.security.permission;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.ServiceProxyFactory;

/**
 * @author Preston Crary
 */
public class UserBagFactoryUtil {

	public static UserBag create(long userId) throws PortalException {
		return _userBagFactory.create(userId);
	}

	private UserBagFactoryUtil() {
	}

	private static volatile UserBagFactory _userBagFactory =
		ServiceProxyFactory.newServiceTrackedInstance(
			UserBagFactory.class, UserBagFactoryUtil.class, "_userBagFactory",
			true);

}