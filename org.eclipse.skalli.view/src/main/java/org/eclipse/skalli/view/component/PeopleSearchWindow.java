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

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.view.internal.container.UserContainer;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class PeopleSearchWindow extends Window {
    private static final long serialVersionUID = -2022741177097739042L;

    private final ThemeResource ICON_BUTTON_ADD_SELECTED = new ThemeResource("icons/button/ok.png"); //$NON-NLS-1$

    private static final String STYLE_USER_DOSEARCH = "pplsrch-dosearch"; //$NON-NLS-1$

    private final IPeopleSelectHandler selectHandler;
    private ListSelect list;
    private TextField searchField;
    private final IndexedContainer dataSource = new IndexedContainer();
    private VerticalLayout root;
    private final Component parent;

    public PeopleSearchWindow(Component parent, IPeopleSelectHandler selectHandler) {
        this.parent = parent;
        this.selectHandler = selectHandler;
        createContents();
    }

    private void createContents() {
        setModal(true);
        setCaption("Search people...");

        setWidth("310px"); //$NON-NLS-1$
        setHeight("400px"); //$NON-NLS-1$

        root = new VerticalLayout();
        root.setMargin(true);
        root.setSpacing(true);

        HorizontalLayout search = new HorizontalLayout();
        search.setSpacing(true);

        searchField = new TextField("Search for:");
        searchField.setWidth("20em"); //$NON-NLS-1$
        searchField.setImmediate(true);
        searchField.addListener(new ValueChangeListener() {
            @Override
            public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                search((String) searchField.getValue());
                list.focus();
            }
        });
        search.addComponent(searchField);
        search.setComponentAlignment(searchField, Alignment.BOTTOM_LEFT);
        search.setExpandRatio(searchField, 1.0f);

        Button searchButton = new NativeButton("", new Button.ClickListener() { //$NON-NLS-1$
                    @Override
                    public void buttonClick(ClickEvent event) {
                        search((String) searchField.getValue());
                    }
                });
        searchButton.setDescription("Search");
        searchButton.setStyleName(STYLE_USER_DOSEARCH);
        search.addComponent(searchButton);
        search.setComponentAlignment(searchButton, Alignment.BOTTOM_LEFT);
        search.setExpandRatio(searchButton, 0);

        root.addComponent(search);

        list = new ListSelect("Search results:", dataSource);
        list.setSizeFull();
        list.setMultiSelect(true);
        list.setImmediate(true);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button addButton = new Button("Add");
        addButton.setIcon(ICON_BUTTON_ADD_SELECTED);
        addButton.setDescription("Adds the selected person to the list.");
        addButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Set<User> values = (Set<User>) list.getValue();
                if (selectHandler != null && values != null) {
                    selectHandler.onPeopleSelected(values);
                    list.removeAllItems();
                    searchField.setValue(""); //$NON-NLS-1$
                    searchField.focus();
                }
            }
        });
        buttons.addComponent(addButton);

        Button addAndCloseButton = new Button("Add & Close");
        addAndCloseButton.setIcon(ICON_BUTTON_ADD_SELECTED);
        addAndCloseButton.setDescription("Adds the selected person to the list and closes the dialog.");
        addAndCloseButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Set<User> values = (Set<User>) list.getValue();
                if (selectHandler != null && values != null) {
                    selectHandler.onPeopleSelected(values);
                    close();
                }
            }
        });
        buttons.addComponent(addAndCloseButton);

        root.addComponent(list);
        root.addComponent(buttons);
        root.setSizeFull();
        root.setExpandRatio(list, 1);
        //    root.setStyleName(STYLE_LAYOUT);
        setContent(root);
        searchField.focus();
    }

    private void search(String searchText) {
        dataSource.removeAllItems();
        if (StringUtils.isNotEmpty(searchText)) {
            TreeSet<User> sorted = new TreeSet<User>(UserContainer.findUsers(searchText));
            for (User user : sorted) {
                dataSource.addItem(user);
            }
            if (sorted.size() == 1) {
                list.select(sorted.iterator().next());
            }
        }
    }

    public void show() {
        parent.getWindow().addWindow(this);
    }

    public static interface IPeopleSelectHandler {
        public void onPeopleSelected(Set<User> users);
    }

}
