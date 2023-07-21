/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.source.formatter.check.util.JavaSourceUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tamyris Bernardo
 */
public class UpgradeGetImagePreviewURLMethodCheck extends BaseFileCheck {

	@Override
	protected String doProcess(
			String fileName, String absolutePath, String content)
		throws Exception {

		if (!fileName.endsWith(".java") && !fileName.endsWith(".jsp")) {
			return content;
		}

		boolean replaced = false;

		Matcher getImagePreviewURLMatcher = _getImagePreviewURLPattern.matcher(
			content);

		while (getImagePreviewURLMatcher.find()) {
			String methodCall = getImagePreviewURLMatcher.group();

			content = StringUtil.replace(
				content, methodCall,
				StringUtil.replace(methodCall, "DLUtil", "_dlURLHelper"));

			replaced = true;
		}

		if (fileName.endsWith(".java") && replaced) {
			content = JavaSourceUtil.addImports(
				content, "com.liferay.document.library.util.DLURLHelper");
			content = StringUtil.replaceLast(
				content, CharPool.CLOSE_CURLY_BRACE,
				"\t@Reference\n\tprivate DLURLHelper _dlURLHelper;\n\n}");
		}

		return content;
	}

	private static final Pattern _getImagePreviewURLPattern = Pattern.compile(
		"DLUtil\\.\\s*getImagePreviewURL\\(");

}