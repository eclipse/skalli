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

import java.text.MessageFormat;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of utilities for {@link java.util.UUID}.
 */
public class UUIDUtils {

    private static final Logger LOG = LoggerFactory.getLogger(UUIDUtils.class);

    // no instances, please!
    private UUIDUtils() {
    }

    /**
     * Checks if the given string is a valid {@link java.util.UUID}.
     * @param s  the string to check.
     * @return  <code>true</code>, if the string is a valid UUID,
     * or <code>false</code>, if the string is <code>null</code>, an
     * empty string or not a valid UUID.
     */
    public static boolean isUUID(String s) {
        if (StringUtils.isNotBlank(s)) {
            try {
                UUID.fromString(s);
                return true;
            } catch (IllegalArgumentException ex) {
                LOG.debug(MessageFormat.format("{0} is not considered to be a valid UUID.", s));
            }
        }
        return false;
    }

    /**
     * Converts the given string into a UUID using {@link UUID#fromString(String)}.
     * Returns {@link UUID#randomUUID()}, if the string is not a valid UUID.
     * (according to {@link #isUUID(String)}).
     * @param s  the string to convert.
     * @return  the UUID corresponding to the given string or a random UUID.
     */
    public static UUID asUUID(String s) {
        if (StringUtils.isNotBlank(s)) {
            try {
                return UUID.fromString(s);
            } catch (IllegalArgumentException ex) {
                LOG.debug(MessageFormat.format(
                        "{0} is not considered to be a valid UUID. Going to return a random one.", s));
            }
        }
        return UUID.randomUUID();
    }

}
