<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
String redirect = ParamUtil.getString(request, "redirect");

JournalArticle article = journalDisplayContext.getArticle();
%>

<pre class="m-4"><%= HtmlUtil.escape(article.getContent()) %></pre>

<aui:button-row>
	<aui:button href="<%= redirect %>" type="cancel" value="close" />
</aui:button-row>