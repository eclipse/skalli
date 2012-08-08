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
package org.eclipse.skalli.view.ext.impl.internal.forms;

import java.util.Iterator;
import java.util.List;

import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.project.ProjectService;
import org.eclipse.skalli.services.template.NoSuchTemplateException;
import org.eclipse.skalli.services.template.ProjectTemplate;
import org.eclipse.skalli.services.template.ProjectTemplateService;
import org.eclipse.skalli.view.component.PhaseSelect;
import org.eclipse.skalli.view.ext.AbstractExtensionFormService;
import org.eclipse.skalli.view.ext.DefaultProjectFieldFactory;
import org.eclipse.skalli.view.ext.ProjectEditContext;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.TextField;

public class ProjectBasicsEditForm extends AbstractExtensionFormService<Project> {

    @Override
    public String getIconPath() {
        return DEFAULT_ICON;
    }

    @Override
    public float getRank() {
        return 0.0f; //always first position
    }

    @Override
    protected String[] getVisibleItemProperties() {
        return new String[] {
                Project.PROPERTY_PROJECTID,
                Project.PROPERTY_NAME,
                Project.PROPERTY_SHORT_NAME,
                Project.PROPERTY_DESCRIPTION_FORMAT,
                Project.PROPERTY_DESCRIPTION,
                Project.PROPERTY_TEMPLATEID,
                Project.PROPERTY_PARENT_ENTITY,
                Project.PROPERTY_PHASE
        };
    }

    @Override
    protected FormFieldFactory getFieldFactory(Project project, ProjectEditContext context) {
        return new FieldFactory(project, context);
    }

    @Override
    protected Item getItemDataSource(Project project) {
        return new BeanItem<Project>(project);
    }

    private class FieldFactory extends DefaultProjectFieldFactory<Project> {
        private static final long serialVersionUID = 8076143381081743513L;
        private Project project;

        public FieldFactory(Project project, ProjectEditContext context) {
            super(project, Project.class, context);
            this.project = project;
        }

        @Override
        protected Field createField(Object propertyId, String caption) {
            Field field = null;
            if (Project.PROPERTY_TEMPLATEID.equals(propertyId)) {
                field = new ComboBox(caption, new ProjectTemplateDataSource(project));
            } else if (Project.PROPERTY_PARENT_ENTITY.equals(propertyId)) {
                field = new ComboBox(caption, new ProjectParentDataSource(project));
            } else if (Project.PROPERTY_PHASE.equals(propertyId)) {
                field = new PhaseSelect(caption,
                        projectTemplate.getAllowedValues(getExtensionClass().getName(), Project.PROPERTY_PHASE),
                        project, isAdmin);
            } else if (Project.PROPERTY_DESCRIPTION_FORMAT.equals(propertyId)){
               field = new ComboBox(caption, Project.TEXT_FORMATS);
            }
            return field;
        }

        @SuppressWarnings({ "serial" })
        @Override
        protected void initializeField(Object propertyId, Field field) {
            if (Project.PROPERTY_TEMPLATEID.equals(propertyId)) {
                ComboBox cb = (ComboBox) field;
                cb.setItemCaptionPropertyId(ProjectTemplateDataSource.PROPERTY_DISPLAYNAME);
                cb.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
                cb.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
                cb.setNewItemsAllowed(false);
                cb.setNullSelectionAllowed(true);
                cb.setNullSelectionItemId(ProjectTemplate.DEFAULT_ID);
                cb.select(project.getProjectTemplateId());
            } else if (Project.PROPERTY_PARENT_ENTITY.equals(propertyId)) {
                final ComboBox cb = (ComboBox) field;
                cb.setItemCaptionPropertyId(ProjectParentDataSource.PROPERTY_DISPLAYNAME);
                cb.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
                cb.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
                cb.setNewItemsAllowed(false);
                cb.setNullSelectionAllowed(true);
                cb.select(project.getParentEntity());
                cb.addListener(new ValueChangeListener() {
                    @Override
                    public void valueChange(ValueChangeEvent event) {
                        context.onPropertyChanged(EntityBase.class.getName(),
                                EntityBase.PROPERTY_PARENT_ENTITY, (EntityBase) cb.getValue());
                    }
                });
            } else if (Project.PROPERTY_PHASE.equals(propertyId)) {
                final PhaseSelect cb = (PhaseSelect) field;
                cb.setFilteringMode(Filtering.FILTERINGMODE_STARTSWITH);
                cb.setNewItemsAllowed(true);
                cb.setNullSelectionAllowed(false);
                cb.select(project.getPhase());
            } else if (Project.PROPERTY_PROJECTID.equals(propertyId)) {
                TextField tf = (TextField) field;
                tf.setCaption(tf.getCaption() + "*");
            } else if (Project.PROPERTY_NAME.equals(propertyId)) {
                TextField tf = (TextField) field;
                tf.setCaption(tf.getCaption() + "*");
            } else if (Project.PROPERTY_DESCRIPTION.equals(propertyId)) {
                TextField tf = (TextField) field;
                tf.setRows(3);
            } else if (Project.PROPERTY_DESCRIPTION_FORMAT.equals(propertyId)){
                final ComboBox cb = (ComboBox) field;
                cb.setFilteringMode(Filtering.FILTERINGMODE_STARTSWITH);
                cb.setNewItemsAllowed(false);
                cb.setNullSelectionAllowed(false);
                cb.select(project.getDescriptionFormat());
            }
        }
    }

