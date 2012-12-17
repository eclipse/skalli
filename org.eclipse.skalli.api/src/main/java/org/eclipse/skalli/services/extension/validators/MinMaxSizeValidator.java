/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.services.extension.validators;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.HtmlUtils;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.PropertyValidator;

/**
 * Property validator to check the number of entries of a collection-like property.
 */
public class MinMaxSizeValidator implements PropertyValidator, Issuer {

    private Severity severity;
    private Class<? extends ExtensionEntityBase> extension;
    private int minSize;
    private int maxSize;
    private String property;
    private String caption;

    /**
     * Creates a validator to check the number of occurences of a property.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to.
     * @param property  the name of a property (see {@link PropertyName}).
     * @param caption  the caption of the property as shown to the user in the UI form.
     * @param minSize the minimal expected number of entries, or zero if the property is optional.
     * @param maxSize the maximal allowed expected number of entries,
     * or <code>Integer.MAX_VALUE</code> if there is no limit.
     */
    public MinMaxSizeValidator(Severity severity, Class<? extends ExtensionEntityBase> extension,
            String property, String caption, int minSize, int maxSize) {
        if (severity == null) {
            throw new IllegalArgumentException("argument 'severity' must not be null"); //$NON-NLS-1$
        }
        if (extension == null) {
            throw new IllegalArgumentException("argument 'extension' must not be null"); //$NON-NLS-1$
        }
        if (StringUtils.isBlank(property)) {
            throw new IllegalArgumentException("argument 'property' must not be null or an empty string"); //$NON-NLS-1$
        }
        if (minSize < 0) {
            throw new IllegalArgumentException("argument 'minSize' must be greater or equal 0"); //$NON-NLS-1$
        }
        if (maxSize < 0) {
            throw new IllegalArgumentException("argument 'maxSize' must be greater or equal 0 or null"); //$NON-NLS-1$
        }
        if (minSize > maxSize) {
            throw new IllegalArgumentException("argument 'minSize' must be less or equal 'maxSize'"); //$NON-NLS-1$
        }

        this.severity = severity;
        this.extension = extension;
        this.property = property;
        this.caption = caption;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    protected String getAboveMaxMessage(int size) {
        if (maxSize == 1) {
            if (StringUtils.isNotBlank(caption)) {
                return HtmlUtils.formatEscaped("{0} should have at most one entry, but it currently has {1}",
                        caption, size);
            } else {
                return HtmlUtils.formatEscaped("Property ''{0}'' should have at most one entry, but it currently has {1}",
                        property, size);
            }
        }
        if (StringUtils.isNotBlank(caption)) {
            return HtmlUtils.formatEscaped("{0} should have at most {1} entries, but it currently has {2}",
                    caption, maxSize, size);
        } else {
            return HtmlUtils.formatEscaped("Property ''{0}'' should have at most {1} entries, but it currently has {2}",
                    property, maxSize, size);
        }
    }

    protected String getBelowMinMessage(int size) {
        if (minSize == 1) {
            if (StringUtils.isNotBlank(caption)) {
                return HtmlUtils.formatEscaped("{0} should have at least one entry", caption);
            } else {
                return HtmlUtils.formatEscaped("Property ''{0}'' should have at least one entry", property);
            }
        }
        if (StringUtils.isNotBlank(caption)) {
            return HtmlUtils.formatEscaped("{0} should have at least {1} entries, but it currently has only {2}",
                    caption, minSize, size);
        } else {
            return HtmlUtils.formatEscaped(
                    "Property ''{0}'' should have at least {1} entries, but it currently has only {2}",
                    property, minSize, size);
        }
    }

    @Override
    public SortedSet<Issue> validate(UUID entity, Object value, Severity minSeverity) {
        TreeSet<Issue> issues = new TreeSet<Issue>();

        if (severity.compareTo(minSeverity) > 0) {
            return issues;
        }

        int size = 0;
        if (value != null) {
            size = 1;
            if (value instanceof Collection<?>) {
                size = ((Collection<?>) value).size();
            }
        }

        if (size > maxSize) {
            String msg = getAboveMaxMessage(size);
            issues.add(new Issue(severity, getClass(), entity, extension, property, msg));
        }

        if (size < minSize) {
            String msg = getBelowMinMessage(size);
            issues.add(new Issue(severity, getClass(), entity, extension, property, msg));
        }
        return issues;
    }

}
