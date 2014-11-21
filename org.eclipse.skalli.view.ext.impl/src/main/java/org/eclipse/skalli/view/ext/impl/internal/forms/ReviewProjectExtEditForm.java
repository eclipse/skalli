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
package org.eclipse.skalli.view.ext.impl.internal.forms;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.misc.ProjectRatingStyle;
import org.eclipse.skalli.model.ext.misc.ReviewProjectExt;
import org.eclipse.skalli.view.ext.AbstractExtensionFormService;
import org.eclipse.skalli.view.ext.DefaultProjectFieldFactory;
import org.eclipse.skalli.view.ext.ProjectEditContext;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;

public class ReviewProjectExtEditForm extends AbstractExtensionFormService<ReviewProjectExt> {

    @Override
    public String getIconPath() {
        return "res/icons/review.png"; //$NON-NLS-1$
    }

    @Override
    public float getRank() {
        return 1.5f;
    }

    @Override
    protected FormFieldFactory getFieldFactory(Project project, ProjectEditContext context) {
        return new FieldFactory(project, context);
    }

    @Override
    protected Item getItemDataSource(Project project) {
        return new BeanItem<ReviewProjectExt>(getExtension(project));
    }

    private class FieldFactory extends DefaultProjectFieldFactory<ReviewProjectExt> {

        private static final long serialVersionUID = 789181533778336279L;

        private static final String STYLE_ICON = "styleIcon"; //$NON-NLS-1$
        private static final String STYLE_CAPTION = "styleCaption"; //$NON-NLS-1$

        private ReviewProjectExt extension;

        public FieldFactory(Project project, ProjectEditContext context) {
            super(project, ReviewProjectExt.class, context);
            this.extension = getExtension(project);
        }

        @Override
        protected Field createField(Object propertyId, String caption) {
            Field field = null;
            if (ReviewProjectExt.PROPERTY_RATING_STYLE.equals(propertyId)) {
                ComboBox cb = new ComboBox(caption, getStylesContainer());
                cb.setWidth("300px"); //$NON-NLS-1$
                cb.setItemCaptionPropertyId(STYLE_CAPTION);
                cb.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
                cb.setItemIconPropertyId(STYLE_ICON);
                cb.setNullSelectionAllowed(false);
                cb.select(extension.getRatingStyle());
                field = cb;
            }
            return field;
        }

        public IndexedContainer getStylesContainer() {
            IndexedContainer c = new IndexedContainer();
            c.addContainerProperty(STYLE_CAPTION, String.class, null);
            c.addContainerProperty(STYLE_ICON, Resource.class, null);
            Item item = c.addItem(ProjectRatingStyle.TWO_STATES);
            item.getItemProperty(STYLE_CAPTION).setValue(" \"Caesar\" Style (thumbs up / thumbs down) ");
            item.getItemProperty(STYLE_ICON).setValue(new ThemeResource("icons/rating/thumbs.png")); //$NON-NLS-1$
            item = c.addItem(ProjectRatingStyle.FIVE_STATES);
            item.getItemProperty(STYLE_CAPTION).setValue(" \"Smiley\" Style (5 levels of consent) ");
            item.getItemProperty(STYLE_ICON).setValue(new ThemeResource("icons/rating/faces.png")); //$NON-NLS-1$
            return c;
        }
    }

    @Override
    public Class<ReviewProjectExt> getExtensionClass() {
        return ReviewProjectExt.class;
    }

    @Override
    public ReviewProjectExt newExtensionInstance() {
        return new ReviewProjectExt();
    }
}