    private static class ProjectParentDataSource extends IndexedContainer {
        private static final long serialVersionUID = -4944225199628809161L;

        @PropertyName(position = 0)
        public static final Object PROPERTY_DISPLAYNAME = "displayName";

        public ProjectParentDataSource(Project thisProject) {
            super();
            addContainerProperty(Project.PROPERTY_NAME, String.class, null);
            addContainerProperty(Project.PROPERTY_PROJECTID, String.class, null);
            addContainerProperty(PROPERTY_DISPLAYNAME, String.class, null);

            ProjectTemplateService templateService = Services.getRequiredService(ProjectTemplateService.class);
            ProjectTemplate thisProjectTemplate = templateService.getProjectTemplateById(thisProject
                    .getProjectTemplateId());
            if (thisProjectTemplate == null) {
                throw new NoSuchTemplateException(thisProject.getUuid(), thisProject.getProjectTemplateId());
            }

            ProjectService projectService = Services.getRequiredService(ProjectService.class);
            List<Project> projects = projectService.getAll();
            for (Project project : projects) {
                // skip all projects that thisProject cannot be assigned to
                // because projectTemplate of the target project does not allow thisProjectTemplate,
                // and skip all projects that are already in the parent chain of thisProject
                // to avoid cycles in the parent hierarchy
                ProjectTemplate projectTemplate = templateService
                        .getProjectTemplateById(project.getProjectTemplateId());
                if (projectTemplate == null) {
                    throw new NoSuchTemplateException(project.getUuid(), project.getProjectTemplateId());
                }
                List<Project> parents = projectService.getParentChain(project.getUuid());
                if (projectTemplate.isAllowedSubprojectTemplate(thisProjectTemplate) && !parents.contains(thisProject)) {
                    addItem(project);
                }
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

    private static class ProjectTemplateDataSource extends IndexedContainer {
        private static final long serialVersionUID = -5676330321132278052L;

        @PropertyName(position = 0)
        public static final Object PROPERTY_DISPLAYNAME = "displayName"; //$NON-NLS-1$
        @PropertyName(position = 1)
        public static final Object PROPERTY_ISDEFAULT = "isDefault"; //$NON-NLS-1$

        public ProjectTemplateDataSource(Project thisProject) {
            super();
            addContainerProperty(PROPERTY_ISDEFAULT, Boolean.class, null);
            addContainerProperty(PROPERTY_DISPLAYNAME, String.class, null);

            // retrieve the template of the parent project (if any)
            ProjectTemplate parentProjectTemplate = null;
            EntityBase parentEntity = thisProject.getParentEntity();
            if (parentEntity instanceof Project) {
                Project parentProject = (Project) parentEntity;
                ProjectTemplateService templateService = Services.getRequiredService(ProjectTemplateService.class);
                parentProjectTemplate = templateService.getProjectTemplateById(parentProject.getProjectTemplateId());
                if (parentProjectTemplate == null) {
                    throw new NoSuchTemplateException(parentProject.getUuid(), parentProject.getProjectTemplateId());
                }
            }

            Iterator<ProjectTemplate> it = Services.getServiceIterator(ProjectTemplate.class);
            while (it.hasNext()) {
                // skip templates that are not compatible with the template of the parent project (if any)
                ProjectTemplate projectTemplate = it.next();
                if (parentProjectTemplate == null || parentProjectTemplate.isAllowedSubprojectTemplate(projectTemplate)) {
                    addItem(projectTemplate);
                }
            }

            // ensure that "default" is always the first entry in the list; rest ordered by display name
            sort(new Object[] { PROPERTY_ISDEFAULT, PROPERTY_DISPLAYNAME }, new boolean[] { false, true });
        }

        private Item addItem(ProjectTemplate projectTemplate) {
            // item key = template identifier
            String templateId = projectTemplate.getId();
            Item item = getItem(templateId);
            if (item == null) {
                item = addItem(templateId); // IndexedContainer#addItem return null, if entry already exists!!!
            }
            if (item != null) {
                item.getItemProperty(PROPERTY_ISDEFAULT).setValue(
                        ProjectTemplate.DEFAULT_ID.equals(projectTemplate.getId()));
                item.getItemProperty(PROPERTY_DISPLAYNAME).setValue(projectTemplate.getDisplayName());
            }
            return item;
        }
    }

    @Override
    public Class<Project> getExtensionClass() {
        return Project.class;
    }

    @Override
    public Project newExtensionInstance() {
        return new Project();
    }

}
