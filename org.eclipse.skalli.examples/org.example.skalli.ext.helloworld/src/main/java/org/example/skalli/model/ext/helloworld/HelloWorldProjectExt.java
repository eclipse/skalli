/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/edl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.example.skalli.model.ext.helloworld;

import java.util.Collection;
import java.util.TreeSet;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.PropertyName;

public class HelloWorldProjectExt extends ExtensionEntityBase {

    public static final String MODEL_VERSION = "1.0";
    public static final String NAMESPACE = "http://org.example.skalli/2011/04/ProjectPortal/Model/Extension-HelloWorld";

    @PropertyName(position = 0)
    public static final String PROPERTY_NAME = "name";

    @PropertyName(position = 1)
    public static final String PROPERTY_FRIENDS = "friends";

    private String name = "";
    private TreeSet<String> friends = new TreeSet<String>();

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Collection<String> getFriends() {
        return this.friends;
    }

    //and some more optional methods for the field friends:
    public void setFriends(Collection<String> friends) {
        this.friends = new TreeSet<String>(friends);
    }

    public void addFriends(Collection<String> additionalFriends) {
        this.friends.addAll(additionalFriends);
    }

    public void addFriend(String friend) {
        this.friends.add(friend);
    }

}
