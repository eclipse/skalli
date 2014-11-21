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
package org.eclipse.skalli.view.internal.window;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.permit.Permit;
import org.eclipse.skalli.services.permit.Permits;
import org.eclipse.skalli.view.ext.ExtensionFormService;
import org.eclipse.skalli.view.ext.ExtensionStreamSource;
import org.eclipse.skalli.view.ext.ProjectEditContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;

/**
 * Entry of a {@link ProjectEditPanel}.
 * Note: this class has a natural ordering that might not be consistent with equals.
 * Two entries are equal if and only if their internal <code>ProjectEditForm</code>
 * are identical (in the sense of == not equals!). However, the natural ordering
 * of entries is based on their rank.
 */
class ProjectEditPanelEntry extends CustomComponent {

    private static final long serialVersionUID = 6723243553337561399L;

    private static final String BUTTON_TEXT_DISABLE = "disable";
    private static final String BUTTON_TEXT_DISABLED = "disabled";
    private static final String BUTTON_TEXT_EDIT = "edit";
    private static final String BUTTON_TEXT_EDITABLE = "editable";
    private static final String BUTTON_TEXT_INHERIT = "inherit";
    private static final String BUTTON_TEXT_INHERITED = "inherited";
    private static final String BUTTON_TEXT_SHOW = "show";
    private static final String BUTTON_TEXT_HIDE = "hide";

    private static final String STYLE_TRAY = "tray"; //$NON-NLS-1$
    private static final String STYLE_TRAY_HEADER = "header"; //$NON-NLS-1$
    private static final String STYLE_TRAY_HEADER_ICON = "header-icon"; //$NON-NLS-1$
    private static final String STYLE_TRAY_HEADER_LABEL = "header-label"; //$NON-NLS-1$
    private static final String STYLE_TRAY_DESCRIPTION = "description"; //$NON-NLS-1$
    private static final String STYLE_TRAY_OPEN = "open"; //$NON-NLS-1$
    private static final String STYLE_TRAY_CLOSED = "closed"; //$NON-NLS-1$
    private static final String STYLE_BUTTON_SELECTED = "selected"; //$NON-NLS-1$
    private static final String STYLE_ISSUES = "prjedt-issues"; //$NON-NLS-1$

    private static final String EXTENSIONS = "extensions"; //$NON-NLS-1$

    private static final Logger LOG = LoggerFactory.getLogger(ProjectEditPanelEntry.class);

    private Project project;
    private Class<? extends ExtensionEntityBase> extensionClass;
    private String extensionClassName;
    private ExtensionEntityBase extension;
    private ExtensionService<? extends ExtensionEntityBase> extensionService;
    private ProjectEditContext context;
    private Application application;

    private Set<String> readOnlyFieldsIds = new HashSet<String>();
    private Set<String> disabledFieldsIds = new HashSet<String>();

    // service to create a Vaadin form for the extension
    // and retrieve caption/description/icon for the tray
    private ExtensionFormService<?> formService;

    // the actual form instance, content of tray */
    private Form form;

    // the outer tray container
    private ComponentContainer tray;
    private Embedded trayIcon;

    // buttons of the tray
    private Button visibleButton;
    private Button inheritButton;
    private Button editButton;
    private Button disableButton;

    // the current state of the tray
    private TrayState state;
    private boolean disableAllowed;
    private boolean inheritAllowed;

    // issue rendering
    private Label issueLabel;

    private enum TrayState {
        DISABLED, EDITABLE_INVISIBLE, EDITABLE_VISIBLE, INHERITED_INVISIBLE, INHERITED_VISIBLE
    }

