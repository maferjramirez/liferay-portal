/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.definition.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.security.xml.SecureXMLFactoryProviderUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowException;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.xml.sax.InputSource;

/**
 * @author Rafael Praxedes
 */
public class WorkflowDefinitionContentUtil {

	public static String toJSON(String xml) throws WorkflowException {
		try {
			DocumentBuilderFactory documentBuilderFactory =
				SecureXMLFactoryProviderUtil.newDocumentBuilderFactory();

			DocumentBuilder documentBuilder =
				documentBuilderFactory.newDocumentBuilder();

			Document document = documentBuilder.parse(
				new InputSource(new StringReader(xml)));

			Element rootElement = document.getDocumentElement();

			JSONObject rootJSONObject = _toJSONObject(rootElement);

			return rootJSONObject.toString();
		}
		catch (Exception exception) {
			throw new WorkflowException(
				"Unable to convert workflow definition", exception);
		}
	}

	public static String toXML(String json) throws WorkflowException {
		StringBuilder contentSB = new StringBuilder();

		contentSB.append("<?xml version=\"1.0\"?>");

		try {
			_toNode(JSONFactoryUtil.createJSONObject(json), contentSB);
		}
		catch (JSONException jsonException) {
			throw new WorkflowException(
				"Unable to convert workflow definition", jsonException);
		}

		return contentSB.toString();
	}

	private static void _appendAttributes(
		Element element, JSONObject jsonObject) {

		NamedNodeMap attributes = element.getAttributes();

		for (int i = 0; i < attributes.getLength(); i++) {
			Node node = attributes.item(i);

			jsonObject.put(node.getNodeName(), node.getNodeValue());
		}
	}

	private static void _appendAttributes(
		JSONObject jsonObject, StringBuilder contentSB) {

		for (String key : jsonObject.keySet()) {
			if (key.equals("#cdata-value") || key.equals("#child-nodes") ||
				key.equals("#tag-name") || key.equals("#value")) {

				continue;
			}

			contentSB.append(StringPool.SPACE);

			contentSB.append(key);

			contentSB.append(StringPool.EQUAL);
			contentSB.append(StringPool.QUOTE);

			contentSB.append(jsonObject.get(key));

			contentSB.append(StringPool.QUOTE);
		}
	}

	private static void _appendValue(Element element, JSONObject jsonObject) {
		if (!element.hasChildNodes()) {
			return;
		}

		String content = null;
		boolean cdata = false;

		NodeList childNodes = element.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);

			if (!(node instanceof Text) || !_hasContent(node.getNodeValue())) {
				continue;
			}

			if (node instanceof CDATASection) {
				cdata = true;
			}

			content = node.getNodeValue();

			break;
		}

		if (content == null) {
			return;
		}

		if (cdata) {
			JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

			String[] lines = content.split(StringPool.NEW_LINE);

			for (String line : lines) {
				jsonArray.put(
					line.replaceAll(StringPool.TAB, StringPool.FOUR_SPACES));
			}

			jsonObject.put("#cdata-value", jsonArray);
		}
		else {
			jsonObject.put("#value", content.replaceAll("[\n\t]", ""));
		}
	}

	private static void _appendValue(
		JSONObject jsonObject, StringBuilder contentSB) {

		if (jsonObject.has("#cdata-value")) {
			JSONArray jsonArray = jsonObject.getJSONArray("#cdata-value");

			StringBundler cdataSB = new StringBundler(
				(jsonArray.length() * 2) + 2);

			cdataSB.append(StringPool.CDATA_OPEN);

			for (int i = 0; i < jsonArray.length(); i++) {
				String line = jsonArray.getString(i);

				line = line.replaceAll("\\s\\s\\s\\s", StringPool.TAB);

				cdataSB.append(line);

				cdataSB.append(StringPool.NEW_LINE);
			}

			cdataSB.append(StringPool.CDATA_CLOSE);

			contentSB.append(cdataSB);
		}
		else if (jsonObject.has("#value")) {
			contentSB.append(jsonObject.getString("#value"));
		}
	}

	private static boolean _hasContent(String value) {
		if (Validator.isNull(value)) {
			return false;
		}

		value = value.replaceAll("[\n\t]", "");

		return !value.isEmpty();
	}

	private static JSONObject _toJSONObject(Element element) {
		JSONObject jsonObject = JSONUtil.put("#tag-name", element.getTagName());

		_appendAttributes(element, jsonObject);

		_appendValue(element, jsonObject);

		NodeList childNodes = element.getChildNodes();

		JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);

			if (!(childNode instanceof Element)) {
				continue;
			}

			jsonArray.put(_toJSONObject((Element)childNode));
		}

		if (jsonArray.length() > 0) {
			jsonObject.put("#child-nodes", jsonArray);
		}

		return jsonObject;
	}

	private static void _toNode(
		JSONObject jsonObject, StringBuilder contentSB) {

		contentSB.append(StringPool.LESS_THAN);
		contentSB.append(jsonObject.getString("#tag-name"));

		_appendAttributes(jsonObject, contentSB);

		contentSB.append(StringPool.GREATER_THAN);

		_appendValue(jsonObject, contentSB);

		JSONArray childNodesJSONArray = jsonObject.getJSONArray("#child-nodes");

		if (childNodesJSONArray != null) {
			childNodesJSONArray.forEach(
				object -> _toNode((JSONObject)object, contentSB));
		}

		contentSB.append(StringPool.LESS_THAN);
		contentSB.append(StringPool.FORWARD_SLASH);
		contentSB.append(jsonObject.getString("#tag-name"));
		contentSB.append(StringPool.GREATER_THAN);
	}

}