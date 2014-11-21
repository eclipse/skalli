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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.skalli.model.Project;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Layout;

public class PhaseSelect extends CustomField {

    private static final long serialVersionUID = -8351312392930159187L;

    private Project project;
    private List<String> phases;
    private ComboBox cb;
    private CheckBox deleted;
    private boolean readOnly;

    public PhaseSelect(String caption, Collection<?> phases, Project project, boolean showDeletedCheckbox) {
        setCaption(caption);
        this.project = project;
        this.phases = new ArrayList<String>();
        if (phases != null) {
            if (!phases.contains(project.getPhase())) {
                addPhase(project.getPhase());
            }
            for (Object phase : phases) {
                addPhase(phase.toString());
            }
        }
        Layout layout = createLayout(showDeletedCheckbox);
        setCompositionRoot(layout);
    }

    private void addPhase(String phase) {
        phases.add(phase);
    }

    private Layout createLayout(boolean showDeletedCheckbox) {
        final FloatLayout layout = new FloatLayout();
        cb = new ComboBox(null, phases);
        cb.setImmediate(true);
        layout.addComponent(cb);
        if (showDeletedCheckbox) {
            deleted = new CheckBox("Deleted", project.isDeleted());
            deleted.setImmediate(true);
            layout.addComponent(deleted, "margin-left:20px;margin-top:3px");
        }
        return layout;
    }

    public void setFilteringMode(int filteringMode) {
        cb.setFilteringMode(filteringMode);
    }

    public void setNewItemsAllowed(boolean allowNewOptions) {
        cb.setNewItemsAllowed(allowNewOptions);
    }

    public void setNullSelectionAllowed(boolean nullSelectionAllowed) {
        cb.setNullSelectionAllowed(nullSelectionAllowed);
    }

    public void select(Object itemId) {
        cb.select(itemId);
    }

    public boolean isDeleted() {
        return deleted != null ? ((Boolean) deleted.getValue()).booleanValue() : project.isDeleted();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        cb.setReadOnly(readOnly);
        if (deleted != null) {
            deleted.setReadOnly(readOnly);
        }
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        validate();
        cb.commit();
        project.setDeleted(isDeleted());
        project.setPhase((String) cb.getValue());
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }
}
