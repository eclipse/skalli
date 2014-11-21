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
package org.eclipse.skalli.services.template;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ProjectNature;
import org.eclipse.skalli.model.ext.commons.InfoExtension;
import org.eclipse.skalli.model.ext.commons.PeopleExtension;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.extension.ExtensionValidator;
import org.eclipse.skalli.services.extension.PropertyValidator;

public abstract class ProjectTemplateBase implements ProjectTemplate {

    protected static final String PROJECT_CLASSNAME = Project.class.getName();
    protected static final String PEOPLE_EXTENSION_CLASSNAME = PeopleExtension.class.getName();
    protected static final String INFO_EXTENSION_CLASSNAME = InfoExtension.class.getName();

    private Map<String, Float> ranks = new HashMap<String, Float>();

    private Set<String> includedExtensions = new HashSet<String>();
    private Set<String> excludedExtensions = new HashSet<String>();
    private Set<String> enabledExtensions = new HashSet<String>();
    private Set<String> visibleExtensions = new HashSet<String>();

    private Set<String> includedSubprojectTemplates = new HashSet<String>();
    private Set<String> excludedSubprojectTemplates = new HashSet<String>();

    private Map<String, Set<Object>> readOnlyProperties = new HashMap<String, Set<Object>>();
    private Map<String, Set<Object>> forAdminsWritableProperties = new HashMap<String, Set<Object>>();

    private Map<String, Set<Object>> disabledProperties = new HashMap<String, Set<Object>>();
    private Map<String, Set<Object>> forAdminsEnabledProperties = new HashMap<String, Set<Object>>();

    private Map<String, Set<Object>> hiddenProperties = new HashMap<String, Set<Object>>();
    private Map<String, Set<Object>> forAdminsVisibleProperties = new HashMap<String, Set<Object>>();

    private Map<String, Set<Object>> newValuesAllowedProperties = new HashMap<String, Set<Object>>();

    private Map<String, Map<Object, Collection<?>>> allowedValues = new HashMap<String, Map<Object, Collection<?>>>();

    private Map<String, Map<Object, Object>> defaultValues = new HashMap<String, Map<Object, Object>>();
    private Map<String, Map<Object, Collection<?>>> defaultCollectionValues = new HashMap<String, Map<Object, Collection<?>>>();

    private Map<String, Map<Object, String>> captions = new HashMap<String, Map<Object, String>>();
    private Map<String, Map<Object, String>> descriptions = new HashMap<String, Map<Object, String>>();
    private Map<String, Map<Object, String>> inputPrompts = new HashMap<String, Map<Object, String>>();
    private Map<String, Map<Object, Integer>> maxSizes = new HashMap<String, Map<Object, Integer>>();

    protected ProjectTemplateBase() {
        setEnabled(PEOPLE_EXTENSION_CLASSNAME, true);
        setEnabled(INFO_EXTENSION_CLASSNAME, true);
        setNewItemsAllowed(PROJECT_CLASSNAME, Project.PROPERTY_PHASE, true);
        setAllowedValues(PROJECT_CLASSNAME, Project.PROPERTY_PHASE, CollectionUtils.asSet(
                    "Proposal", "Experimental", "Prototype", "Incubation", "Specification",
                    "Design", "Implementation", "Alpha", "Beta", "Public Beta", "Release Candidate",
                    "Validation", "Ramp Up", "Released", "Stable", "Mature", "Maintenance",
                    "Deprecated", "Abandoned", "Closed"));
    }

    @Override
    public Set<String> getIncludedExtensions() {
        if (includedExtensions.isEmpty()) {
            return null;
        }
        Set<String> result = new HashSet<String>();
        for (String extensionClassName: includedExtensions) {
            if (ExtensionServices.getByExtensionClassName(extensionClassName) != null) {
                result.add(extensionClassName);
            }
        }
        return result;
    }

    protected void addIncludedExtension(String extensionClassName) {
        includedExtensions.add(extensionClassName);
    }

    @Override
    public Set<String> getExcludedExtensions() {
        if (excludedExtensions.isEmpty()) {
            return null;
        }
        Set<String> result = new HashSet<String>();
        for (String extensionClassName: excludedExtensions) {
            if (ExtensionServices.getByExtensionClassName(extensionClassName) != null) {
                result.add(extensionClassName);
            }
        }
        return result;
    }

    protected void addExcludedExtension(String extensionClassName) {
        excludedExtensions.add(extensionClassName);
    }

