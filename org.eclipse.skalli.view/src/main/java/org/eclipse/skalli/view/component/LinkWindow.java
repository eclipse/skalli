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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validatable;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class LinkWindow extends Window {

    private static final long serialVersionUID = 7764538915405149666L;
    private static final Logger LOG = LoggerFactory.getLogger(LinkWindow.class);

    private final ThemeResource ICON_BUTTON_OK = new ThemeResource("icons/button/ok.png"); //$NON-NLS-1$

    private final Component parent;
    private final Collection<String> knownGroups;
    private final ILinkAddedHandler linkAddedHandler;

    private final ILinkModifiedHandler linkModifiedHandler;
    private final LinkGroup oldGroup;
    private final Link link;

    private VerticalLayout root;

    /**
     * @param parent  the calling component
     * @param linkGroups  groups that are added to the combo box
     * @param linkAddedHandler callback implementation
     */
    public LinkWindow(Component parent, Collection<LinkGroup> linkGroups, ILinkAddedHandler linkAddedHandler) {
        this.parent = parent;
        this.knownGroups = new ArrayList<String>(linkGroups.size());
        this.linkAddedHandler = linkAddedHandler;
        this.linkModifiedHandler = null;
        this.oldGroup = null;
        this.link = null;

        for (LinkGroup linkGroup : linkGroups) {
            this.knownGroups.add(linkGroup.getCaption());
        }

        createContents("Add Link");
    }

    public LinkWindow(Component parent, Collection<LinkGroup> linkGroups, LinkGroup oldGroup, Link link,
            ILinkModifiedHandler linkModifiedHandler) {
        this.parent = parent;
        this.knownGroups = new ArrayList<String>(linkGroups.size());
        this.linkAddedHandler = null;
        this.linkModifiedHandler = linkModifiedHandler;
        this.oldGroup = oldGroup;
        this.link = link;

        for (LinkGroup linkGroup : linkGroups) {
            this.knownGroups.add(linkGroup.getCaption());
        }

        createContents("Edit Link");
    }

    /**
     * Render the window
     */
    @SuppressWarnings("serial")
    private void createContents(String title) {
        setModal(true);
        setCaption(title);

        setWidth("400px"); //$NON-NLS-1$
        setHeight("300px"); //$NON-NLS-1$

        root = new VerticalLayout();
        root.setMargin(true);
        root.setSpacing(true);

        final ComboBox cbLinkGroup = new ComboBox("Link Group");
        cbLinkGroup.setInputPrompt("Enter a new group name or select from the list");
        cbLinkGroup.setWidth("100%"); //$NON-NLS-1$
        for (String groupName : knownGroups) {
            cbLinkGroup.addItem(groupName);
        }
        if (oldGroup != null && knownGroups.contains(oldGroup.getCaption())) {
            cbLinkGroup.select(oldGroup.getCaption());
        }
        cbLinkGroup.setImmediate(true);
        cbLinkGroup.setNullSelectionAllowed(false);
        cbLinkGroup.setNewItemsAllowed(true);
        cbLinkGroup.setNewItemHandler(new NewItemHandler() {
            @Override
            public void addNewItem(String newGroupName) {
                cbLinkGroup.removeAllItems();
                for (String groupName : knownGroups) {
                    cbLinkGroup.addItem(groupName);
                }
                if (!cbLinkGroup.containsId(newGroupName)) {
                    cbLinkGroup.addItem(newGroupName);
                }
                cbLinkGroup.select(newGroupName);
            }
        });
        cbLinkGroup.setRequired(true);
        cbLinkGroup.addValidator(new StringValidator());
        root.addComponent(cbLinkGroup);

        final TextField tfLinkCaption = new TextField("Page Title");
        tfLinkCaption.setInputPrompt("Enter a descriptive name for the page");
        tfLinkCaption.setWidth("100%"); //$NON-NLS-1$
        tfLinkCaption.setImmediate(true);
        tfLinkCaption.setRequired(true);
        tfLinkCaption.addValidator(new StringValidator());
        if (link != null) {
            tfLinkCaption.setValue(link.getLabel());
        }
        root.addComponent(tfLinkCaption);

        final TextField tfLinkURL = new TextField("URL");
        tfLinkURL.setInputPrompt("e.g. http://www.your-site.domain/path");
        tfLinkURL.setWidth("100%"); //$NON-NLS-1$
        tfLinkURL.setImmediate(true);
        tfLinkURL.setRequired(true);
        tfLinkURL.addValidator(new StringValidator());
        tfLinkURL.addValidator(new URLValidator());
        if (link != null) {
            tfLinkURL.setValue(link.getUrl());
        }
        root.addComponent(tfLinkURL);

        final Button okAndCloseButton = new Button("OK & Close");
        okAndCloseButton.setIcon(ICON_BUTTON_OK);
        okAndCloseButton.setDescription("Performs the action and closes the dialog.");
        okAndCloseButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                validateInput(cbLinkGroup);
                validateInput(tfLinkURL);
                validateInput(tfLinkCaption);

                if (cbLinkGroup.isValid() && tfLinkURL.isValid() && tfLinkCaption.isValid()) {
                    String groupName = String.valueOf(cbLinkGroup.getValue());
                    String linkLabel = String.valueOf(tfLinkCaption.getValue());
                    String linkUrl = String.valueOf(tfLinkURL.getValue());

                    if (linkAddedHandler != null) {
                        Link link = new Link(linkUrl, linkLabel);
                        linkAddedHandler.onLinkAdded(groupName, link);
                        close();
                    }

                    if (linkModifiedHandler != null) {
                        boolean linkModified = !link.getLabel().equals(linkLabel) || !link.getUrl().equals(linkUrl);
                        link.setLabel(linkLabel);
                        link.setUrl(linkUrl);
                        linkModifiedHandler.onLinkModified(oldGroup, groupName, link, linkModified);
                        close();
                    }
                }
            }
        });
        root.addComponent(okAndCloseButton);

        root.setSizeFull();
        setContent(root);
    }

    /**
     * Display this window.
     *
     * Typically called from the outside when the window was instantiated.
     */
    public void show() {
        parent.getWindow().addWindow(this);
    }

    private void validateInput(Validatable validatable) {
        try {
            validatable.validate();
        } catch (EmptyValueException e) {
            final AbstractComponent component = (AbstractComponent) validatable;
            component.setComponentError(new UserError("This field is required."));
            ((Field) component).addListener(new ValueNotLongerEmptyListener());
        } catch (InvalidValueException e) {
            // Automatically handled by validator implementations (onValueChange)
            LOG.debug("Invalid value", e); //$NON-NLS-1$
        }
    }

    /**
     * Removes the (manually set) component error in case the property isn't blank anymore.
     */
    private final class ValueNotLongerEmptyListener implements ValueChangeListener {

        private static final long serialVersionUID = -1568052404052787122L;

        @Override
        public void valueChange(ValueChangeEvent event) {
            Property property = event.getProperty();
            if (property != null) {
                Object value = property.getValue();
                if (StringUtils.isNotBlank((String) value)) {
                    ((AbstractComponent) property).setComponentError(null);
                }
            }
        }
    }

    /**
     * Checks for 'not empty' and no trailing/leading white spaces
     */
    private final class StringValidator implements Validator {

        private static final long serialVersionUID = 383201426094234832L;

        @Override
        public void validate(Object value) throws InvalidValueException {
            String stringValue = (String) value;

            if (StringUtils.isBlank(stringValue)) {
                throw new InvalidValueException("This field must not be blank.");
            }
            if (StringUtils.trimToEmpty(stringValue).length() != stringValue.length()) {
                throw new InvalidValueException("This field must not contain trailing or leading spaces.");
            }
        }

        @Override
        public boolean isValid(Object value) {
            try {
                validate(value);
            } catch (InvalidValueException e) {
                return false;
            }
            return true;
        }
    }

    /**
     * Checks for a valid URL pattern
     */
    private final class URLValidator implements Validator {

        private static final long serialVersionUID = -7840893696280942983L;

        @Override
        public void validate(Object value) throws InvalidValueException {
            String stringValue = (String) value;

            try {
                new URL(stringValue);
            } catch (MalformedURLException e) {
                throw new InvalidValueException("The URL is malformed.");
            }
        }

        @Override
        public boolean isValid(Object value) {
            try {
                validate(value);
            } catch (InvalidValueException e) {
                return false;
            }
            return true;
        }
    }

    /**
     * Callback interface for components that open this window (case 'add').
     */
    public interface ILinkAddedHandler {
        public void onLinkAdded(String groupName, Link link);
    }

    /**
     * Callback interface for components that open this window (case 'edit').
     */
    public interface ILinkModifiedHandler {
        public void onLinkModified(LinkGroup oldGroup, String newGroupName, Link link, boolean linkModified);
    }

}
