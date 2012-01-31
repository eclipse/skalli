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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.extension.PropertyLookup;

public class MapperUtil {

    public static boolean matches(String s, LinkMappingConfig mapping) {
        Pattern regex = Pattern.compile(mapping.getPattern());
        Matcher matcher = regex.matcher(s);
        return matcher.matches();
    }

    public static String convert(String s, LinkMappingConfig mapping, String projectId) {
        return convert(s, mapping.getPattern(), mapping.getTemplate(), projectId);
    }

    public static String convert(String s, String pattern, String template, String projectId) {
        Map<String,Object> properties = new HashMap<String,Object>();
        if (StringUtils.isNotBlank(projectId)) {
            properties.put("0", projectId); //$NON-NLS-1$
        }
        return convert(s, pattern, template, null, properties);
    }

    public static String convert(String s, String pattern, String template, Project project, String userId) {
        Map<String,Object> properties = new HashMap<String,Object>();
        if (StringUtils.isNotBlank(userId)) {
            properties.put(User.PROPERTY_USERID, userId);
        }
        return convert(s, pattern, template, project, properties);
    }

    public static String convert(String s, String pattern, String template, Project project, Map<String,Object> properties) {
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(s);
        if (!matcher.matches()) {
            return null;
        }
        if (properties == null) {
            properties = new HashMap<String,Object>();
        }
        // put the project ID as property ${0}
        if (project != null) {
            properties.put("0", project.getProjectId()); //$NON-NLS-1$
        }
        // put the groups found by the matcher as properties ${1}, ${2}, ...
        for (int i = 1; i <= matcher.groupCount(); i++) {
            properties.put(Integer.toString(i), matcher.group(i));
        }
        StrLookup propertyResolver = new PropertyLookup(project, properties);
        StrSubstitutor subst = new StrSubstitutor(propertyResolver);
        return subst.replace(template);
    }
}
