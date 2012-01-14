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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.PropertyValidator;

/**
 * A <code>PropertyValidator</code> which allows to validate the occurrence of a property.
 * This validator can be applied to single-valued properties and to {@link java.util.Collection collections}.
 */
public class MinMaxOccurrencesPropertyValidator implements PropertyValidator, Issuer {

    private Severity severity;
    private Class<? extends ExtensionEntityBase> extension;
    private int maxAllowedOccurrences;
    private int minExpectedOccurrences;
    private String propertyCaption;
    private String extensionCaption;

    /**
     * Returns a <code>PropertyValidator</code> instance for a property which checks number of occurrences of the property.
     *
     * @param severity  the severity that should be assigned to reported issues.
     * @param extension  the class of the model extension the property belongs to.
     * @param propertyName  the name of a property (see {@link PropertyName}).
     * @param minExpectedOccurrences the minimal expected number of occurrences. 0 makes the property optional.
     * @param maxAllowedOccurrences the maximal allowed number of occurrences of the property, or <code>Integer.MAX_VALUE</code> if there is no limit.
     */
    public MinMaxOccurrencesPropertyValidator(Severity severity, Class<? extends ExtensionEntityBase> extension,
            String extensionCaption, String propertyCaption, int minExpectedOccurrences, int maxAllowedOccurrences) {
        if (severity == null) {
            throw new IllegalArgumentException("argument 'severity' must not be null");
        }
        if (extension == null) {
            throw new IllegalArgumentException("argument 'extension' must not be null");
        }

        if (StringUtils.isBlank(extensionCaption)) {
            throw new IllegalArgumentException("argument 'extensionCaption' must not be null or an empty string");
        }
        if (StringUtils.isBlank(propertyCaption)) {
            throw new IllegalArgumentException("argument 'propertyCaption' must not be null or an empty string");
        }

        if (minExpectedOccurrences < 0) {
            throw new IllegalArgumentException("argument 'minExpectedOccurrences' must be greater or equal 0");
        }

        if (maxAllowedOccurrences < 0) {
            throw new IllegalArgumentException("argument 'maxAllowedOccurrences' must be greater or equal 0 or null");
        }

        if (minExpectedOccurrences > maxAllowedOccurrences)
        {
            throw new IllegalArgumentException(
                    "argument 'minExpectedOccurrences' has to be less or equal 'maxAllowedOccurrences'");
        }

        this.severity = severity;
        this.extension = extension;
        this.propertyCaption = propertyCaption;
        this.extensionCaption = extensionCaption;
        this.maxAllowedOccurrences = maxAllowedOccurrences;
        this.minExpectedOccurrences = minExpectedOccurrences;
    }

    @Override
    public SortedSet<Issue> validate(UUID entity, Object value, Severity minSeverity) {
        TreeSet<Issue> issues = new TreeSet<Issue>();

        if (severity.compareTo(minSeverity) > 0) {
            return issues;
        }

        int occurrences = 0;

        if (value != null) {
            occurrences = 1;
            if (value instanceof Collection<?>) {
                occurrences = ((Collection<?>) value).size();
            }
        }

        if (occurrences > maxAllowedOccurrences) {
            String msg = MessageFormat
                    .format(
                            "Property ''{0}'' of extension ''{1}'' should not have more than {2} values, but it currently has {3} values.",
                            propertyCaption, extensionCaption, maxAllowedOccurrences, occurrences);
            issues.add(new Issue(severity, getClass(), entity, extension, propertyCaption, msg));
        }

        if (occurrences < minExpectedOccurrences) {
            String msg = MessageFormat
                    .format(
                            "Property ''{0}'' of extension ''{1}'' should have at least {2} values, but it currently has only {3} values.",
                            propertyCaption, extensionCaption, minExpectedOccurrences, occurrences);
            issues.add(new Issue(severity, getClass(), entity, extension, propertyCaption, msg));
        }
        return issues;
    }

}
