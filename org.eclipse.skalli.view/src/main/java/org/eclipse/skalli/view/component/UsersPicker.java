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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.eclipse.skalli.model.Member;
import org.eclipse.skalli.model.User;
import org.eclipse.skalli.services.user.UserServices;
import org.eclipse.skalli.view.component.PeopleSearchWindow.IPeopleSelectHandler;
import org.eclipse.skalli.view.internal.container.UserContainer;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class UsersPicker extends CustomField implements IPeopleSelectHandler {

    private static final long serialVersionUID = -8373359795470666228L;

    private static final String STYLE_TABLE = "usrpk-tbl";
    private static final String STYLE_BUTTON = "multitext-btn"; // use the same style as used in multitext component
    public static final String COLUMN_USER = "user";
    public static final String COLUMN_REMOVE = "remove";

    private final VerticalLayout layout;
    private final UsersPickerContainer tableDateSource;
    private final Collection<Member> members;
    private boolean modified;
    private boolean readOnly;

    private Table table;

    public UsersPicker(String caption, Collection<Member> members) {
        modified = false;
        this.members = members;

        tableDateSource = UsersPickerContainer.newInstance(new ArrayList<User>());
        initializeTableDataSource();

        layout = new VerticalLayout();
        layout.setWidth("100%"); // do not change that! otherwise right border of table is hidden in IE!
        renderTable();
        setCaption(caption);
        setCompositionRoot(layout);
    }

    private void initializeTableDataSource() {
        if (members != null) {
            for (Member member : members) {
                User user = UserContainer.getUser(member);
                if (user != null) {
                    tableDateSource.addItem(user);
                }
            }
        }
    }

    @SuppressWarnings({ "serial", "deprecation" })
    private void renderTable() {
        table = new Table();
        table.addStyleName(STYLE_TABLE);
        table.setSelectable(true);
        table.setContainerDataSource(tableDateSource);

        table.addGeneratedColumn(COLUMN_USER, new Table.ColumnGenerator() {
            @Override
            public Component generateCell(Table source, Object itemId, Object columnId) {
                String userId = itemId.toString();
                User user = UserServices.getUser(userId);
                PeopleComponent peopleComponent = new PeopleComponent(user);
                return peopleComponent;
            }
        });

        if (!readOnly) {
            table.addGeneratedColumn(COLUMN_REMOVE, new Table.ColumnGenerator() {
                @Override
                public Component generateCell(Table source, final Object itemId, Object columnId) {
                    Button b = new Button("remove");
                    b.setStyleName(Button.STYLE_LINK);
                    b.addStyleName(STYLE_BUTTON);
                    b.setDescription("Remove this member");
                    b.addListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(ClickEvent event) {
                            removeItem(itemId);
                            adjustPageLength(table);
                        }
                    });
                    return b;
                }
            });
        }

        table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
        if (!readOnly) {
            table.setVisibleColumns(new String[] { COLUMN_USER, COLUMN_REMOVE });
            table.setColumnExpandRatio(COLUMN_USER, 0.5f);
            table.setColumnWidth(COLUMN_REMOVE, 50);
            table.setColumnHeaders(new String[] { "User", "Remove" });
        } else {
            table.setVisibleColumns(new String[] { COLUMN_USER });
            table.setColumnHeaders(new String[] { "User", });
        }

        table.setSelectable(false);
        table.setWidth("100%"); // do not change that! otherwise right border of table is hidden in IE!
        table.setReadOnly(readOnly);
        adjustPageLength(table);

        layout.addComponent(table);
        layout.setExpandRatio(table, 1.0f);

        if (!readOnly) {
            Button searchButton = new Button("Add user", new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    PeopleSearchWindow search = new PeopleSearchWindow(UsersPicker.this, UsersPicker.this);
                    search.show();
                    adjustPageLength(table);
                }
            });
            searchButton.setStyle(Button.STYLE_LINK);
            searchButton.setDescription("Add user");
            searchButton.setWidth("80px");
            layout.addComponent(searchButton);
        }
    }

    private void removeItem(Object itemId) {
        if (itemId != null) {
            tableDateSource.removeItem(itemId);
            modified = true;
        }
    }

    private void adjustPageLength(Table table) {
        if (tableDateSource.size() > 0) {
            table.setPageLength(0);
        } else {
            table.setPageLength(1);
        }
    }

    @Override
    public void onPeopleSelected(Set<User> users) {
        for (User user : users) {
            tableDateSource.addItem(user);
        }
        modified = true;
        adjustPageLength(table);
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        // add all existing
        for (Object itemId : tableDateSource.getItemIds()) {
            String userId = (String) itemId;
            Member member = null;
            for (Member m : members) {
                if (m.getUserID().equalsIgnoreCase(userId)) {
                    member = m;
                    break;
                }
            }
            if (member == null) {
                member = new Member((String) itemId);
            }
            members.add(member);
        }

        // remove all nonexisting
        Iterator<Member> iterator = members.iterator();
        while (iterator.hasNext()) {
            Member member = iterator.next();
            Item item = tableDateSource.getItem(member.getUserID().toLowerCase(Locale.ENGLISH));
            if (item == null) {
                iterator.remove();
            }
        }
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        layout.removeAllComponents();
        renderTable();
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public Class<?> getType() {
        return Set.class;
    }

}