    @Override
    public boolean isAllowedSubprojectTemplate(ProjectTemplate projectTemplate) {
        // projects are not allowed as subprojects of components!
        if (getProjectNature() == ProjectNature.COMPONENT
                && projectTemplate.getProjectNature() == ProjectNature.PROJECT) {
            return false;
        }
        String templateId = projectTemplate.getId();
        if (includedSubprojectTemplates.size() > 0 && !includedExtensions.contains(templateId)) {
            return false;
        }
        if (excludedSubprojectTemplates.size() > 0 && excludedSubprojectTemplates.contains(templateId)) {
            return false;
        }
        return true;
    }

    @Override
    public Set<UUID> getAllowedParents() {
        return Collections.emptySet();
    }

    @Override
    public UUID getDirectParent() {
        return null;
    }

    protected void addIncludedSubprojectTemplate(String templateId) {
        includedExtensions.add(templateId);
    }

    protected void addExcludedSubprojectTemplate(String templateId) {
        excludedExtensions.add(templateId);
    }

    @Override
    public boolean isEnabled(String extensionClassName) {
        return enabledExtensions.contains(extensionClassName);
    }

    protected void setEnabled(String extensionClassName, boolean enabled) {
        if (enabled) {
            enabledExtensions.add(extensionClassName);
        } else {
            enabledExtensions.remove(extensionClassName);
        }
    }

    @Override
    public boolean isVisible(String extensionClassName) {
        return visibleExtensions.contains(extensionClassName);
    }

    protected void setVisible(String extensionClassName, boolean visible) {
        if (visible) {
            visibleExtensions.add(extensionClassName);
        } else {
            visibleExtensions.remove(extensionClassName);
        }
    }

    @Override
    public Collection<?> getAllowedValues(String extensionClassName, Object propertyId) {
        return getValues(extensionClassName, propertyId, allowedValues);
    }

    protected void setAllowedValues(String extensionClassName, Object propertyId, Collection<?> values) {
        setValues(extensionClassName, propertyId, values, allowedValues);
    }

    @Override
    public Object getDefaultValue(String extensionClassName, Object propertyId) {
        return getValue(extensionClassName, propertyId, defaultValues);
    }

    protected void setDefaultValue(String extensionClassName, Object propertyId, Object value) {
        setValue(extensionClassName, propertyId, value, defaultValues);
    }

    @Override
    public Collection<?> getDefaultValues(String extensionClassName, Object propertyId) {
        return getValues(extensionClassName, propertyId, defaultCollectionValues);
    }

    protected void setDefaultValues(String extensionClassName, Object propertyId, Collection<?> values) {
        setValues(extensionClassName, propertyId, values, defaultCollectionValues);
    }

    @Override
    public String getCaption(String extensionClassName, Object propertyId) {
        return getValue(extensionClassName, propertyId, captions);
    }

    protected void setCaption(String extensionClassName, Object propertyId, String caption) {
        setValue(extensionClassName, propertyId, caption, captions);
    }

    @Override
    public String getDescription(String extensionClassName, Object propertyId) {
        return getValue(extensionClassName, propertyId, descriptions);
    }

    protected void setDescription(String extensionClassName, Object propertyId, String description) {
        setValue(extensionClassName, propertyId, description, descriptions);
    }

    @Override
    public String getInputPrompt(String extensionClassName, Object propertyId) {
        return getValue(extensionClassName, propertyId, inputPrompts);
    }

    protected void setInputPrompt(String extensionClassName, Object propertyId, String inputPrompt) {
        setValue(extensionClassName, propertyId, inputPrompt, inputPrompts);
    }

    @Override
    public boolean isDisabled(String extensionClassName, Object propertyId, boolean isAdmin) {
        return getBooleanValue(extensionClassName, propertyId, isAdmin, disabledProperties, forAdminsEnabledProperties);
    }

    protected void setDisabled(String extensionClassName, Object propertyId, boolean disabled) {
        setBooleanValue(extensionClassName, propertyId, disabled, disabledProperties);
    }

    protected void setEnabledForAdmins(String extensionClassName, Object propertyId, boolean enabled) {
        setBooleanValue(extensionClassName, propertyId, enabled, forAdminsEnabledProperties);
    }

    @Override
    public boolean isReadOnly(String extensionClassName, Object propertyId, boolean isAdmin) {
        return getBooleanValue(extensionClassName, propertyId, isAdmin, readOnlyProperties, forAdminsWritableProperties);
    }

    protected void setReadOnly(String extensionClassName, Object propertyId, boolean readOnly) {
        setBooleanValue(extensionClassName, propertyId, readOnly, readOnlyProperties);
    }

