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
package org.eclipse.skalli.view.ext;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.User;

public interface Navigator {

    public void navigateProjectView(Project project);

    public void navigateProjectEditView(Project project);

    public void navigateProjectNewView();

    public void navigateBrowseView();

    public void navigateWelcomeView();

    public void navigateTagView(String tag);

    public void navigateSearchResultView(String query);

    public void navigateUserView(User user);

    public void navigateLoginUserView();

    public void navigateAllTagView();

    public void showFeedbackWindow();

}
