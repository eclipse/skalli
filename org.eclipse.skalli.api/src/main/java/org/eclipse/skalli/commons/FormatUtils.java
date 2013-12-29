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
package org.eclipse.skalli.commons;

import org.apache.commons.lang.time.DateFormatUtils;

public class FormatUtils {

    // no instances, please!
    private FormatUtils() {
    }

    /**
     * Returns the given timestamp in the ISO 8601 format <tt>"yyyy-MM-dd'T'HH:mm:ss'Z'"</tt>.
     * Note, the given format also meets the requirement of the xsd:dateTime format used in
     * the REST API. Timezone is always UTC.
     *
     * @param millis  the timestamp to convert.
     */
    public static String formatUTC(long millis) {
        return DateFormatUtils.formatUTC(millis, "yyyy-MM-dd'T'HH:mm:ss'Z'"); //$NON-NLS-1$
    }

    /**
     * Returns the given timestamp in the ISO 8601 format <tt>"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"</tt>
     * including the milliseconds. Timezone is always UTC.
     *
     * @param millis  the timestamp to convert.
     */
    public static String formatUTCWithMillis(long millis) {
        return DateFormatUtils.formatUTC(millis, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); //$NON-NLS-1$
    }
}
