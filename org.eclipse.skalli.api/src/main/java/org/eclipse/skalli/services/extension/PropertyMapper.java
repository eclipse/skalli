/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.services.extension;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.model.User;

/**
 * Utility for mapping strings matching a given regular expression pattern
 * into other strings specified by a template that may contain placeholders.
 * <p>
 * The {@link MatchResult#group(int) groups} of the match result are available
 * as placeholders <tt>${1},${2},...</tt>. Some methods of this class take a
 * <code>userId</code> as input parameter, which is mapped to the placeholder
 * <tt>${userId}</tt>. Furthermore, custom properties can be specified that
 * are mapped to placeholders with their respective keys.
 * <p>
 * The mapping can be performed in the context of an {@link EntityBase entity},
 * e.g. a {@link Project project}. The properties of that entity are mapped
 * to placeholders of the form <tt>${propertyName}</tt>, where <tt>propertyName</tt>
 * is the identifier of the property as declared by the corresponding {@link PropertyName}
 * annotation.<br>
 * For {@link ExtensibleEntityBase extensible entities}, properties of the extensions
 * are mapped to placeholders of the form <tt>${extension.propertyName}</tt>, where
 * <tt>extension</tt> is the {@link ExtensionService#getShortName() short name}
 * of the extension to which the property belongs. Example: <tt>${devInf.scmLocations}</tt>.
 */
public class PropertyMapper {

    private PropertyMapper() {
    }

    /**
     * Returns <code>true</code>, if the string matches the given
     * {@link Pattern regular expression}.
     *
     * @param s  the string to check.
     * @param pattern  the regular expression to apply.
     */
    public static boolean matches(String s, String pattern) {
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(s);
        return matcher.matches();
    }

    /**
     * Returns <code>true</code>, if the string matches the given
     * regular expression pattern.
     *
     * @param s  the string to check.
     * @param pattern  the regular expression to apply.
     */
    public static boolean matches(String s, Pattern pattern) {
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }

    /**
     * Converts a string by applying the given <code>template</code>, if it matches
     * the given {@link Pattern regular expression}. If specified, the <code>uniqueId</code>,
     * e.g. a project's symbolic name, is mapped to the placeholder <tt>${0}</tt>. The
     * placeholders <tt>${1},${2},...</tt> provide the {@link MatchResult#group(int) groups}
     * of the match result.
     *
     * @param s  the string to check.
     * @param pattern the regular expression to apply.
     * @param template  the template to use for the mapping.
     * @param uniqueId  some unique identifier to map to placeholder <tt>${0}</tt>.
     *
     * @return the mapped string, or <code>null</code>, if the string did not match the
     * given regular expression.
     */
    public static String convert(String s, String pattern, String template, String uniqueId) {
        Map<String,Object> properties = new HashMap<String,Object>();
        if (StringUtils.isNotBlank(uniqueId)) {
            properties.put("0", uniqueId); //$NON-NLS-1$
        }
        return convert(s, pattern, template, null, properties);
    }

    /**
     * Converts a string by applying the given <code>template</code>, if it matches
     * the given regular expression. If specified, the <code>uniqueId</code>,
     * e.g. a project's symbolic name, is mapped to the placeholder <tt>${0}</tt>. The
     * placeholders <tt>${1},${2},...</tt> provide the {@link MatchResult#group(int) groups}
     * of the match result.
     *
     * @param s  the string to check.
     * @param pattern the regular expression to apply.
     * @param template  the template to use for the mapping.
     * @param uniqueId  some unique identifier to map to placeholder <tt>${0}</tt>.
     *
     * @return the mapped string, or <code>null</code>, if the string did not match the
     * given regular expression.
     */
    public static String convert(String s, Pattern pattern, String template, String uniqueId) {
        Map<String,Object> properties = new HashMap<String,Object>();
        if (StringUtils.isNotBlank(uniqueId)) {
            properties.put("0", uniqueId); //$NON-NLS-1$
        }
        return convert(s, pattern, template, null, properties);
    }

    /**
     * Converts a string by applying the given <code>template</code>, if it matches
     * the given {@link Pattern regular expression}. The properties of the entity, if specified, are mapped to
     * placeholders of the form <tt>${propertyName}</tt>.
     * Properties of extensions of the entity, if any, are mapped to placeholders of the form
     * <tt>${extension.propertyName}</tt>.
     * The placeholders <tt>${1},${2},...</tt> provide the {@link MatchResult#group(int) groups}
     * of the match result.
     *
     * @param s  the string to check.
     * @param pattern the regular expression to apply.
     * @param template  the template to use for the mapping.
     * @param entity  any (extensible) entity.
     *
     * @return the mapped string, or <code>null</code>, if the string did not match the
     * given regular expression.
     */
    public static String convert(String s, String pattern, String template, EntityBase entity) {
        return convert(s, pattern, template, entity, (Map<String, Object>)null);
    }


    /**
     * Converts a string by applying the given <code>template</code>, if it matches
     * the given regular expression. The properties of the entity, if specified, are mapped to
     * placeholders of the form <tt>${propertyName}</tt>.
     * Properties of extensions of the entity, if any, are mapped to placeholders of the form
     * <tt>${extension.propertyName}</tt>.
     * The placeholders <tt>${1},${2},...</tt> provide the {@link MatchResult#group(int) groups}
     * of the match result.
     *
     * @param s  the string to check.
     * @param pattern the regular expression to apply.
     * @param template  the template to use for the mapping.
     * @param entity  any (extensible) entity.
     *
     * @return the mapped string, or <code>null</code>, if the string did not match the
     * given regular expression.
     */
    public static String convert(String s, Pattern pattern, String template, EntityBase entity) {
        return convert(s, pattern, template, entity, (Map<String, Object>)null);
    }

