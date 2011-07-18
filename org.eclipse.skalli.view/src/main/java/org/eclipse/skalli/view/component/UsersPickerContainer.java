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
package org.eclipse.skalli.view.component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Locale;

import org.eclipse.skalli.common.User;
import org.eclipse.skalli.model.ext.PropertyName;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

class UsersPickerContainer extends IndexedContainer implements Serializable {

    private static final long serialVersionUID = 632503436661743735L;

    @PropertyName(position = 0)
    public static final Object PROPERTY_USER = "user";

    private UsersPickerContainer(Collection<User> users) {
        super();
        addContainerProperty(User.PROPERTY_FIRSTNAME, String.class, null);
        addContainerProperty(User.PROPERTY_LASTNAME, String.class, null);
        addContainerProperty(User.PROPERTY_EMAIL, String.class, null);
        addContainerProperty(User.PROPERTY_DISPLAY_NAME, String.class, null);
        addContainerProperty(PROPERTY_USER, User.class, null);
        for (User user : users) {
            addItem(user);
        }
    }

    public Item addItem(User user) {
        String userId = user.getUserId().toLowerCase(Locale.ENGLISH);
        Item item = getItem(userId);
        if (item == null) {
            item = addItem(userId); // IndexedContainer#addItem return null, if entry already exists!!!
        }
        if (item != null) {
            item.getItemProperty(User.PROPERTY_FIRSTNAME).setValue(user.getFirstname());
            item.getItemProperty(User.PROPERTY_LASTNAME).setValue(user.getLastname());
            item.getItemProperty(User.PROPERTY_EMAIL).setValue(user.getEmail());
            item.getItemProperty(User.PROPERTY_DISPLAY_NAME).setValue(user.getDisplayName());
            item.getItemProperty(PROPERTY_USER).setValue(user);
        }
        return item;
    }

    public static synchronized UsersPickerContainer newInstance(Collection<User> users) {
        UsersPickerContainer container = new UsersPickerContainer(users);
        return container;
    }

}
