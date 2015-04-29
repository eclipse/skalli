/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.commons;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Utilities for parsing and navigating in JSON data structures.
 */
public final class JSONUtils {

    private JSONUtils() {
    }

    /**
     * Parses the given serialized JSON and returns the result as
     * generic JSON element.
     *
     * @param s  the string to parse.
     * @return  the JSON element corresponding to the passed in text.
     *
     * @throws JsonParseException if the specified text is not valid JSON.
     */
    public static JsonElement jsonFromString(String s) throws JsonParseException {
        return new JsonParser().parse(s);
    }

    /**
     * Parses the given serialized JSON and returns the result as
     * generic JSON object.
     *
     * @param s  the string to parse.
     * @return  the JSON object corresponding to the passed in text.
     *
     * @throws JsonParseException if the specified text is not valid JSON,
     * or the result is not a JSON object.
     */
    public static JsonObject jsonObjectFromString(String s) throws JsonParseException {
        JsonElement parsed = jsonFromString(s);
        if (!parsed.isJsonObject()) {
            throw new JsonParseException(MessageFormat.format("Not a JSON object: ''{0}''", s));
        }
        return parsed.getAsJsonObject();
    }

    /**
     * Parses the given serialized JSON and returns the result as
     * generic JSON array.
     *
     * @param s  the string to parse.
     * @return  the JSON array corresponding to the passed in text.
     *
     * @throws JsonParseException if the specified text is not valid JSON,
     * or the result is not a JSON array.
     */
    public static JsonArray jsonArrayFromString(String s) throws JsonParseException {
        JsonElement parsed = jsonFromString(s);
        if (!parsed.isJsonArray()) {
            throw new JsonParseException(MessageFormat.format("Not a JSON array: ''{0}''", s));
        }
        return parsed.getAsJsonArray();
    }

    /**
     * Returns the integer value of a given attribute of a JSON object.
     * Supports nested attributes like &quot;outer.inner.attribute&quot;.
     *
     * @param json  a serialized JSON string,
     * @param attribute  the attribute to return the value for.
     *
     * @return the integer value of the attribute, or <code>null</code> if the specified
     * attribute does not exist. If the value is a string, its numerical representationm
     * is returned, if possible.
     */
    public static Integer getInteger(String json, String attribute) {
        JsonPrimitive value = getPrimitive(jsonObjectFromString(json), attribute);
        if (value != null) {
            if (value.isNumber()) {
                return value.getAsInt();
            }
            if (value.isString()) {
                try {
                    return Integer.parseInt(value.getAsString());
                } catch (NumberFormatException e) {
                    // ignore and return null
                }
            }
        }
        return null;
    }

    /**
     * Returns the string value of a given attribute of a JSON object.
     * Supports nested attributes like &quot;outer.inner.attribute&quot;.
     *
     * @param json  serialized JSON string.
     * @param attribute  the attribute to return the value for.
     *
     * @return the string value of the attribute, or <code>null</code> if the specified
     * attribute does not exist.
     */
    public static String getString(String json, String attribute) {
        JsonPrimitive value = getPrimitive(jsonObjectFromString(json), attribute);
        return value != null && (value.isString() || value.isNumber() || value.isBoolean())? value.getAsString() : null;
    }

    /**
     * Navigates into a JSON object following a dot notation, e.g.
     * &quot;outer.inner.attribute&quot;.
     *
     * @param root  the JSON object to start with.
     * @param path  the path to navigate through the JSON object tree.
     *
     * @return the value as generic JSON primitive (string, number etc.).
     */
    public static JsonPrimitive getPrimitive(JsonObject root, String path) {
        JsonElement value = getValue(root, path);
        return value != null && value.isJsonPrimitive()? value.getAsJsonPrimitive() : null;
    }

    /**
     * Navigates into a JSON object following a dot notation, e.g.
     * &quot;outer.inner.attribute&quot;.
     *
     * @param root  the JSON object to start with.
     * @param path  the path to navigate through the JSON object tree.
     *
     * @return the value as generic JSON element.
     */
    public static JsonElement getValue(JsonObject root, String path) {
        if (StringUtils.isBlank(path)) {
            return root;
        }
        String[] split = path.split("\\."); //$NON-NLS-1$

        int i = 0;
        JsonObject next = root;
        while (i < split.length - 1) {
            if (!next.has(split[i])) {
                return null;
            }
            JsonElement elem = next.get(split[i]);
            if (!elem.isJsonObject()) {
                return null;
            }
            next = elem.getAsJsonObject();
            ++i;
        }
        return next.get(split[i]);
    }

}
