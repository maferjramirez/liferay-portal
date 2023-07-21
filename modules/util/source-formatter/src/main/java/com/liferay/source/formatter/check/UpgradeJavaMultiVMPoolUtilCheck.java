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
public class UpgradeJavaMultiVMPoolUtilCheck extends BaseFileCheck {

	@Override
	protected String doProcess(
			String fileName, String absolutePath, String content)
		throws Exception {

		if (!fileName.endsWith(".java")) {
			return content;
		}

		if (content.contains(_MULTI_VM_POOL_UTIL_IMPORT)) {
			content = StringUtil.replace(
				content, _MULTI_VM_POOL_UTIL_IMPORT,
				"import com.liferay.portal.kernel.cache.MultiVMPool;");
			content = _replaceGetPortalCache(content);
		}

		if (content.contains(_WARNING_CASE_TYPE)) {
			addMessage(
				fileName,
				"Could not resolve types for MultiVMPool.getPortalCache(). " +
					"Replace 'TO_BE_REPLACED' with the correct type");
		}

		return content;
	}

	private String _replaceGetPortalCache(String content) {
		content = JavaSourceUtil.addImports(
			content, "org.osgi.service.component.annotations.Reference");

		Matcher portalCacheMatcher = _getPortalCachePattern.matcher(content);

		while (portalCacheMatcher.find()) {
			String newDeclaration = StringUtil.replace(
				portalCacheMatcher.group(0), "MultiVMPoolUtil.getPortalCache(",
				"(PortalCache" + portalCacheMatcher.group(1) +
					") _multiVMPool.getPortalCache(");

			content = StringUtil.replace(
				content, portalCacheMatcher.group(0), newDeclaration);
		}

		content = StringUtil.replace(
			content, "MultiVMPoolUtil.getPortalCache(",
			_WARNING_CASE_TYPE + " _multiVMPool.getPortalCache(");
		content = StringUtil.replaceLast(
			content, CharPool.CLOSE_CURLY_BRACE,
			"\n\t@Reference\n\tprivate MultiVMPool _multiVMPool;\n\n}");

		return content;
	}

	private static final String _MULTI_VM_POOL_UTIL_IMPORT =
		"import com.liferay.portal.kernel.cache.MultiVMPoolUtil;";

	private static final String _WARNING_CASE_TYPE =
		"(PortalCache<TO_BE_REPLACED, TO_BE_REPLACED>)";

	private static final Pattern _getPortalCachePattern = Pattern.compile(
		"PortalCache\\s*(<.+, +?.+>)\\s*\\w+\\s*=\\s*" +
			"MultiVMPoolUtil\\.getPortalCache\\(");

}