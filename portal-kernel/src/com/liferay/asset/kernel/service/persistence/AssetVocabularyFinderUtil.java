/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.asset.kernel.service.persistence;

import com.liferay.portal.kernel.bean.PortalBeanLocatorUtil;

/**
 * @author Brian Wing Shun Chan
 * @generated
 */
public class AssetVocabularyFinderUtil {

	public static int countByG_N(long groupId, String name) {
		return getFinder().countByG_N(groupId, name);
	}

	public static int filterCountByG_N(long groupId, String name) {
		return getFinder().filterCountByG_N(groupId, name);
	}

	public static java.util.List<com.liferay.asset.kernel.model.AssetVocabulary>
		filterFindByG_N(
			long groupId, String name, int start, int end,
			com.liferay.portal.kernel.util.OrderByComparator
				<com.liferay.asset.kernel.model.AssetVocabulary>
					orderByComparator) {

		return getFinder().filterFindByG_N(
			groupId, name, start, end, orderByComparator);
	}

	public static java.util.List<com.liferay.asset.kernel.model.AssetVocabulary>
		findByG_N(
			long groupId, String name, int start, int end,
			com.liferay.portal.kernel.util.OrderByComparator
				<com.liferay.asset.kernel.model.AssetVocabulary>
					orderByComparator) {

		return getFinder().findByG_N(
			groupId, name, start, end, orderByComparator);
	}

	public static AssetVocabularyFinder getFinder() {
		if (_finder == null) {
			_finder = (AssetVocabularyFinder)PortalBeanLocatorUtil.locate(
				AssetVocabularyFinder.class.getName());
		}

		return _finder;
	}

	public void setFinder(AssetVocabularyFinder finder) {
		_finder = finder;
	}

	private static AssetVocabularyFinder _finder;

}