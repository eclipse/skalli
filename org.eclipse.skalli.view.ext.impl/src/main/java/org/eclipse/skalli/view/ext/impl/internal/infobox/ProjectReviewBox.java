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
package org.eclipse.skalli.view.ext.impl.internal.infobox;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.misc.ReviewProjectExt;
import org.eclipse.skalli.view.ext.ExtensionUtil;
import org.eclipse.skalli.view.ext.InfoBox;
import org.eclipse.skalli.view.ext.InfoBoxBase;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

public class ProjectReviewBox extends InfoBoxBase implements InfoBox {

    private static int DEFAULT_PAGE_LENGH = 3;
    private static int MAX_PAGE_LENGH = 10;

    @Override
    public String getIconPath() {
        return "res/icons/review.png"; //$NON-NLS-1$
    }

    @Override
    public String getCaption() {
        return "Ratings & Reviews";
    }

    @Override
    public Component getContent(Project project, ExtensionUtil util) {
        CssLayout layout = new CssLayout() {
            @Override
            protected String getCss(Component c) {
                return "padding-left: 3px;";
            }
        };
        layout.setMargin(false);
        layout.setSizeFull();

        ReviewProjectExt ext = project.getExtension(ReviewProjectExt.class);
        if (ext != null) {
            ReviewComponent reviewComponent = new ReviewComponent(project, DEFAULT_PAGE_LENGH, MAX_PAGE_LENGH, util);
            layout.addComponent(reviewComponent);
        }
        return layout;
    }

    @Override
    public float getPositionWeight() {
        return 3.0f;
    }

    @Override
    public int getPreferredColumn() {
        return COLUMN_WEST;
    }

    @Override
    public boolean isVisible(Project project, String loggedInUserId) {
        if (project.getExtension(ReviewProjectExt.class) != null) {
            return true;
        } else {
            return false;
        }
    }

}
