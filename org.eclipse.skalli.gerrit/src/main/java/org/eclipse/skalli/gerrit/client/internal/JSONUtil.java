/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.gerrit.client.internal;

import org.json.JSONException;
import org.json.JSONObject;

public final class JSONUtil {

    private JSONUtil() {
    }

    /**
     * Returns the Integer value for <code>attributeName</code>. Supports nested
     * attribute names (e.g. &quot;outer.inner.attribute&quot;).
     *
     * @param json
     *            a serialized JSON String
     * @param attributeName
     *            name of the attribute
     *
     * @return the Integer value, null in case of failure
     */
    public static Integer getInteger(final String json, final String attributeName) {
        return getValue(json, attributeName);
    }

    /**
     * Returns the String value for <code>attributeName</code>. Supports nested
     * attribute names (e.g. &quot;outer.inner.attribute&quot;).
     *
     * @param json
     *            a serialized JSON String
     * @param attributeName
     *            name of the attribute
     *
     * @return the String value, null in case of failure
     */
    public static String getString(final String json, final String attributeName) {
        return getValue(json, attributeName);
    }

    /**
     * Navigates into a JSON object following a dot notation (e.g.
     * &quot;outer.inner.attribute&quot;).
     *
     * @param json
     *            a JSON object
     * @param path
     *            the way to navigate through the JSON object
     *
     * @return the value
     */
    @SuppressWarnings("unchecked")
    private static <T> T getValue(String json, final String path) {
        try {
            JSONObject jObj = new JSONObject(json);
            final String[] split = path.split("\\."); //$NON-NLS-1$

            int i = 0;
            while (i < split.length - 1) {
                jObj = jObj.getJSONObject(split[i]);
                i++;
            }

            return (T) jObj.get(split[i]);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
