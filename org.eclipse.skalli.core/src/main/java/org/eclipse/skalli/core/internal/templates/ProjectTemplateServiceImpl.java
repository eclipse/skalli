/*******************************************************************************
<o * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.internal.templates;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.ServiceFilter;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.template.ProjectTemplate;
import org.eclipse.skalli.services.template.ProjectTemplateService;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectTemplateServiceImpl implements ProjectTemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectTemplateServiceImpl.class);

    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[ProjectTemplateService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[ProjectTemplateService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    public Set<ProjectTemplate> getAllTemplates() {
        return Services.getServices(ProjectTemplate.class, new TemplateComparator());
    }

    @Override
    public ProjectTemplate getProjectTemplate(String className) {
        Iterator<ProjectTemplate> templateServices = Services.getServiceIterator(ProjectTemplate.class);
        while (templateServices.hasNext()) {
            ProjectTemplate projectTemplate = templateServices.next();
            if (projectTemplate.getClass().getName().equals(className)) {
                return projectTemplate;
            }
        }
        return new DefaultProjectTemplate();
    }

    @Override
    public ProjectTemplate getProjectTemplateById(String id) {
        Iterator<ProjectTemplate> templateServices = Services.getServiceIterator(ProjectTemplate.class);
        while (templateServices.hasNext()) {
            ProjectTemplate projectTemplate = templateServices.next();
            if (projectTemplate.getId().equals(id)) {
                return projectTemplate;
            }
        }
        return new DefaultProjectTemplate();
    }

    private static class TemplateComparator implements Comparator<ProjectTemplate> {

        @Override
        public int compare(ProjectTemplate o1, ProjectTemplate o2) {
            int result = compareBoolean("default".equals(o1.getId()), "default".equals(o2.getId()));
            if (result == 0) {
                result = o1.getDisplayName().compareTo(o2.getDisplayName());
                if (result == 0) {
                    result = o1.getId().compareTo(o2.getId());
                }
            }
            return result;
        }

        private int compareBoolean(boolean b1, boolean b2) {
            if (b1) {
                return b2 ? 0 : -1;
            } else {
                return b2 ? 1 : 0;
            }
        }

    }

    @Override
    @SuppressWarnings("rawtypes")
    public Set<Class<? extends ExtensionEntityBase>> getSelectableExtensions(final ProjectTemplate template,
            final Project project) {
        final Set<Class<? extends ExtensionEntityBase>> selectableExtensions = new HashSet<Class<? extends ExtensionEntityBase>>();

        if (project != null) {
            for (ExtensionEntityBase extension : project.getAllExtensions()) {
                selectableExtensions.add(extension.getClass());
            }
        }

        final Set<String> included = template.getIncludedExtensions();
        final Set<String> excluded = template.getExcludedExtensions();
        Services.getServices(ExtensionService.class,
                new ServiceFilter<ExtensionService>() {
                    @Override
                    public boolean accept(ExtensionService instance) {
                        // 1) ask the extension, whether it can work with the given template
                        // 2) if so, check if we have an exclude list and the extension is excluded
                        // 3) if so, reject the extensions, otherwise check if we have an include
                        //    list and the extension is included
                        // 4) if so, accept it, otherwise reject it
                        Set<String> allowedTemplates = ((ExtensionService<?>) instance).getProjectTemplateIds();
                        if (allowedTemplates == null || allowedTemplates.contains(template.getId())) {
                            Class<? extends ExtensionEntityBase> extensionClass = instance.getExtensionClass();
                            String extensionClassName = extensionClass.getName();

                            if (excluded != null && excluded.contains(extensionClassName)
                                    || included != null && !included.contains(extensionClassName)) {
                                return false;
                            }

                            selectableExtensions.add(extensionClass);
                            return true;
                        }
                        return false;
                    }
                });

        return selectableExtensions;
    }
}
