/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.web.internal.configuration.activator;

import com.liferay.object.web.internal.configuration.FFOneToOneRelationshipConfiguration;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

/**
 * @author Mateus Santana
 */
@Component(
	configurationPid = "com.liferay.object.web.internal.configuration.FFOneToOneRelationshipConfiguration",
	service = FFOneToOneRelationshipConfigurationActivator.class
)
public class FFOneToOneRelationshipConfigurationActivator {

	public boolean enabled() {
		return _ffOneToOneRelationshipConfiguration.enabled();
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_ffOneToOneRelationshipConfiguration =
			ConfigurableUtil.createConfigurable(
				FFOneToOneRelationshipConfiguration.class, properties);
	}

	private volatile FFOneToOneRelationshipConfiguration
		_ffOneToOneRelationshipConfiguration;

}