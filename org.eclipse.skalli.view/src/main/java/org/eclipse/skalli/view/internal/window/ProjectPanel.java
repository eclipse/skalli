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
package org.eclipse.skalli.view.internal.window;

import org.eclipse.skalli.model.Project;

import com.vaadin.ui.Component;

/**
 * Common interface for components that can be embedded in the project window.
 */
public interface ProjectPanel extends Component {
    public Project getProject();
}
