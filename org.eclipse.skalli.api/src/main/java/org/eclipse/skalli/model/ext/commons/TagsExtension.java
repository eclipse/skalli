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
package org.eclipse.skalli.model.ext.commons;

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Taggable;

public class TagsExtension extends ExtensionEntityBase implements Taggable {

    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model/Extension-Tags"; //$NON-NLS-1$

    private TreeSet<String> tags = new TreeSet<String>();

    public TagsExtension() {
    }


    public TagsExtension(String... tags) {
        addTags(tags);
    }

    @Override
    public synchronized void addTag(String tag) {
        if (tags == null) {
            tags = new TreeSet<String>();
        }
        tags.add(tag);
    }

    @Override
    public void addTags(String... tags) {
        if (tags != null) {
            for (String tag: tags) {
                addTag(tag);
            }
        }
    }

    @Override
    public synchronized SortedSet<String> getTags() {
        if (tags == null) {
            tags = new TreeSet<String>();
        }
        return tags;
    }

    @Override
    public synchronized boolean hasTag(String tag) {
        if (tags == null) {
            tags = new TreeSet<String>();
        }
        return tags.contains(tag);
    }

    @Override
    public synchronized void removeTag(String tag) {
        if (tags == null) {
            tags = new TreeSet<String>();
        }
        tags.remove(tag);
    }
}
