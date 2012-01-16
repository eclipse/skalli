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
package org.eclipse.skalli.view.component;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Window.Notification;

public class Tray extends CustomComponent {
    private static final long serialVersionUID = -5069545260062750691L;

    private Embedded trayIcon;
    private ThemeResource icon;
    private boolean isVisible;
    private boolean isEnabled;
    private boolean isRequired;
    private final Component component;
    private Button activator;
    private Button opener;

    private final ThemeResource ERROR_ICON = new ThemeResource("icons/tray/error.png");

    private static final String STYLE_TRAY = "tray";
    private static final String STYLE_TRAY_HEADER = "header";
    private static final String STYLE_TRAY_HEADER_ICON = "header-icon";
    private static final String STYLE_TRAY_HEADER_LABEL = "header-label";
    private static final String STYLE_TRAY_DESCRIPTION = "description";
    private static final String STYLE_TRAY_OPEN = "open";
    private static final String STYLE_TRAY_CLOSED = "closed";
    private static final String STYLE_TRAY_ENABLED = "enabled";
    private static final String STYLE_TRAY_DISABLED = "disabled";

    public Tray(Component component,
            String caption, String description, ThemeResource icon,
            boolean isVisible, boolean isEnabled, boolean isRequired)
    {
        this.component = component;
        ComponentContainer content = createContent(caption, description, icon, isRequired);
        content.addComponent(component);
        setContentState(isVisible, isEnabled);
    }

    private ComponentContainer createContent(String caption, String description, ThemeResource icon, boolean isRequired) {
        CssLayout layout = new CssLayout();
        layout.setStyleName(STYLE_TRAY);
        layout.setMargin(true);

        HorizontalLayout header = new HorizontalLayout();
        header.setSpacing(true);
        header.setStyleName(STYLE_TRAY_HEADER);
        header.setWidth("100%");

        this.icon = icon;
        trayIcon = new Embedded(null, icon);
        trayIcon.setStyleName(STYLE_TRAY_HEADER_ICON);
        header.addComponent(trayIcon);
        header.setComponentAlignment(trayIcon, Alignment.MIDDLE_LEFT);
        header.setExpandRatio(trayIcon, 0);

        Label captionLabel = new Label(caption, Label.CONTENT_XHTML);
        captionLabel.setStyleName(STYLE_TRAY_HEADER_LABEL);
        header.addComponent(captionLabel);
        header.setExpandRatio(captionLabel, 1);
        header.setComponentAlignment(captionLabel, Alignment.MIDDLE_LEFT);

        this.isRequired = isRequired;
        if (!isRequired) {
            activator = new NativeButton("", new ToggleEnabledListener());
            header.addComponent(activator);
            header.setExpandRatio(activator, 0);
            header.setComponentAlignment(activator, Alignment.MIDDLE_RIGHT);
        }

        opener = new NativeButton("", new ToggleVisibleListener());
        header.addComponent(opener);
        header.setExpandRatio(opener, 0);
        header.setComponentAlignment(opener, Alignment.MIDDLE_RIGHT);

        layout.addComponent(header);

        CssLayout content = new CssLayout();
        Label descriptionLabel = new Label(description, Label.CONTENT_XHTML);
        descriptionLabel.setStyleName(STYLE_TRAY_DESCRIPTION);
        content.addComponent(descriptionLabel);

        layout.addComponent(content);
        setCompositionRoot(layout);
        return content;
    }

    public boolean isContentVisible() {
        return isVisible;
    }

    public void setContentVisible(boolean isContentVisible) {
        setContentState(isContentVisible, isEnabled);
        requestRepaint();
    }

    public boolean isContentEnabled() {
        return isEnabled;
    }

    public void setContentEnabled(boolean isContentEnabled) {
        setContentState(isContentEnabled, isContentEnabled);
        requestRepaint();
    }

    public void markAsInvalid(boolean isInvalid) {
        if (isInvalid) {
            trayIcon.setSource(ERROR_ICON);
        } else {
            trayIcon.setSource(icon);
        }
    }

    protected void setContentState(boolean isVisible, boolean isEnabled) {
        this.isVisible = isVisible;
        this.isEnabled = isRequired ? true : isEnabled;
        if (component != null) {
            component.setEnabled(isEnabled);
            component.setVisible(isVisible);
        }
        opener.setDescription(isVisible ? "Hide details" : "Show details");
        opener.setStyleName(isVisible ? STYLE_TRAY_OPEN : STYLE_TRAY_CLOSED);
        if (!isRequired) {
            activator.setDescription(isEnabled ? "Disable this project nature" : "Enable this project nature");
            activator.setStyleName(isEnabled ? STYLE_TRAY_ENABLED : STYLE_TRAY_DISABLED);
        }
    }

    private class ToggleVisibleListener implements Button.ClickListener {
        private static final long serialVersionUID = 6624509900591216536L;

        @Override
        public void buttonClick(ClickEvent event) {
            setContentVisible(!isVisible);
        }
    }

    private class ToggleEnabledListener implements Button.ClickListener {
        private static final long serialVersionUID = 539162056096468214L;

        @Override
        public void buttonClick(ClickEvent event) {
            boolean wasEnabled = isEnabled;
            setContentEnabled(!isEnabled);
            if (wasEnabled) {
                getWindow().showNotification(
                        "Potential Data Loss",
                        "Removing a project nature removes also the maintained data!",
                        Notification.TYPE_WARNING_MESSAGE);
            }
        }
    }
}
