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
package org.eclipse.skalli.model;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.ComparatorUtils;

/**
 * Class for reporting of validation issues.
 * A validation issue always has a {@link Severity severity}, is assigned to a certain entity
 * and has a message. If no explicit message is defined, a default message is assigned to
 * an issue.<br>
 * Besides that an issue may be related to a certain extension and/or to a certain property
 * of that extension. Optionally an issue can have a description (e.g. a hint how to solve
 * the issue) and a timestamp.
 */
public class Issue implements Comparable<Issue> {

    private Severity severity;
    private Class<? extends Issuer> issuer;
    private UUID entityId;
    private Class<? extends ExtensionEntityBase> extension;
    private Object propertyId;
    private int item;
    private String message;
    private String description;
    private long timestamp;

    /**
     * Default constructor. Required for XML streaming.
     */
    public Issue() {
    }

    /**
     * Creates an issue with the given <code>severity</code> for an entity specified by
     * its unique identifier. The issue is created with a default message.
     *
     * @param severity  the severity of the issue.
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param entityId  the unique identifier of the entity that causes this validation issue.
     */
    public Issue(Severity severity, Class<? extends Issuer> issuer, UUID entityId) {
        this(severity, issuer, entityId, null, null, 0, null);
    }

    /**
     * Creates an issue with the given <code>severity</code> and custom message.
     *
     * @param severity  the severity of the issue.
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param message   the message of the issue.
     */
    public Issue(Severity severity, Class<? extends Issuer> issuer, String message) {
        this(severity, issuer, null, null, null, 0, message);
    }

    /**
     * Creates an issue with the given <code>severity</code> and custom message for an entity
     * specified by its unique identifier.
     *
     * @param severity  the severity of the issue.
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param entityId  the unique identifier of the entity that causes this validation issue.
     * @param message   the message of the the issue.
     */
    public Issue(Severity severity, Class<? extends Issuer> issuer, UUID entityId, String message) {
        this(severity, issuer, entityId, null, null, 0, message);
    }

    /**
     * Creates an issue with the given <code>severity</code> for a property of a model extension that is
     * assigned to an entity specified by its unique identifier. The issue is created with a default message.
     *
     * @param severity  the severity of the issue.
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param entityId  the unique identifier of an entity.
     * @param extension  the class of a model extension, or <code>null</code>.
     * @param propertyId  the property that causes this validation issue, or <code>null</code>.
     */
    public Issue(Severity severity, Class<? extends Issuer> issuer, UUID entityId,
            Class<? extends ExtensionEntityBase> extension, Object propertyId) {
        this(severity, issuer, entityId, extension, propertyId, 0, null);
    }

    /**
     * Creates an issue with the given <code>severity</code> and custom message for a property of a model
     * extension that is assigned to an entity specified by its unique identifier.
     *
     * @param severity  the severity of the issue.
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param entityId  the unique identifier of the entity that causes this validation issue.
     * @param extension  the class of a model extension, or <code>null</code>.
     * @param propertyId  the property that causes this validation issue, or <code>null</code>.
     * @param message   the message of the the issue.
     */
    public Issue(Severity severity, Class<? extends Issuer> issuer, UUID entityId,
            Class<? extends ExtensionEntityBase> extension, Object propertyId, String message) {
        this(severity, issuer, entityId, extension, propertyId, 0, message);
    }

    /**
     * Creates an issue with the given <code>severity</code> and custom message for a property of a model
     * extension that is assigned to an entity specified by its unique identifier.
     *
     * @param severity  the severity of the issue.
     * @param issuer    the issuer that raises this issue, e.g. a validator.
     * @param entityId  the unique identifier of the entity that causes this validation issue.
     * @param extension  the class of a model extension, or <code>null</code>.
     * @param propertyId  the property that causes this validation issue, or <code>null</code>.
     * @param item  a unique item number that distinguishes issues related to the
     * same property/extension/entity/issuer.
     * @param message   the message of the the issue.
     */
    public Issue(Severity severity, Class<? extends Issuer> issuer, UUID entityId,
            Class<? extends ExtensionEntityBase> extension, Object propertyId, int item, String message) {
        if (severity == null) {
            throw new IllegalArgumentException("argument 'severity' must not be null");
        }
        if (issuer == null) {
            throw new IllegalArgumentException("argument 'issuer' must not be null");
        }
        this.severity = severity;
        this.issuer = issuer;
        this.entityId = entityId;
        this.extension = extension;
        this.propertyId = propertyId;
        this.item = item;
        this.message = message;
    }

