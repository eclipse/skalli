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
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.TextField;

public class MultiTextField extends CustomField {

    private static final long serialVersionUID = -1926189316257001272L;

    private static final String STYLE_LAYOUT = "multitext-layout"; //$NON-NLS-1$
    private static final String STYLE_LINE_LAYOUT = "multitext-line"; //$NON-NLS-1$
    private static final String STYLE_BUTTON = "multitext-btn"; //$NON-NLS-1$

    private Collection<String> values;

    private boolean readOnly;
    private CssLayout layout;
    private List<TextFieldEntry> textFieldEntries;
    private String description;
    private String inputPrompt;
    private Validator validator;
    private int columns = -1;
    private int maxSize;

    private static class TextFieldEntry {
        public TextFieldEntry(TextField textField) {
            this.textField = textField;
        }

        TextField textField;
        Button removeButton;
    }

    public MultiTextField(String caption, Collection<String> values) {
        if (values == null) {
            throw new IllegalArgumentException("argument 'value' must not be null");
        }
        setCaption(caption);
        this.values = values;
        this.maxSize = Integer.MAX_VALUE;
        initTextFieldEntries(values);
        layout = new CssLayout();
        layout.setStyleName(STYLE_LAYOUT);
        renderTextFields();
        setCompositionRoot(layout);
    }

    private void renderTextFields() {
        int size = textFieldEntries.size();
        int last = size - 1;
        for (int i = 0; i <= last; ++i) {
            TextFieldEntry textFieldEntry = textFieldEntries.get(i);
            CssLayout css = new CssLayout();
            css.setStyleName(STYLE_LINE_LAYOUT);
            textFieldEntry.textField.setReadOnly(readOnly);
            css.addComponent(textFieldEntry.textField);
            if (!readOnly) {
                if (size > 1) {
                    Button b = createRemoveButton();
                    textFieldEntry.removeButton = b;
                    css.addComponent(b);
                }
                if (size < maxSize && i == last) {
                    css.addComponent(createAddButton());
                }
            }
            layout.addComponent(css);
        }
    }

    private void initTextFieldEntries(Collection<String> values) {
        textFieldEntries = new ArrayList<TextFieldEntry>();
        List<String> nonBlankValues = new ArrayList<String>();
        if (values != null) {
            for (String value : values) {
                if (StringUtils.isNotBlank(value)) {
                    nonBlankValues.add(value);
                }
            }
        }
        if (nonBlankValues.isEmpty()) {
            TextField tf = createTextField(""); //$NON-NLS-1$
            textFieldEntries.add(new TextFieldEntry(tf));
        } else {
            for (String value : nonBlankValues) {
                TextField tf = createTextField(value);
                textFieldEntries.add(new TextFieldEntry(tf));
            }
        }
    }

    private TextField createTextField(String value) {
        TextField tf = new TextField();
        tf.setImmediate(true);
        if (columns > 0) {
            tf.setColumns(columns);
        }
        if (description != null) {
            tf.setDescription(description);
        }
        if (inputPrompt != null) {
            tf.setInputPrompt(inputPrompt);
        }
        if (validator != null) {
            tf.addValidator(validator);
            tf.setValidationVisible(false);
        }
        if (value != null) {
            tf.setValue(value);
        }
        return tf;
    }

    private Button createAddButton() {
        Button b = new Button("Add");
        b.setStyleName(Button.STYLE_LINK);
        b.addStyleName(STYLE_BUTTON);
        b.setDescription("Add another entry");
        //b.setIcon(ICON_BUTTON_ADD);
        b.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                TextField tf = createTextField("");
                textFieldEntries.add(new TextFieldEntry(tf));
                layout.removeAllComponents();
                renderTextFields();
            }
        });
        return b;
    }

    private Button createRemoveButton() {
        Button b = new Button("Remove");
        b.setStyleName(Button.STYLE_LINK);
        b.addStyleName(STYLE_BUTTON);
        b.setDescription("Remove this entry");
        //b.setIcon(ICON_BUTTON_REMOVE);
        b.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                Button b = event.getButton();
                Iterator<TextFieldEntry> it = textFieldEntries.iterator();
                while (it.hasNext()) {
                    TextFieldEntry textFieldEntry = it.next();
                    if (textFieldEntry.removeButton == b) {
                        it.remove();
                        break;
                    }
                }
                layout.removeAllComponents();
                renderTextFields();
            }
        });
        return b;
    }

    public void setInputPrompt(String inputPrompt) {
        this.inputPrompt = inputPrompt;
        for (TextFieldEntry textFieldEntry : textFieldEntries) {
            textFieldEntry.textField.setInputPrompt(inputPrompt);
        }
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
        for (TextFieldEntry textFieldEntry : textFieldEntries) {
            textFieldEntry.textField.setDescription(description);
        }
    }

    public void setColumns(int columns) {
        this.columns = columns;
        for (TextFieldEntry textFieldEntry : textFieldEntries) {
            textFieldEntry.textField.setColumns(columns);
        }
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize <= 0 ? Integer.MAX_VALUE : maxSize;
        requestRepaint();
    }

    @Override
    public void addValidator(Validator validator) {
        this.validator = validator;
        for (TextFieldEntry textFieldEntry : textFieldEntries) {
            textFieldEntry.textField.addValidator(validator);
            textFieldEntry.textField.setValidationVisible(false);
        }
        super.addValidator(new MultipleTextFieldValidator(validator));
    }

    private static class MultipleTextFieldValidator implements Validator {

        private static final long serialVersionUID = 1929837970818336522L;

        private Validator validator;

        public MultipleTextFieldValidator(Validator validator) {
            this.validator = validator;

        }

        @Override
        public boolean isValid(Object value) {
            if (value != null) {
                for (Object item : (Collection<?>) value) {
                    String s = (String) item;
                    if (StringUtils.isBlank(s)) {
                        continue;
                    }
                    if (!validator.isValid(s)) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public void validate(Object value)
                throws InvalidValueException {
            List<String> errors = new ArrayList<String>();
            if (value != null) {
                for (Object item : (Collection<?>) value) {
                    String s = (String) item;
                    if (StringUtils.isBlank(s)) {
                        continue;
                    }
                    try {
                        validator.validate(s);
                    } catch (InvalidValueException e) {
                        errors.add(e.getMessage());
                    }
                }
            }
            if (!errors.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String error : errors) {
                    if (sb.length() > 0) {
                        sb.append("<br>"); //$NON-NLS-1$
                    }
                    sb.append(error);
                }
                throw new InvalidValueException(sb.toString());
            }
        }
    }

    @Override
    public Object getValue() {
        LinkedHashSet<String> value = new LinkedHashSet<String>(textFieldEntries.size());
        copyTextFieldValues(value);
        return value;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        layout.removeAllComponents();
        renderTextFields();
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        values.clear();
        copyTextFieldValues(values);
    }

    private void copyTextFieldValues(Collection<String> values) {
        for (TextFieldEntry textFieldEntry : textFieldEntries) {
            String value = (String) textFieldEntry.textField.getValue();
            if (StringUtils.isNotBlank(value)) {
                values.add(value);
            }
        }
    }

    @Override
    public Class<?> getType() {
        return Collection.class;
    }

}