    /**
     * Converts a string by applying the given <code>template</code>, if it matches
     * the given {@link Pattern regular expression}. If specified, the <code>userId</code>
     * parameter, is mapped to the placeholder <tt>${userId}</tt>. The properties of
     * the entity, if specified, are mapped to placeholders of the form <tt>${propertyName}</tt>.
     * Properties of extensions of the entity, if any, are mapped to placeholders of the form
     * <tt>${extension.propertyName}</tt>.
     * The placeholders <tt>${1},${2},...</tt> provide the {@link MatchResult#group(int) groups}
     * of the match result.
     *
     * @param s  the string to check.
     * @param pattern the regular expression to apply.
     * @param template  the template to use for the mapping.
     * @param entity  any (extensible) entity.
     * @param userId  the unique identifier of a user.
     *
     * @return the mapped string, or <code>null</code>, if the string did not match the
     * given regular expression.
     */
    public static String convert(String s, String pattern, String template, EntityBase entity, String userId) {
        Map<String,Object> properties = new HashMap<String,Object>();
        if (StringUtils.isNotBlank(userId)) {
            properties.put(User.PROPERTY_USERID, userId);
        }
        return convert(s, pattern, template, entity, properties);
    }

    /**
     * Converts a string by applying the given <code>template</code>, if it matches
     * the given regular expression. If specified, the <code>userId</code>
     * parameter, is mapped to the placeholder <tt>${userId}</tt>. The properties of
     * the entity, if specified, are mapped to placeholders of the form <tt>${propertyName}</tt>.
     * Properties of extensions of the entity, if any, are mapped to placeholders of the form
     * <tt>${extension.propertyName}</tt>.
     * The placeholders <tt>${1},${2},...</tt> provide the {@link MatchResult#group(int) groups}
     * of the match result.
     *
     * @param s  the string to check.
     * @param pattern the regular expression to apply.
     * @param template  the template to use for the mapping.
     * @param entity  any (extensible) entity.
     * @param userId  the unique identifier of a user.
     *
     * @return the mapped string, or <code>null</code>, if the string did not match the
     * given regular expression.
     */
    public static String convert(String s, Pattern pattern, String template, EntityBase entity, String userId) {
        Map<String,Object> properties = new HashMap<String,Object>();
        if (StringUtils.isNotBlank(userId)) {
            properties.put(User.PROPERTY_USERID, userId);
        }
        return convert(s, pattern, template, entity, properties);
    }

    /**
     * Converts a string by applying the given <code>template</code>, if it matches
     * the given {@link Pattern regular expression}. The properties of the entity, if specified, are mapped to
     * placeholders of the form <tt>${propertyName}</tt>.
     * Properties of extensions of the entity, if any, are mapped to placeholders of the form
     * <tt>${extension.propertyName}</tt>.
     * The custom properties, if specified, are mapped to placeholders with their respective keys,
     * e.g. property with key <tt>"prop"</tt> is mapped to the placeholder <tt>${prop}</tt>.
     * The placeholders <tt>${1},${2},...</tt> provide the {@link MatchResult#group(int) groups}
     * of the match result.
     *
     * @param s  the string to check.
     * @param pattern the regular expression to apply.
     * @param template  the template to use for the mapping.
     * @param entity  any (extensible) entity.
     * @param properties  additional properties.
     *
     * @return the mapped string, or <code>null</code>, if the string did not match the
     * given regular expression.
     */
    public static String convert(String s, String pattern, String template, EntityBase entity, Map<String,Object> properties) {
        if (pattern == null) {
            return null;
        }
        Pattern regex = Pattern.compile(pattern);
        return convert(s, regex, template, entity, properties);
    }

    /**
     * Converts a string by applying the given <code>template</code>, if it matches
     * the given regular expression. The properties of the entity, if specified, are mapped to
     * placeholders of the form <tt>${propertyName}</tt>.
     * Properties of extensions of the entity, if any, are mapped to placeholders of the form
     * <tt>${extension.propertyName}</tt>.
     * The custom properties, if specified, are mapped to placeholders with their respective keys,
     * e.g. property with key <tt>"prop"</tt> is mapped to the placeholder <tt>${prop}</tt>.
     * The placeholders <tt>${1},${2},...</tt> provide the {@link MatchResult#group(int) groups}
     * of the match result.
     *
     * @param s  the string to check.
     * @param pattern the regular expression to apply.
     * @param template  the template to use for the mapping.
     * @param entity  any (extensible) entity.
     * @param properties  additional properties.
     *
     * @return the mapped string, or <code>null</code>, if the string did not match the
     * given regular expression.
     */
    public static String convert(String s, Pattern pattern, String template, EntityBase entity, Map<String,Object> properties) {
        if (s == null || pattern == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(s);
        if (!matcher.matches()) {
            return null;
        }
        if (properties == null) {
            properties = new HashMap<String,Object>();
        }
        // put the project ID as property ${0}
        if (entity instanceof Project) {
            properties.put("0", ((Project)entity).getProjectId()); //$NON-NLS-1$
        }
        // put the groups found by the matcher as properties ${1}, ${2}, ...
        for (int i = 1; i <= matcher.groupCount(); i++) {
            properties.put(Integer.toString(i), matcher.group(i));
        }
        StrLookup propertyResolver = new PropertyLookup(entity, properties);
        StrSubstitutor subst = new StrSubstitutor(propertyResolver);
        return subst.replace(template);
    }
}
