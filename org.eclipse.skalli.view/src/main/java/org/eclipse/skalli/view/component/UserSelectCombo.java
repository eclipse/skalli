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
package org.eclipse.skalli.view.component;

import org.eclipse.skalli.model.User;
import org.eclipse.skalli.view.internal.container.UserContainer;

import com.vaadin.ui.ComboBox;

public class UserSelectCombo extends ComboBox {

    private static final long serialVersionUID = 3342826866111425927L;

    public UserSelectCombo(String caption, String width) {
        super(caption);
        setWidth(width);
        setContainerDataSource(UserContainer.getInstance());
        setItemCaptionPropertyId(User.PROPERTY_DISPLAY_NAME);
        setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
        setImmediate(true);
    }

}