    public ProjectEditPanelEntry(Project project, ExtensionFormService<?> formService, ProjectEditContext context,
            Application application) {
        this.project = project;
        this.formService = formService;
        this.context = context;
        this.application = application;

        extensionClass = formService.getExtensionClass();
        extensionClassName = extensionClass.getName();
        if (!project.isInherited(extensionClass)) {
            extension = project.getExtension(extensionClass);
        }
        extensionService = ExtensionServices.getByExtensionClass(extensionClass);
        if (extensionService == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "ExtensionFormService{0} has no corresponding ExtensionService", formService.getClass()));
        }
        boolean isEnabledIntially = context.getProjectTemplate().isEnabled(extensionClassName);

        if (extensionClass.equals(project.getClass())) {
            disableAllowed = false;
            inheritAllowed = false;
        }
        else {
            disableAllowed = true;
            inheritAllowed = true;
        }

        tray = createTray();
        layoutNewEditForm();

        if (extensionClass.equals(project.getClass())) {
            setState(TrayState.EDITABLE_VISIBLE);
        }
        else if (isEnabledIntially && project.isInherited(extensionClass)) {
            setState(TrayState.INHERITED_VISIBLE);
        }
        else if (project.isInherited(extensionClass)) {
            setState(TrayState.INHERITED_INVISIBLE);
        }
        else if (isEnabledIntially && extension != null) {
            setState(TrayState.EDITABLE_VISIBLE);
        }
        else if (extension != null) {
            setState(TrayState.EDITABLE_VISIBLE);
        }
        else {
            setState(TrayState.DISABLED);
        }

        String extensionShortName = extensionService.getShortName();
        if (!Permits.isAllowed(Permit.ACTION_DELETE, project, EXTENSIONS, extensionShortName)) {
            disableButton.setEnabled(false);
        }
        if (!Permits.isAllowed(Permit.ACTION_POST, project, EXTENSIONS, extensionShortName)) {
            editButton.setEnabled(false);
        }
        if (!Permits.isAllowed(Permit.ACTION_PUT, project, EXTENSIONS, extensionShortName,
                "properties", "inherited")) { //$NON-NLS-1$ //$NON-NLS-2$
            inheritButton.setEnabled(false);
        }
        setCompositionRoot(tray);
    }

    public boolean isValid() {
        return form != null ? form.isValid() : true;
    }

    public void commit() {
        if (form != null) {
            form.commit();
        }
    }

    public void expand() {
        switch (state) {
        case EDITABLE_INVISIBLE:
            setState(TrayState.EDITABLE_VISIBLE);
            break;
        case INHERITED_INVISIBLE:
            setState(TrayState.INHERITED_VISIBLE);
            break;
        }
    }

    public void collapse() {
        switch (state) {
        case EDITABLE_VISIBLE:
            setState(TrayState.EDITABLE_INVISIBLE);
            break;
        case INHERITED_VISIBLE:
            setState(TrayState.INHERITED_INVISIBLE);
            break;
        }
    }

    public boolean isDisabled() {
        return TrayState.DISABLED.equals(state);
    }

    public boolean isExpanded() {
        return TrayState.EDITABLE_VISIBLE.equals(state) || TrayState.INHERITED_VISIBLE.equals(state);
    }


    public boolean isEditable() {
        return TrayState.EDITABLE_VISIBLE.equals(state) || TrayState.EDITABLE_INVISIBLE.equals(state);
    }

    public boolean isInherited() {
        return TrayState.INHERITED_VISIBLE.equals(state) || TrayState.INHERITED_INVISIBLE.equals(state);
    }

    public String getDisplayName() {
        return extensionService.getCaption();
    }

    public float getRank() {
        return formService.getRank();
    }

    public Class<? extends ExtensionEntityBase> getExtensionClass() {
        return extensionClass;
    }

    public String getExtensionClassName() {
        return extensionClassName;
    }

    public void onPropertyChanged(String extensionClassName, String propertyId, Object newValue) {
        if (EntityBase.class.getName().equals(extensionClassName) && EntityBase.PROPERTY_PARENT_ENTITY.equals(propertyId)) {
            project.setParentEntity((EntityBase) newValue);
            if (TrayState.INHERITED_INVISIBLE.equals(state) || TrayState.INHERITED_VISIBLE.equals(state)) {
                inheritExtension();
                setState(state);
            }
        }
        if (formService.listenOnPropertyChanged(extensionClassName, propertyId, newValue)) {
            form.commit();
            layoutNewEditForm();
        }
    }

    public Object getProperty(String propertyName) {
        if (!isDisabled() && extension != null && extension.hasProperty(propertyName)) {
            form.commit();
            return extension.getProperty(propertyName);
        }
        return null;
    }

    public void setProperty(String propertyName, Object propertyValue) {
        if (isInherited()) {
            return;
        }
        if (isDisabled()) {
            setState(TrayState.EDITABLE_VISIBLE);
        }
        if (extension != null && extension.hasProperty(propertyName)) {
            extension.setProperty(propertyName, propertyValue);
            layoutNewEditForm();
            expand();
        }
    }

    public void showIssues(SortedSet<Issue> issues, boolean collapseValid) {
        if (issueLabel == null) {
            issueLabel = new Label(StringUtils.EMPTY, Label.CONTENT_XHTML);
            issueLabel.setWidth(100, UNITS_PERCENTAGE);
            issueLabel.addStyleName(STYLE_ISSUES);
            tray.addComponent(issueLabel);
        }

        if (issues == null || issues.isEmpty()) {
            issueLabel.setValue(StringUtils.EMPTY);
            issueLabel.setVisible(false);
            if (collapseValid) {
                collapse();
            }
        } else {
            issueLabel.setValue(Issue.asHTMLList(null, issues));
            issueLabel.setVisible(true);
            expand();
        }
    }

    @SuppressWarnings("serial")
    private ComponentContainer createTray() {
        CssLayout layout = new CssLayout();
        layout.setStyleName(STYLE_TRAY);
        layout.setMargin(true);

        HorizontalLayout header = new HorizontalLayout();
        header.setSpacing(true);
        header.setStyleName(STYLE_TRAY_HEADER);
        header.setWidth("100%"); //$NON-NLS-1$

        trayIcon = new Embedded();
        trayIcon.setStyleName(STYLE_TRAY_HEADER_ICON);

        final String icon = formService.getIconPath();
        if (StringUtils.isNotBlank(icon)) {
            trayIcon.setSource(new StreamResource(new ExtensionStreamSource(formService.getClass(), icon),
                    FilenameUtils.getName(icon), application));
        }
        header.addComponent(trayIcon);
        header.setComponentAlignment(trayIcon, Alignment.MIDDLE_LEFT);
        header.setExpandRatio(trayIcon, 0);

        Label captionLabel = new Label(getCaptionWithAnchor(), Label.CONTENT_XHTML);
        captionLabel.setStyleName(STYLE_TRAY_HEADER_LABEL);
        header.addComponent(captionLabel);
        header.setExpandRatio(captionLabel, 1);
        header.setComponentAlignment(captionLabel, Alignment.MIDDLE_LEFT);

        editButton = new Button();
        layoutLinkButton(editButton, header, new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                switch (state) {
                case DISABLED:
                    setState(TrayState.EDITABLE_VISIBLE);
                    break;
                case INHERITED_VISIBLE:
                    setState(TrayState.EDITABLE_VISIBLE);
                    break;
                case INHERITED_INVISIBLE:
                    setState(TrayState.EDITABLE_VISIBLE);
                    break;
                }
            }
        });

        inheritButton = new Button();
        layoutLinkButton(inheritButton, header, new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                form.commit();
                switch (state) {
                case DISABLED:
                    setState(TrayState.INHERITED_VISIBLE);
                    break;
                case EDITABLE_VISIBLE:
                    setState(TrayState.INHERITED_VISIBLE);
                    break;
                case EDITABLE_INVISIBLE:
                    setState(TrayState.INHERITED_VISIBLE);
                    break;
                }
            }
        });

        disableButton = new Button();
        layoutLinkButton(disableButton, header, new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                form.commit();
                switch (state) {
                case EDITABLE_INVISIBLE:
                    setState(TrayState.DISABLED);
                    break;
                case EDITABLE_VISIBLE:
                    setState(TrayState.DISABLED);
                    break;
                case INHERITED_INVISIBLE:
                    setState(TrayState.DISABLED);
                    break;
                case INHERITED_VISIBLE:
                    setState(TrayState.DISABLED);
                    break;
                }
            }
        });

        visibleButton = new NativeButton();
        layoutIconButton(visibleButton, header, new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                switch (state) {
                case EDITABLE_INVISIBLE:
                    setState(TrayState.EDITABLE_VISIBLE);
                    break;
                case EDITABLE_VISIBLE:
                    setState(TrayState.EDITABLE_INVISIBLE);
                    break;
                case INHERITED_INVISIBLE:
                    setState(TrayState.INHERITED_VISIBLE);
                    break;
                case INHERITED_VISIBLE:
                    setState(TrayState.INHERITED_INVISIBLE);
                    break;
                }
            }
        });

        layout.addComponent(header);

        CssLayout content = new CssLayout();
        Label descriptionLabel = new Label(extensionService.getDescription(), Label.CONTENT_XHTML);
        descriptionLabel.setStyleName(STYLE_TRAY_DESCRIPTION);
        content.addComponent(descriptionLabel);

        layout.addComponent(content);
        return layout;
    }

    @SuppressWarnings("nls")
    private String getCaptionWithAnchor() {
        StringBuilder sb = new StringBuilder();
        sb.append("<a id=\"");
        sb.append(extensionClassName);
        sb.append("\" name=\"");
        sb.append(extensionClassName);
        sb.append("\"><!-- --></a>");
        sb.append(StringEscapeUtils.escapeHtml(extensionService.getCaption()));
        return sb.toString();
    }

    private void setState(TrayState newState) {
        switch (newState) {
        case DISABLED:
            if (state != null) {
                disableExtension();
            }
            editButton.setEnabled(true);
            editButton.setCaption(BUTTON_TEXT_EDIT);
            layoutInheritButton(BUTTON_TEXT_INHERIT, true);
            layoutDisableButton(BUTTON_TEXT_DISABLED, false);
            visibleButton.setEnabled(false);
            visibleButton.setDescription(BUTTON_TEXT_SHOW);
            visibleButton.setStyleName(STYLE_TRAY_CLOSED);
            form.setVisible(false);
            setFormEnabled(false);
            setFormReadOnly(true);
            editButton.removeStyleName(STYLE_BUTTON_SELECTED);
            inheritButton.removeStyleName(STYLE_BUTTON_SELECTED);
            disableButton.addStyleName(STYLE_BUTTON_SELECTED);
            break;

        case INHERITED_VISIBLE:
            if (TrayState.DISABLED.equals(state) ||
                    TrayState.EDITABLE_INVISIBLE.equals(state) ||
                    TrayState.EDITABLE_VISIBLE.equals(state)) {
                inheritExtension();
            }
            editButton.setEnabled(true);
            editButton.setCaption(BUTTON_TEXT_EDIT);
            layoutInheritButton(BUTTON_TEXT_INHERITED, false);
            layoutDisableButton(BUTTON_TEXT_DISABLE, true);
            visibleButton.setEnabled(true);
            visibleButton.setDescription(BUTTON_TEXT_HIDE);
            visibleButton.setStyleName(STYLE_TRAY_OPEN);
            form.setVisible(true);
            setFormEnabled(true);
            setFormReadOnly(true);
            editButton.removeStyleName(STYLE_BUTTON_SELECTED);
            inheritButton.addStyleName(STYLE_BUTTON_SELECTED);
            disableButton.removeStyleName(STYLE_BUTTON_SELECTED);
            break;

        case INHERITED_INVISIBLE:
            if (TrayState.DISABLED.equals(state) ||
                    TrayState.EDITABLE_INVISIBLE.equals(state) ||
                    TrayState.EDITABLE_VISIBLE.equals(state)) {
                inheritExtension();
            }
            editButton.setEnabled(true);
            editButton.setCaption(BUTTON_TEXT_EDIT);
            layoutInheritButton(BUTTON_TEXT_INHERITED, false);
            layoutDisableButton(BUTTON_TEXT_DISABLE, true);
            visibleButton.setEnabled(true);
            visibleButton.setDescription(BUTTON_TEXT_SHOW);
            visibleButton.setStyleName(STYLE_TRAY_CLOSED);
            form.setVisible(false);
            setFormEnabled(false);
            setFormReadOnly(false);
            editButton.removeStyleName(STYLE_BUTTON_SELECTED);
            inheritButton.addStyleName(STYLE_BUTTON_SELECTED);
            disableButton.removeStyleName(STYLE_BUTTON_SELECTED);
            break;

        case EDITABLE_VISIBLE:
            if (TrayState.DISABLED.equals(state) ||
                    TrayState.INHERITED_INVISIBLE.equals(state) ||
                    TrayState.INHERITED_VISIBLE.equals(state)) {
                editExtension();
            }
            editButton.setEnabled(false);
            editButton.setCaption(BUTTON_TEXT_EDITABLE);
            layoutInheritButton(BUTTON_TEXT_INHERIT, true);
            layoutDisableButton(BUTTON_TEXT_DISABLE, true);
            visibleButton.setEnabled(true);
            visibleButton.setDescription(BUTTON_TEXT_HIDE);
            visibleButton.setStyleName(STYLE_TRAY_OPEN);
            form.setVisible(true);
            setFormEnabled(true);
            setFormReadOnly(false);
            editButton.addStyleName(STYLE_BUTTON_SELECTED);
            inheritButton.removeStyleName(STYLE_BUTTON_SELECTED);
            disableButton.removeStyleName(STYLE_BUTTON_SELECTED);
            break;

        case EDITABLE_INVISIBLE:
            if (TrayState.DISABLED.equals(state) ||
                    TrayState.INHERITED_INVISIBLE.equals(state) ||
                    TrayState.INHERITED_VISIBLE.equals(state)) {
                editExtension();
            }
            editButton.setEnabled(false);
            editButton.setCaption(BUTTON_TEXT_EDITABLE);
            layoutInheritButton(BUTTON_TEXT_INHERIT, true);
            layoutDisableButton(BUTTON_TEXT_DISABLE, true);
            visibleButton.setEnabled(true);
            visibleButton.setDescription(BUTTON_TEXT_SHOW);
            visibleButton.setStyleName(STYLE_TRAY_CLOSED);
            form.setVisible(false);
            setFormEnabled(true);
            setFormReadOnly(false);
            editButton.addStyleName(STYLE_BUTTON_SELECTED);
            inheritButton.removeStyleName(STYLE_BUTTON_SELECTED);
            disableButton.removeStyleName(STYLE_BUTTON_SELECTED);
            break;
        }
        state = newState;
    }

    private void setFormReadOnly(boolean readonly) {
        form.setReadOnly(readonly);
        for (String readOnlyFieldId: readOnlyFieldsIds) {
            form.getField(readOnlyFieldId).setReadOnly(true);
        }
    }

    private void setFormEnabled(boolean enabled) {
        form.setEnabled(enabled);
        for (String disabledFieldsId: disabledFieldsIds) {
            form.getField(disabledFieldsId).setEnabled(false);
        }
    }

    private void layoutNewEditForm() {
        Form newForm;
        try {
            newForm = formService.createForm(project, context);
        } catch (RuntimeException e) {
            LOG.error("Failed to render '" + StringEscapeUtils.escapeHtml(getDisplayName()) + "' form", e);
            newForm = new Form();
            tray.addComponent(new Label("<span style=\"font-weight:bold;color:red\">Sorry, can't display this form. " +
                    "An internal error occurred. Please notify the administrator.</span>", Label.CONTENT_XHTML));
        }
        if (form != null) {
            tray.replaceComponent(form, newForm);
        } else {
            tray.addComponent(newForm);
        }
        form = newForm;
        initFieldStates();
    }

    private void initFieldStates() {
        readOnlyFieldsIds.clear();
        disabledFieldsIds.clear();
        for (Object propertyId: form.getItemPropertyIds()) {
            Field field = form.getField(propertyId);
            if (field.isReadOnly()) {
                readOnlyFieldsIds.add((String)propertyId);
            }
            if (!field.isEnabled()) {
                disabledFieldsIds.add((String)propertyId);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void layoutLinkButton(Button button, HorizontalLayout layout, ClickListener listener) {
        button.setStyle(Button.STYLE_LINK);
        button.addListener(listener);
        layout.addComponent(button);
        layout.setExpandRatio(button, 0);
        layout.setComponentAlignment(button, Alignment.MIDDLE_RIGHT);
    }

    private void layoutIconButton(Button button, HorizontalLayout layout, ClickListener listener) {
        button.addListener(listener);
        layout.addComponent(button);
        layout.setExpandRatio(button, 0);
        layout.setComponentAlignment(button, Alignment.MIDDLE_RIGHT);
    }

    private void layoutDisableButton(String text, boolean enabled) {
        if (disableAllowed) {
            disableButton.setEnabled(enabled);
            disableButton.setCaption(text);
        } else {
            disableButton.setEnabled(false);
            disableButton.setCaption(BUTTON_TEXT_DISABLE);
        }
    }

    private void layoutInheritButton(String text, boolean enabled) {
        if (inheritAllowed) {
            inheritButton.setEnabled(enabled);
            inheritButton.setCaption(text);
        } else {
            inheritButton.setEnabled(false);
            inheritButton.setCaption(BUTTON_TEXT_INHERIT);
        }
    }

    private void inheritExtension() {
        if (extension == null && !project.isInherited(extensionClass)) {
            extension = project.getExtension(extensionClass);
        }
        project.setInherited(extensionClass, true);
        layoutNewEditForm();
    }

    private void editExtension() {
        project.setInherited(extensionClass, false);
        if (extension == null) {
            extension = formService.newExtensionInstance();
        }
        project.addExtension(extension);
        layoutNewEditForm();
    }

    private void disableExtension() {
        if (extension == null && !project.isInherited(extensionClass)) {
            extension = project.getExtension(extensionClass);
        }
        project.removeExtension(extensionClass);
        tray.removeComponent(form);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null) {
            if (obj instanceof ProjectEditPanelEntry) {
                return formService == ((ProjectEditPanelEntry) obj).formService;
            }
            if (obj instanceof ExtensionFormService) {
                return formService == (ExtensionFormService<?>) obj;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return formService.hashCode();
    }

    @Override
    public String toString() {
        return extensionService.getCaption();
    }
}
