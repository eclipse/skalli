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

/**
 * Enumeration to denote the severity of an {@link Issue}.
 */
public enum Severity {

    /**
     * The issue is fatal, i.e. the entity cannot be persisted before
     * the issue is resolved.
     */
    FATAL,

    /**
     * The issue is serious but the entity can still be persisted.
     * Example: URL is a valid URL but points to a non-existing host.
     */
    ERROR,

    /**
     * The issue is not serious, but should be fixed.
     * Example: An extensible entity has an optional extension, but
     * that extension is in its initial state and should be either
     * removed from the entity or some data should be maintained.
     */
    WARNING,

    /**
     * Not an issue at all, but a hint how the data quality of an entity could
     * be improved further.
     */
    INFO
}
