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
package org.eclipse.skalli.ext.mapping;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapperUtil {

    public static boolean matches(String source, LinkMappingConfig mapping) {
        Pattern regex = Pattern.compile(mapping.getPattern());
        Matcher matcher = regex.matcher(source);
        return matcher.matches();
    }

    public static String convert(String projectId, String source, LinkMappingConfig mapping) {
        return convert(projectId, source, mapping.getPattern(), mapping.getTemplate());
    }

    public static String convert(String projectId, String source, String pattern, String template) {
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(source);
        if (!matcher.matches()) {
            return null;
        }
        String[] groups = new String[matcher.groupCount() + 1];
        groups[0] = projectId;
        for (int i = 1; i <= matcher.groupCount(); i++) {
            groups[i] = matcher.group(i);
        }
        return MessageFormat.format(template, (Object[]) groups);
    }

}