    /**
     * Returns the severity of this issue.
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Returns the issuer that raised this issue, e.g. a validator.
     */
    public Class<? extends Issuer> getIssuer() {
        return issuer;
    }

    /**
     * Returns the unique identifier of the entity that causes this issue.
     */
    public UUID getEntityId() {
        return entityId;
    }

    /**
     * Returns the message of this issue. If no custom message has been defined,
     * a default message is created from the other parameters of the issue.
     */
    public String getMessage() {
        if (StringUtils.isNotBlank(message)) {
            return message;
        }
        String msg = null;
        if (entityId != null) {
            if (extension != null) {
                if (propertyId != null) {
                    msg = MessageFormat.format("Property {0} of extension {1} of entity {2} is invalid",
                            propertyId, extension.getName(), entityId);
                } else {
                    msg = MessageFormat.format("Extension {0} of entity {1} is invalid",
                            extension.getName(), entityId);
                }
            } else {
                msg = MessageFormat.format("Entity {0} is invalid", entityId);
            }
        } else {
            if (extension != null) {
                if (propertyId != null) {
                    msg = MessageFormat.format("Property {0} of extension {1} is invalid",
                            propertyId, extension.getName());
                } else {
                    msg = MessageFormat.format("Extension {0} is invalid",
                            extension.getName());
                }
            } else {
                msg = "invalid";
            }

        }
        return msg;
    }

    /**
     * Sets the message of this issue.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the class of the model extension that causes this issue.
     */
    public Class<? extends ExtensionEntityBase> getExtension() {
        return extension;
    }

    /**
     * Sets the class of the model extension that causes this issue.
     */
    public void setExtension(Class<? extends ExtensionEntityBase> extension) {
        this.extension = extension;
    }

    /**
     * Returns the identifier of the property that causes this issue.
     * @see {@link org.eclipse.skalli.services.projects.PropertyName}
     */
    public Object getPropertyId() {
        return propertyId;
    }

    /**
     * Sets the identifier of the property that causes this issue.
     * @see {@link org.eclipse.skalli.services.projects.PropertyName}
     */
    public void setPropertyId(Object propertyId) {
        this.propertyId = propertyId;
    }

    /**
     * Returns a unique item number that distinguishes issues related
     * to the same property/extension/entity/issuer.
     */
    public int getItem() {
        return item;
    }

    /**
     * Sets a unique item number that distinguishes issues related
     * to the same property/extension/entity/issuer.
     */
    public void setItem(int item) {
        this.item = item;
    }

