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
 * @author Nícolas Moura
 */
public class UpgradeJavaExtractTextMethodCheck extends BaseFileCheck {

	@Override
	protected String doProcess(
			String fileName, String absolutePath, String content)
		throws Exception {

		if (!fileName.endsWith(".java")) {
			return content;
		}

		String newContent = content;

		boolean replaced = false;

		Matcher extractTextMatcher = _extractTextPattern.matcher(content);

		while (extractTextMatcher.find()) {
			String methodCall = JavaSourceUtil.getMethodCall(
				content, extractTextMatcher.start());

			newContent = StringUtil.replace(
				newContent, methodCall,
				StringUtil.replace(
					methodCall, "HtmlUtil.extractText(",
					"_htmlParser.extractText("));

			replaced = true;
		}

		if (replaced) {
			newContent = JavaSourceUtil.addImports(
				newContent, "com.liferay.portal.kernel.util.HtmlParser",
				"org.osgi.service.component.annotations.Reference");
			newContent = StringUtil.replaceLast(
				newContent, CharPool.CLOSE_CURLY_BRACE,
				"\n\t@Reference\n\tprivate HtmlParser _htmlParser;\n}");
		}

		return newContent;
	}

	private static final Pattern _extractTextPattern = Pattern.compile(
		"HtmlUtil\\.extractText\\(");

}