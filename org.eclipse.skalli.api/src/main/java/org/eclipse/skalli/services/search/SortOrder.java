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
package org.eclipse.skalli.services.search;

/**
 * The sort order to apply to result of a {@link SearchQuery search query}.
 */
public enum SortOrder {

    /** Do not sort the projects. */
    NONE,

    /** Sort projects by their unique identifiers. */
    UUID,

    /** Sort projects by their project identifiers. */
    PROJECT_ID,

    /** Sort projects by their display names. */
    PROJECT_NAME
}