    /**
     * Returns the description of this issue.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this issue.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the timestanp of this issue.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestanp of this issue.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Issue) {
            return compareTo((Issue) obj) == 0;
        }
        return false;
    }

    /**
     * Compares two issues according to their severity ({@link Severity.FATAL} first),
     * entity id, extension class name, property id and issuer (in that order).
     * Issues related to a whole entity (no extension, no property) are consider to be less
     * than issues related to a whole extension (extension set, but no property).
     * Issues related to a whole extension (extension set, but no property) are consider to be less
     * than issues related to a certain property (extension and property both).
     */
    @Override
    public int compareTo(Issue issue) {
        int result = severity.compareTo(issue.severity);
        if (result == 0) {
            result = ComparatorUtils.compare(entityId, issue.entityId);
            if (result == 0) {
                result = ComparatorUtils.compareAsStrings(extension, issue.extension);
                if (result == 0) {
                    result = ComparatorUtils.compareAsStrings(propertyId, issue.propertyId);
                    if (result == 0) {
                        result = ComparatorUtils.compare(issuer.getName(), issue.issuer.getName());
                        if (result == 0) {
                            result = item < issue.item ? -1 : (item == issue.item ? 0 : 1);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * This method returns {@link #getMessage()}.
     */
    @Override
    public String toString() {
        return getMessage();
    }

    /**
     * Composes a message from a given message and the {@link Issue#getMessage() detail messages
     * of the given issues. The messages of the issues are appended in form of a bulleted list
     * (using <tt>"-"</tt> as bullet) in the order defined by {@link Issue#compareTo(Issue)}.
     * If no explicit <code>message</code> is specified, then only the list of issue messages
     * is returned. If there is only a single issue given, then {@link Issue#getMessage()} is
     * returned without leading bullet.
     */
    @SuppressWarnings("nls")
    public static String getMessage(String message, SortedSet<Issue> issues) {
        StringBuilder sb = new StringBuilder();
        boolean hasMessage = StringUtils.isNotBlank(message);
        if (hasMessage) {
            sb.append(message);
        }
        if (issues != null) {
            int n = issues.size();
            int i = 0;
            for (Issue issue : issues) {
                message = issue.getMessage();
                if (StringUtils.isNotBlank(message)) {
                    if (hasMessage && i == 0 || i > 0) {
                        sb.append("\n");
                    }
                    if (hasMessage || n > 1) {
                        sb.append(" - ");
                    }
                    sb.append(message);
                    ++i;
                }
            }
        }
        return sb.toString();
    }

    /**
     * Renders the given message and set of issues as HTML bulleted list (&lt;ut&gt;) with
     * the message as caption. If not message is specified, only the bulleted list is rendered.
     * Example:
     * <ul>
     * <li style="color:#8a1f11;background-color:#fbe3e4;border-color:#fbc2c4;border-style:solid;border-width:1px;
     * margin: 2px 0px 2px 0px;list-style-type:none;padding:2px 2px 2px 15px;width:300px;min-height:20px;">
     * <b>ERROR</b>&nbsp;&nbsp;Description must not be empty</li>
     * </ul>
     *
     * @param message  the message to render as caption of the list, or <code>null</code>.
     * @param issues  the set of issues to render, or <code>null</code>.
     */
    public static String asHTMLList(String message, Set<Issue> issues) {
        return asHTMLList(message, issues, null);
    }

    /**
     * Renders the given message and set of issues as HTML bulleted list (&lt;ut&gt;) with
     * the message as caption. If not message is specified, only the bulleted list is rendered.
     * Example:
     * <ul>
     * <li style="color:#8a1f11;background-color:#fbe3e4;border-color:#fbc2c4;border-style:solid;border-width:1px;
     * margin: 2px 0px 2px 0px;list-style-type:none;padding:2px 2px 2px 15px;width:600px;min-height:20px;">
     * <b>ERROR</b>&nbsp;&nbsp;Development Infrastructure:&nbsp;SCM location is invalid</li>
     * </ul>
     * @param message  the message to render as caption of the list, or <code>null</code>.
     * @param issues  the set of issues to render, or <code>null</code>.
     * @param displayNames  the display names of extensions that are referenced by the given issues. If for
     * a given issue the display name of the extension it belongs to is known, the display name is
     * rendered (as link with <tt>href="#&lt;extensionName&gt;"</tt>) between the severity label an the
     * message of the issue.
     */
    @SuppressWarnings("nls")
    public static String asHTMLList(String message, Set<Issue> issues, Map<String, String> displayNames) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(message)) {
            sb.append(message);
        }
        if (issues != null && issues.size() > 0) {
            sb.append("<ul>");
            for (Issue issue : issues) {
                sb.append("<li class=\"").append(issue.getSeverity().name()).append("\">");
                sb.append("<strong>").append(issue.getSeverity().name()).append("</strong>&nbsp;&nbsp;");
                if (displayNames != null) {
                    Class<? extends ExtensionEntityBase> extension = issue.getExtension();
                    if (extension != null && displayNames.containsKey(extension.getName())) {
                        sb.append("<a href=\"#" + extension.getName() + "\">");
                        sb.append(displayNames.get(extension.getName())).append("</a>:&nbsp;");
                    }
                }
                sb.append(issue.getMessage());
                sb.append("</li>");
            }
            sb.append("</ul>");
        }
        return sb.toString();
    }

    /**
     * Returns issues that are equal of more serious than the given <code>minSeverity</code>.
     *
     * @param minSeverity  the minimal severity of issues to include in the result.
     * @return a set of issues sorted by {@link Issue#compareTo(Issue)}, or an empty set.
     */
    public static SortedSet<Issue> filterBySeverity(SortedSet<Issue> issues, Severity minSeverity) {
        TreeSet<Issue> result = new TreeSet<Issue>();
        if (issues != null) {
            for (Issue issue : issues) {
                if (issue.getSeverity().compareTo(minSeverity) <= 0) {
                    result.add(issue);
                }
            }
        }
        return result;
    }

    /**
     * Returns <code>true</code>, if {@link Severity#FATAL} issues are present
     * in the given set of issues.
     */
    public static boolean hasFatalIssues(SortedSet<Issue> issues) {
        return filterBySeverity(issues, Severity.FATAL).size() > 0;
    }
}
