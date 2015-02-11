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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.UUIDList;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.services.entity.EntityServices;
import org.eclipse.skalli.services.project.ProjectService;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Select;
import com.vaadin.ui.VerticalLayout;

public class MultiComboBox extends CustomField {

    private static final long serialVersionUID = -2946220818606365985L;

    private static final String STYLE_LAYOUT = "multicombobox-layout";
    private static final String STYLE_LINE_LAYOUT = "multicombobox-line";
    private static final String STYLE_BUTTON = "multicombobox-btn";

    private UUIDList values;
    private VerticalLayout layout;
    private List<ComboBoxElement> comboBoxEntries;
    private String description;
    private String inputPrompt;
    private boolean readOnly;
    private int columns;
    private int maxSize;

    private static class ComboBoxElement {
        public ComboBoxElement(ComboBox comboBox) {
            this.comboBox = comboBox;
            comboBox.setItemCaptionPropertyId(ProjectDataSource.PROPERTY_DISPLAYNAME);
            comboBox.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
            comboBox.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
            comboBox.setImmediate(true);
            comboBox.setNewItemsAllowed(false);
            comboBox.setNullSelectionAllowed(true);
        }

        ComboBox comboBox;
        Button removeButton;
    }

    public MultiComboBox(String caption, UUIDList values) {
        if (values == null) {
            throw new IllegalArgumentException("argument 'values' must not be null");
        }
        setCaption(caption);
        this.values = values;
        this.maxSize = Integer.MAX_VALUE;
        init(values);
        layout = new VerticalLayout();
        layout.setStyleName(STYLE_LAYOUT);
        renderComboBoxes();
        setCompositionRoot(layout);
    }

    private void renderComboBoxes() {
        int size = comboBoxEntries.size();
        int last = size - 1;
        for (int i = 0; i <= last; ++i) {
            ComboBoxElement comboBoxEntry = comboBoxEntries.get(i);
            HorizontalLayout horLayout = new HorizontalLayout();
            horLayout.setStyleName(STYLE_LINE_LAYOUT);
            ComboBox comboBox = comboBoxEntry.comboBox;
            if (comboBox.getValue() == null) {
                comboBox.setEnabled(!readOnly);
            } else {
                comboBox.setReadOnly(readOnly);
            }
            horLayout.addComponent(comboBox);
            if (!readOnly) {
                if (size > 1) {
                    Button b = createRemoveButton();
                    comboBoxEntry.removeButton = b;
                    horLayout.addComponent(b);
                }
                if (size < maxSize && i == last) {
                    horLayout.addComponent(createAddButton());
                }
            }
            layout.addComponent(horLayout);
        }
    }

    private void init(UUIDList values) {
        comboBoxEntries = new ArrayList<ComboBoxElement>();
        if (values != null && !values.isEmpty()) {
            for (UUID value : values) {
                ComboBox comboBox = createComboBox(value);
                comboBoxEntries.add(new ComboBoxElement(comboBox));
            }
        } else {
            ComboBox comboBox = createComboBox(null);
            comboBoxEntries.add(new ComboBoxElement(comboBox));
        }
    }

    private ComboBox createComboBox(UUID uuid) {
        ComboBox comboBox = new ComboBox(null, new ProjectDataSource());
        comboBox.setImmediate(true);
        if (description != null) {
            comboBox.setDescription(description);
        }
        if (inputPrompt != null) {
            comboBox.setInputPrompt(inputPrompt);
        }
        if (uuid != null) {
            ProjectService projectService = ((ProjectService)EntityServices.getByEntityClass(Project.class));
            Project project = projectService.getByUUID(uuid);
            comboBox.select(project);
        }
        return comboBox;
    }

    public void setColumns(int columns) {
        this.columns = columns;
        for (ComboBoxElement entry : comboBoxEntries) {
            entry.comboBox.setWidth(columns, Select.UNITS_EM);
        }
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize <=0 ? Integer.MAX_VALUE : maxSize;
        requestRepaint();
    }

    private Button createAddButton() {
        Button b = new Button("Add");
        b.setStyleName(Button.STYLE_LINK);
        b.addStyleName(STYLE_BUTTON);
        b.setDescription("Add another entry");
        b.setEnabled(!readOnly);
        b.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                ComboBox cb = createComboBox(null);
                cb.setWidth(columns, Select.UNITS_EM);
                comboBoxEntries.add(new ComboBoxElement(cb));
                layout.removeAllComponents();
                renderComboBoxes();
            }
        });
        return b;
    }

    private Button createRemoveButton() {
        Button b = new Button("Remove");
        b.setStyleName(Button.STYLE_LINK);
        b.addStyleName(STYLE_BUTTON);
        b.setDescription("Remove this entry");
        b.setEnabled(!readOnly);
        b.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Button b = event.getButton();
                Iterator<ComboBoxElement> it = comboBoxEntries.iterator();
                while (it.hasNext()) {
                    ComboBoxElement element = it.next();
                    if (element.removeButton == b) {
                        it.remove();
                        break;
                    }
                }
                layout.removeAllComponents();
                renderComboBoxes();
            }
        });
        return b;
    }

    public void setInputPrompt(String inputPrompt) {
        this.inputPrompt = inputPrompt;
        for (ComboBoxElement element : comboBoxEntries) {
            element.comboBox.setInputPrompt(inputPrompt);
        }
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
        for (ComboBoxElement element : comboBoxEntries) {
            element.comboBox.setDescription(description);
        }
    }

    @Override
    public Object getValue() {
        UUIDList uuid = new UUIDList(comboBoxEntries.size());
        copyValues(uuid);
        return uuid;
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        values.clear();
        copyValues(values);
    }

    private void copyValues(UUIDList values) {
        for (ComboBoxElement entry : comboBoxEntries) {
            Project project = (Project) entry.comboBox.getValue();
            if (project == null) {
                return;
            }
            UUID value = project.getUuid();
            if (StringUtils.isNotBlank(value.toString())) {
                values.add(value);
            }
        }
    }

    @Override
    public Class<?> getType() {
        return Collection.class;
    }

    private static class ProjectDataSource extends IndexedContainer {

        private static final long serialVersionUID = 8192530422436522676L;

        @PropertyName(position = 0)
        public static final Object PROPERTY_DISPLAYNAME = "displayName";

        public ProjectDataSource() {
            super();
            addContainerProperty(Project.PROPERTY_NAME, String.class, null);
            addContainerProperty(Project.PROPERTY_PROJECTID, String.class, null);
            addContainerProperty(PROPERTY_DISPLAYNAME, String.class, null);

            ProjectService projectService = ((ProjectService)EntityServices.getByEntityClass(Project.class));
            List<Project> projects = projectService.getAll();
            for (Project project : projects) {
                addItem(project);
            }
            sort(new Object[] { Project.PROPERTY_NAME, Project.PROPERTY_PROJECTID }, new boolean[] { true, true });
        }

        private Item addItem(Project project) {
            // item key = project instance
            Item item = getItem(project);
            if (item == null) {
                item = super.addItem(project); // IndexedContainer#addItem return null, if entry already exists!!!
            }
            if (item != null) {
                String projectId = project.getProjectId();
                String name = project.getName();
                item.getItemProperty(Project.PROPERTY_NAME).setValue(name);
                item.getItemProperty(Project.PROPERTY_PROJECTID).setValue(projectId);
                item.getItemProperty(PROPERTY_DISPLAYNAME).setValue(name + " <" + projectId + ">");
            }
            return item;
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        layout.removeAllComponents();
        renderComboBoxes();
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }
}
