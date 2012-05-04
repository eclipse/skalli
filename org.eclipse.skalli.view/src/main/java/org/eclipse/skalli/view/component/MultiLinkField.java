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

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.model.ext.linkgroups.LinkGroup;
import org.eclipse.skalli.model.ext.linkgroups.OrderableGroup;
import org.eclipse.skalli.view.component.LinkWindow.ILinkAddedHandler;
import org.eclipse.skalli.view.component.LinkWindow.ILinkModifiedHandler;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

public class MultiLinkField extends CustomField implements ILinkAddedHandler, ILinkModifiedHandler {

    private static final long serialVersionUID = 8114438955923388539L;

    private static final String STYLE_LAYOUT = "multilink-layout"; //$NON-NLS-1$
    private static final String STYLE_LABEL_GROUP = "multilink-grouplabel"; //$NON-NLS-1$
    private static final String STYLE_LABEL_LINK = "multilink-linklabel"; //$NON-NLS-1$
    private static final String STYLE_BUTTON_GROUPACTION = "multilink-groupaction"; //$NON-NLS-1$
    private static final String STYLE_BUTTON_LINKACTION = "multilink-linkaction"; //$NON-NLS-1$
    private static final String STYLE_BUTTON_ADD = "multilink-addlink"; //$NON-NLS-1$

    private GridLayout layout;
    private String buttonCaption;

    private boolean modified;
    private boolean readOnly;

    private final OrderableGroup<LinkGroup> linkGroups;

    public MultiLinkField(String caption, String buttonCaption, Collection<LinkGroup> linkGroups) {
        setCaption(caption);
        this.buttonCaption = StringUtils.isNotBlank(buttonCaption)? buttonCaption : "Add Link";
        this.linkGroups = new OrderableGroup<LinkGroup>(linkGroups);
        layout = new GridLayout();
        // do not change the width! otherwise right border of table is hidden in IE!
        layout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        layout.setStyleName(STYLE_LAYOUT);
        render();
        setCompositionRoot(layout);
    }

    private void renderIfModified() {
        if (modified) {
            render();
        }
    }

