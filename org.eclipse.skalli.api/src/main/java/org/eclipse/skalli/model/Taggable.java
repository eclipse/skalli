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

import java.util.SortedSet;


/**
 * Marker interface for model entities that support tagging.
 */
public interface Taggable {

    @PropertyName
    public static final String PROPERTY_TAGS = "tags"; //$NON-NLS-1$

    public SortedSet<String> getTags();

    public void addTag(String tag);

    public void addTags(String... tags);

    public void removeTag(String tag);

    public boolean hasTag(String tag);
}
