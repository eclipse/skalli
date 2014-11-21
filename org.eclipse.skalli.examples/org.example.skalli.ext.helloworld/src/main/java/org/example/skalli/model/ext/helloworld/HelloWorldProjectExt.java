/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/edl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.example.skalli.model.ext.helloworld;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;

public class HelloWorldProjectExt extends ExtensionEntityBase {

    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://org.example.skalli/2011/04/ProjectPortal/Model/Extension-HelloWorld"; //$NON-NLS-1$

    @PropertyName(position = 0)
    public static final String PROPERTY_NAME = "name"; //$NON-NLS-1$

    @PropertyName(position = 1)
    public static final String PROPERTY_FRIENDS = "friends"; //$NON-NLS-1$

    private String name = ""; //$NON-NLS-1$
    private TreeSet<String> friends = new TreeSet<String>();

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Set<String> getFriends() {
        return this.friends;
    }

    public boolean hasFriend(String friend) {
        return friends.contains(friend);
    }

    public void addFriend(String friend) {
        this.friends.add(friend);
    }

    public void removeFriend(String friend) {
        friends.remove(friend);
    }
}