    protected void setWriteableForAdmins(String extensionClassName, Object propertyId, boolean writeable) {
        setBooleanValue(extensionClassName, propertyId, writeable, forAdminsWritableProperties);
    }

    @Override
    public boolean isHidden(String extensionClassName, Object propertyId, boolean isAdmin) {
        return getBooleanValue(extensionClassName, propertyId, isAdmin, hiddenProperties, forAdminsVisibleProperties);
    }

    protected void setHidden(String extensionClassName, Object propertyId, boolean hidden) {
        setBooleanValue(extensionClassName, propertyId, hidden, hiddenProperties);
    }

    protected void setVisibleForAdmins(String extensionClassName, Object propertyId, boolean visible) {
        setBooleanValue(extensionClassName, propertyId, visible, forAdminsVisibleProperties);
    }

    @Override
    public int getMaxSize(String extensionClassName, Object propertyId) {
        return getValue(extensionClassName, propertyId, maxSizes, Integer.MAX_VALUE);
    }

    protected void setMaxSize(String extensionClassName, Object propertyId, int maxSize) {
        setValue(extensionClassName, propertyId, maxSize, maxSizes);
    }

    @Override
    public boolean isNewItemsAllowed(String extensionClassName, Object propertyId) {
        return contains(newValuesAllowedProperties, extensionClassName, propertyId);
    }

    protected void setNewItemsAllowed(String extensionClassName, Object propertyId, boolean allowed) {
        setBooleanValue(extensionClassName, propertyId, allowed, newValuesAllowedProperties);
    }

    @Override
    public float getRank(String extensionClassName) {
        Float rank = ranks.get(extensionClassName);
        return rank != null ? rank : -1.0f;
    }

    protected void setRank(String extensionClassName, float rank) {
        ranks.put(extensionClassName, rank);
    }

    @Override
    public List<ExtensionValidator<?>> getExtensionValidators(String extensionClassName) {
        return Collections.emptyList();
    }

    @Override
    public List<PropertyValidator> getPropertyValidators(String extensionClassName, Object propertyId) {
        return Collections.emptyList();
    }

    private <T> T getValue(String extensionClassName, Object propertyId, Map<String, Map<Object, T>> values) {
        return getValue(extensionClassName, propertyId, values, null);
    }

    private <T> T getValue(String extensionClassName, Object propertyId, Map<String, Map<Object, T>> values, T defaultValue) {
        Map<Object, T> map = values.get(extensionClassName);
        if (map == null) {
            return defaultValue;
        }
        T value = map.get(propertyId);
        return value != null? value : defaultValue;
    }

    private boolean contains(Map<String, Set<Object>> values, String extensionClassName, Object propertyId) {
        Set<Object> set = values.get(extensionClassName);
        return set != null ? set.contains(propertyId) : false;
    }

    private boolean getBooleanValue(String extensionClassName, Object propertyId, boolean isAdmin,
            Map<String, Set<Object>> values, Map<String, Set<Object>> adminValues) {
        if (contains(adminValues, extensionClassName, propertyId)) {
            return isAdmin;
        }
        return contains(values, extensionClassName, propertyId);
    }

    private Collection<?> getValues(String extensionClassName, Object propertyId, Map<String, Map<Object, Collection<?>>> values) {
        Map<Object, Collection<?>> items = values.get(extensionClassName);
        if (items == null) {
            return Collections.EMPTY_LIST;
        }
        Collection<?> c = items.get(propertyId);
        return c != null ? c : Collections.EMPTY_LIST;
    }

    private void setBooleanValue(String extensionClassName, Object propertyId, boolean b, Map<String, Set<Object>> values) {
        Set<Object> set = values.get(extensionClassName);
        if (set == null) {
            set = new HashSet<Object>();
            values.put(extensionClassName, set);
        }
        set.remove(propertyId);
        if (b) {
            set.add(propertyId);
        }
    }

    private <T> void setValue(String extensionClassName, Object propertyId, T value, Map<String, Map<Object, T>> values) {
        Map<Object, T> map = values.get(extensionClassName);
        if (map == null) {
            map = new HashMap<Object, T>();
            values.put(extensionClassName, map);
        }
        map.remove(propertyId);
        if (value != null) {
            map.put(propertyId, value);
        }
    }

    private void setValues(String extensionClassName, Object propertyId, Collection<?> value, Map<String, Map<Object, Collection<?>>> values) {
        Map<Object,  Collection<?>> map = values.get(extensionClassName);
        if (map == null) {
            map = new HashMap<Object, Collection<?>>();
            values.put(extensionClassName, map);
        }
        map.remove(propertyId);
        if (value != null) {
            map.put(propertyId, value);
        }
    }
}
