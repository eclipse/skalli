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
package org.eclipse.skalli.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Denotes that an entity has validation issues.
 */
public class ValidationException extends Exception {
    private static final long serialVersionUID = 6730613810858572488L;

    private SortedSet<Issue> issues;

    /**
     * Constructs a new <code>ValidationException</code> with <code>null</code> as
     * its detail message. If {@link #addIssue(Issue) issues are assigned} subsequently
     * to this exception, the detail message is composed from the detail messages of the issue.
     */
    public ValidationException() {
        super();
    }

    /**
     * Constructs a new <code>ValidationException</code> with the given detail message.
     * If {@link #addIssue(Issue) issues are assigned} subsequently to this
     * exception, the detail message is composed from the given detail message and the
     * detail messages of the issue.
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new <code>ValidationException</code> with the specified cause and
     * a default detail message.
     *
     * @param cause  the cause of this exception.
     */
    public ValidationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new <code>ValidationException</code> with the specified cause and
     * detail message.
     *
     * @param message  the detail message of this exception.
     * @param cause  the cause of this exception.
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a <code>ValidationException</code> with a single {@link Severity#FATAL} issue
     * that is related to a certain model extension. The issue is created with a default message.
     *
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param entityId  the unique identifier of an entity.
     * @param extension  the class of a model extension, or <code>null</code>.
     */
    public ValidationException(Class<? extends Issuer> issuer, UUID entityId,
            Class<? extends ExtensionEntityBase> extension) {
        this(issuer, entityId, extension, null, null);
    }

    /**
     * Creates a <code>ValidationException</code> with a single {@link Severity#FATAL} issue
     * that is related to a certain property of a model extension. The issue is created with a default message.
     *
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param entityId  the unique identifier of an entity.
     * @param extension  the class of a model extension, or <code>null</code>.
     * @param propertyId  the property that causes this validation issue, or <code>null</code>.
     */
    public ValidationException(Class<? extends Issuer> issuer, UUID entityId,
            Class<? extends ExtensionEntityBase> extension, String propertyId) {
        this(issuer, entityId, extension, propertyId, null);
    }

    /**
     * Creates a <code>ValidationException</code> with a single {@link Severity#FATAL} issue
     * that is related to a certain property of a model extension. The issue is created with the given error message.
     *
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param entityId  the unique identifier of an entity.
     * @param extension  the class of a model extension, or <code>null</code>.
     * @param propertyId  the property that causes this validation issue, or <code>null</code>.
     * @param message  the error message of the issue, or <code>null</code>.
     */
    public ValidationException(Class<? extends Issuer> issuer, UUID entityId,
            Class<? extends ExtensionEntityBase> extension, String propertyId, String message) {
        this(new Issue(Severity.FATAL, issuer, entityId, extension, propertyId, message));
    }

    /**
     * Creates a <code>ValidationException</code> from the given issues.
     * Note, it is recommended (but not checked!) that the issues all have {@link Severity#FATAL}.
     *
     * @param issues  the issues to wrap as <code>ValidationException</code>.
     */
    public ValidationException(Issue... issues) {
        this(null, issues);
    }

    /**
     * Creates a <code>ValidationException</code> from the given issues.
     * Note, it is recommended (but not checked!) that the issues all have {@link Severity#FATAL}.
     *
     * @param message  the message of the exception. Note that the individual issues may have own messages.
     * @param issues   the issues to wrap as <code>ValidationException</code>.
     */
    public ValidationException(String message, Issue... issues) {
        this(message, Arrays.asList(issues));
    }

    /**
     * Creates a <code>ValidationException</code> from the given collection of issues.
     * Note, that it is recommended (but not checked!) that the issues all have {@link Severity#FATAL}.
     *
     * @param issues  the issues to wrap as <code>ValidationException</code>.
     */
    public ValidationException(Collection<Issue> issues) {
        this(null, issues);
    }

    /**
     * Creates a <code>ValidationException</code> from the given collection ofissues.
     * Note, it is recommended (but not checked!) that the issues all have {@link Severity#FATAL}.
     *
     * @param message  the message of the exception. Note that the individual issues may have own messages.
     * @param issues   the issues to wrap as <code>ValidationException</code>.
     */
    public ValidationException(String message, Collection<Issue> issues) {
        super(message);
        this.issues = new TreeSet<Issue>(issues);
    }

    /**
     * Adds the given <code>issue</code> to this <code>ValidationException</code>.
     *
     * @param issue  the issue to add.
     */
    public synchronized void addIssue(Issue issue) {
        if (issue != null) {
            if (issues == null) {
                issues = new TreeSet<Issue>();
            }
            issues.add(issue);
        }
    }

    /**
     * Returns <code>true</code>, if issues have beed assigned to this <code>ValidationException</code>.
     */
    public boolean hasIssues() {
        return issues != null && issues.size() > 0;
    }

    /**
     * Returns <code>true</code>, if {@link Severity#FATAL} issues have beed assigned
     * to this <code>ValidationException</code>.
     */
    public boolean hasFatalIssues() {
        return getIssues(Severity.FATAL).size() > 0;
    }

    /**
     * Returns the issues assigned to this <code>ValidationException</code>.
     * @return a set of issues sorted by {@link Issue#compareTo(Issue)}, or an empty set.
     */
    public SortedSet<Issue> getIssues() {
        if (issues == null) {
            return new TreeSet<Issue>();
        }
        return issues;
    }

    /**
     * Returns issues that are equal of more serious than the given <code>minSeverity</code>.
     *
     * @param minSeverity  the minimal severity of issues to include in the result.
     * @return a set of issues sorted by {@link Issue#compareTo(Issue)}, or an empty set.
     */
    public SortedSet<Issue> getIssues(Severity minSeverity) {
        return Issue.filterBySeverity(issues, minSeverity);
    }

    /**
     * Composes the detail message string of this <code>ValidationException</code> from
     * the {@link Throwable#getMessage() detail message of the exception} and the
     * {@link Issue#getMessage() detail messages of the assigned issues}. The messages of the issues
     * are appended in form of a bulleted list (using <tt>"-"</tt> as bullet) in the order defined by
     * {@link Issue#compareTo(Issue)}. If the exception has no own detail message, then only the
     * list of issue messages is returned. If there is only a single issue assigned, then
     * {@link Issue#getMessage()} is returned without leading bullet.
     */
    @Override
    public String getMessage() {
        return Issue.getMessage(super.getMessage(), issues);
    }
}
