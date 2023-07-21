/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.rest.petra.sql.dsl.expression;

import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.portal.odata.entity.EntityModel;

/**
 * @author Gabriel Albuquerque
 */
public interface FilterPredicateFactory {

	public Predicate create(
		EntityModel entityModel, String filterString, long objectDefinitionId);

	public Predicate create(String filterString, long objectDefinitionId);

}