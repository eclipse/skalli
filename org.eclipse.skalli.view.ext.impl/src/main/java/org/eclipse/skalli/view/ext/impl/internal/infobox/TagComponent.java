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
package org.eclipse.skalli.view.ext.impl.internal.infobox;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.model.ext.commons.TaggingUtils;
import org.eclipse.skalli.services.tagging.TaggingService;
import org.eclipse.skalli.view.ext.ExtensionUtil;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.ui.AbstractSelect.NewItemHandler;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

public class TagComponent extends CustomComponent {

    private static final long serialVersionUID = 2926468032572843890L;

    private static final String STYLE_TAG_COMPONENT = "tag-component"; //$NON-NLS-1$

    private final Project project;
    private final TaggingService taggingService;
    private final ExtensionUtil util;

    private final Layout layout;
    private final CssLayout tagListLayout;
    private final CssLayout tagComboBoxLayout;

    @SuppressWarnings("serial")
    public TagComponent(Project project, TaggingService taggingService, ExtensionUtil util) {
        this.project = project;
        this.taggingService = taggingService;
        this.util = util;

        tagListLayout = new CssLayout() {
            @Override
            protected String getCss(Component c) {
                if (c instanceof Label) {
                    return "float: left; line-height: 18px; padding-right: 3px;";
                } else {
                    return "float: left";
                }
            }
        };
        tagComboBoxLayout = new CssLayout();
        tagComboBoxLayout.setSizeFull();
        layout = new CssLayout();
        layout.setSizeFull();
        layout.addStyleName(STYLE_TAG_COMPONENT);
        paintTagView();
        setCompositionRoot(layout);
    }

    @SuppressWarnings({ "deprecation", "serial" })
    private void paintTagView() {
        layout.removeAllComponents();

        Set<String> tags = TaggingUtils.getTags(project);
        if (tags != null && tags.size() > 0)
        {
            Layout tagListLayout = new CssLayout() {
                @Override
                protected String getCss(Component c) {
                    if (c instanceof Label) {
                        return "float: left; line-height:18px; padding-right: 3px;";
                    } else {
                        return "float: left; padding-right: 5px";
                    }
                }
            };
            tagListLayout.setSizeFull();
            Label tagLabel = new Label("Tags:");
            tagLabel.setSizeUndefined();
            tagListLayout.addComponent(tagLabel);

            for (String tag : tags) {
                Button tagButton = new Button(tag, new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        String tag = event.getButton().getCaption();
                        util.getNavigator().navigateTagView(tag);
                    }
                });
                tagButton.setStyleName(Button.STYLE_LINK);
                tagListLayout.addComponent(tagButton);
            }

            Button editButton = new Button("(edit tags)");
            if (util.getLoggedInUser() != null) {
                editButton.addListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        paintTagEdit();
                    }
                });
            } else {
                editButton.setEnabled(false);
                editButton.setDescription("Login to tag this project");
            }
            editButton.setStyleName(Button.STYLE_LINK);
            tagListLayout.addComponent(editButton);

            layout.addComponent(tagListLayout);
        }
        else {
            if (util.getLoggedInUser() != null) {
                Button addTagButton = new Button("(add tags)", new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        paintTagEdit();
                    }
                });
                addTagButton.setStyleName(Button.STYLE_LINK);
                layout.addComponent(addTagButton);
            }
        }

    }

    @SuppressWarnings({ "deprecation", "serial" })
    private void paintTagEdit() {
        layout.removeAllComponents();
        layout.setSizeFull();

        paintEditTagList();
        layout.addComponent(tagListLayout);

        paintTagComboBoxLayout();
        layout.addComponent(tagComboBoxLayout);

        Button doneButton = new Button("done", new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                paintTagView();
            }
        });
        doneButton.setStyle(Button.STYLE_LINK);
        layout.addComponent(doneButton);
    }

    @SuppressWarnings({ "deprecation", "serial" })
    private void paintEditTagList() {
        tagListLayout.removeAllComponents();
        tagListLayout.setWidth("100%"); //$NON-NLS-1$

        Label tagLabel = new Label("Tags:");
        tagLabel.setSizeUndefined();
        tagListLayout.addComponent(tagLabel);

        SortedSet<String> tags = TaggingUtils.getTags(project);
        for (String tag : tags) {
            Layout l = new CssLayout() {
                @Override
                protected String getCss(Component c) {
                    if (c instanceof Label) {
                        return "float: left; line-height: 18px; padding-right: 3px"; //$NON-NLS-1$
                    } else {
                        return "float: left; line-height: 18px; padding-right: 5px"; //$NON-NLS-1$
                    }
                }
            };
            Label label = new Label(tag);
            label.setSizeUndefined();
            l.addComponent(label);

            Button removeButton = new Button("(remove)", new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    String tag = (String) event.getButton().getData();
                    TaggingUtils.removeTag(project, tag);
                    util.persist(project);
                    paintTagEdit();
                }
            });
            removeButton.setStyleName(Button.STYLE_LINK);
            removeButton.setData(tag);
            removeButton.setSizeUndefined();
            l.addComponent(removeButton);
            tagListLayout.addComponent(l);
        }
    }



    @SuppressWarnings("serial")
    private void paintTagComboBoxLayout() {
        tagComboBoxLayout.removeAllComponents();
        // add tag input field
        final ComboBox tagComboBox = new ComboBox();
        SortedMap<String,Integer> availableTags = taggingService.getTags(Project.class);
        for (String tag : availableTags.keySet()) {
            tagComboBox.addItem(tag);
        }
        tagComboBox.setNewItemsAllowed(true);
        tagComboBox.setNewItemHandler(new NewItemHandler() {
            @Override
            public void addNewItem(String newItemCaption) {
                Collection<Validator> validators = tagComboBox.getValidators();
                for (Validator val : validators) {
                    if (!val.isValid(newItemCaption)) {
                        Iterator<Component> it = tagComboBoxLayout.getComponentIterator();
                        it.next(); // this is the combobox
                        if (!it.hasNext()) {
                            tagComboBoxLayout.addComponent(new Label("Use lowercase letters without whitespaces only."));
                        }
                        return;
                    }
                }
                if (!tagComboBox.containsId(newItemCaption)) {
                    TaggingUtils.addTags(project, newItemCaption);
                    util.persist(project);
                    paintTagEdit(); // TODO performance?
                }
            }
        });
        tagComboBox.addListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                if (!TaggingUtils.hasTag(project, event.getProperty().getValue().toString())) {
                    TaggingUtils.addTags(project, event.getProperty().getValue().toString());
                    util.persist(project);
                    paintTagEdit(); // TODO performance?
                }
            }
        });

        tagComboBox.setImmediate(true);
        tagComboBox.setInputPrompt("Enter tag confirmed by enter");
        tagComboBox.setWidth("300px");
        tagComboBox.setNullSelectionAllowed(false);
        tagComboBox.focus();

        tagComboBox.addValidator(new TagValidator());
        tagComboBoxLayout.addComponent(tagComboBox);
    }

    private static class TagValidator implements Validator {
        private static final long serialVersionUID = -8732474379417852332L;

        @Override
        public void validate(Object value) throws InvalidValueException {
            if (!isValid(value)) {
                throw new InvalidValueException("tags can only contain lowercase characters and digits");
            }
        }

        @Override
        public boolean isValid(Object value) {
            char[] chars = value.toString().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                int charType = Character.getType(chars[i]);
                if (!(charType == Character.LOWERCASE_LETTER
                || charType == Character.DECIMAL_DIGIT_NUMBER)) {
                    return false;
                }
            }
            return true;
        }
    }
}