    @SuppressWarnings({ "serial", "deprecation" })
    private void render() {
        layout.removeAllComponents();
        if (!readOnly) {
            layout.setColumns(2);
        }

        int groupsIdx = 0;
        int groupsSize = linkGroups.getItems().size();
        for (final LinkGroup linkGroup : linkGroups.getItems()) {
            Label linkGroupLabel = new Label(linkGroup.getCaption());
            linkGroupLabel.addStyleName(STYLE_LABEL_GROUP);

            layout.addComponent(linkGroupLabel);
            layout.setComponentAlignment(linkGroupLabel, Alignment.TOP_RIGHT);

            if (!readOnly) {
                Button btnUpGroup = null;
                Button btnDownGroup = null;
                Button btnRemoveGroup = null;
                // up
                if (groupsIdx > 0) {
                    btnUpGroup = new Button("up");
                    btnUpGroup.setStyleName(Button.STYLE_LINK);
                    btnUpGroup.addStyleName(STYLE_BUTTON_GROUPACTION);
                    btnUpGroup.setDescription(String.format("Move up group '%s'", linkGroup.getCaption()));
                    btnUpGroup.addListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(ClickEvent event) {
                            modified = linkGroups.moveUp(linkGroup);
                            renderIfModified();
                        }
                    });
                }
                // down
                if (groupsIdx < groupsSize - 1) {
                    btnDownGroup = new Button("down");
                    btnDownGroup.setStyleName(Button.STYLE_LINK);
                    btnDownGroup.addStyleName(STYLE_BUTTON_GROUPACTION);
                    btnDownGroup.setDescription(String.format("Move down group '%s'", linkGroup.getCaption()));
                    btnDownGroup.addListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(ClickEvent event) {
                            modified = linkGroups.moveDown(linkGroup);
                            renderIfModified();
                        }
                    });
                }
                // remove
                btnRemoveGroup = new Button("remove");
                btnRemoveGroup.setStyleName(Button.STYLE_LINK);
                btnRemoveGroup.addStyleName(STYLE_BUTTON_GROUPACTION);
                btnRemoveGroup.setDescription(String.format("Remove group '%s'", linkGroup.getCaption()));
                btnRemoveGroup.addListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        modified = linkGroups.remove(linkGroup);
                        renderIfModified();
                    }
                });

                HorizontalLayout toolbar = getToolbar(btnUpGroup, btnDownGroup, btnRemoveGroup);
                layout.addComponent(toolbar);
                layout.setComponentAlignment(toolbar, Alignment.TOP_RIGHT);
            }

            layout.newLine();

            Collection<Link> links = linkGroup.getItems();
            int linksIdx = 0;
            int linksSize = links.size();
            for (final Link link : links) {

                if (readOnly) {
                    // view
                    Label linkLabel = new Label(link.getLabel());
                    linkLabel.addStyleName(STYLE_LABEL_LINK);
                    linkLabel.setDescription(StringUtils.abbreviate(link.getUrl(), 50));
                    layout.addComponent(linkLabel);
                    layout.setComponentAlignment(linkLabel, Alignment.TOP_LEFT);
                }
                else {
                    // edit
                    Button btnEditLink = new Button(link.getLabel());
                    btnEditLink.setStyleName(Button.STYLE_LINK);
                    btnEditLink.addStyleName(STYLE_LABEL_LINK);
                    btnEditLink.setDescription(String.format("Edit link '%s' %s", link.getLabel(),
                            StringUtils.isBlank(link.getUrl()) ? "" : "[" + link.getUrl() + "]"));
                    btnEditLink.addListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(ClickEvent event) {
                            LinkWindow editLinkWindow = new LinkWindow(MultiLinkField.this, linkGroups.getItems(),
                                    linkGroup, link, MultiLinkField.this);
                            editLinkWindow.show();
                        }
                    });
                    layout.addComponent(btnEditLink);
                    layout.setComponentAlignment(btnEditLink, Alignment.TOP_LEFT);
                }

                if (!readOnly) {
                    Button btnUpLink = null;
                    Button btnDownLink = null;
                    Button btnRemoveLink = null;
                    // up
                    if (linksIdx > 0) {
                        btnUpLink = new Button("up");
                        btnUpLink.setStyleName(Button.STYLE_LINK);
                        btnUpLink.addStyleName(STYLE_BUTTON_LINKACTION);
                        btnUpLink.setDescription(String.format("Move up link '%s'", link.getLabel()));
                        btnUpLink.addListener(new Button.ClickListener() {
                            @Override
                            public void buttonClick(ClickEvent event) {
                                modified = linkGroup.moveUp(link);
                                renderIfModified();
                            }
                        });
                    }
                    // down
                    if (linksIdx < linksSize - 1) {
                        btnDownLink = new Button("down");
                        btnDownLink.setStyleName(Button.STYLE_LINK);
                        btnDownLink.addStyleName(STYLE_BUTTON_LINKACTION);
                        btnDownLink.setDescription(String.format("Move down link '%s'", link.getLabel()));
                        btnDownLink.addListener(new Button.ClickListener() {
                            @Override
                            public void buttonClick(ClickEvent event) {
                                modified = linkGroup.moveDown(link);
                                renderIfModified();
                            }
                        });
                    }
                    // remove
                    btnRemoveLink = new Button("remove");
                    btnRemoveLink.setStyleName(Button.STYLE_LINK);
                    btnRemoveLink.addStyleName(STYLE_BUTTON_LINKACTION);
                    btnRemoveLink.setDescription(String.format("Remove link '%s'", link.getLabel()));
                    btnRemoveLink.addListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(ClickEvent event) {
                            modified = linkGroup.remove(link);
                            if (linkGroup.getItems().isEmpty()) {
                                linkGroups.remove(linkGroup);
                            }
                            renderIfModified();
                        }
                    });

                    HorizontalLayout toolbar = getToolbar(btnUpLink, btnDownLink, btnRemoveLink);
                    layout.addComponent(toolbar);
                    layout.setComponentAlignment(toolbar, Alignment.TOP_RIGHT);
                }

                layout.newLine();

                linksIdx++;
            }

            groupsIdx++;
        }

        if (!readOnly) {
            Button btnAddLink = new Button(buttonCaption, new Button.ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    LinkWindow addLinkWindow = new LinkWindow(MultiLinkField.this, linkGroups.getItems(),
                            MultiLinkField.this);
                    addLinkWindow.show();
                }
            });
            btnAddLink.setStyleName(Button.STYLE_LINK);
            btnAddLink.addStyleName(STYLE_BUTTON_ADD);
            btnAddLink.setDescription("Add Link");
            layout.addComponent(btnAddLink);
        }
    }

    private HorizontalLayout getToolbar(Component... components) {
        HorizontalLayout lineLayout = new HorizontalLayout();
        for (Component component : components) {
            if (component != null) {
                lineLayout.addComponent(component);
            }
        }
        return lineLayout;
    }

    @Override
    public void onLinkAdded(String groupName, Link link) {
        LinkGroup linkGroup = getOrCreateLinkGroup(groupName);
        if (!linkGroup.hasItem(link)) {
            modified = linkGroup.add(link);
        }
        renderIfModified();
    }

    @Override
    public void onLinkModified(LinkGroup oldGroup, String newGroupName, Link link, boolean linkModified) {
        modified = linkModified;

        // move link if the group changed
        if (!oldGroup.getCaption().equals(newGroupName)) {
            LinkGroup newGroup = getOrCreateLinkGroup(newGroupName);
            if (!newGroup.hasItem(link)) {
                modified = newGroup.add(link) || modified;
            }

            modified = oldGroup.remove(link) || modified;
            if (oldGroup.getItems().isEmpty()) {
                modified = linkGroups.remove(oldGroup) || modified;
            }
        }

        renderIfModified();
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        layout.removeAllComponents();
        render();
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public Object getValue() {
        return linkGroups.getItems();
    }

    @Override
    public Class<?> getType() {
        return Collection.class;
    }

    private LinkGroup getOrCreateLinkGroup(String groupName) {
        for (LinkGroup linkGroup : linkGroups.getItems()) {
            if (linkGroup.getCaption().equals(groupName)) {
                return linkGroup;
            }
        }

        LinkGroup newLinkGroup = new LinkGroup();
        newLinkGroup.setCaption(groupName);
        linkGroups.add(newLinkGroup);

        return newLinkGroup;
    }

}
