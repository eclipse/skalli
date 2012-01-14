/*******************************************************************************
 * Copyright (c) 2011 SAP AG and others.
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

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;

/**
 * A {@link CustomComponent} that also implements the {@link Field} interface, so
 * form fields can be created by composing Vaadin components.
 *
 * Subclasses need to set the composition root, which is done in the constructor
 * typically.
 *
 * See {@link Field} for more information on the individual methods. Typically,
 * only {@link #setValue(Object)}, {@link #getValue()} and {@link #getType()}
 * need to be overridden.
 *
 */
public abstract class CustomField extends CustomComponent implements Field {

    private final class AbstractFieldDelegator extends AbstractField {
        private static final long serialVersionUID = 1L;

        @Override
        public Class<?> getType() {
            return CustomField.this.getType();
        }

        @Override
        public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
            if (isReadThrough() || !isModified()) {
                fireValueChange(false);
            }
        }

        @Override
        public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
            CustomField.this.setValue(newValue);
        }

        @Override
        public Object getValue() {
            return CustomField.this.getValue();
        }

        private Object superGetValue(){
          return super.getValue();
        }

        private void superSetValue(Object newValue){
            super.setValue(newValue);
        }
    }

    private static final long serialVersionUID = 1L;
    private AbstractFieldDelegator abstractField;



    public CustomField() {
        abstractField = new AbstractFieldDelegator();
    }

    @Override
    public boolean isInvalidCommitted() {
        return abstractField.isInvalidCommitted();
    }

    @Override
    public void setInvalidCommitted(boolean isCommitted) {
        abstractField.setInvalidCommitted(isCommitted);
    }

    @Override
    public void commit() throws SourceException, InvalidValueException {
        abstractField.commit();
    }

    @Override
    public void discard() throws SourceException {
        abstractField.discard();
    }

    @Override
    public boolean isWriteThrough() {
        return abstractField.isWriteThrough();
    }

    @Override
    public void setWriteThrough(boolean writeThrough) throws SourceException, InvalidValueException {
        abstractField.setWriteThrough(writeThrough);
    }

    @Override
    public boolean isReadThrough() {
        return abstractField.isReadThrough();
    }

    @Override
    public void setReadThrough(boolean readThrough) throws SourceException {
        abstractField.setReadThrough(readThrough);
    }

    @Override
    public boolean isModified() {
        return abstractField.isModified();
    }

    @Override
    public void addValidator(Validator validator) {
        abstractField.addValidator(validator);
    }

    @Override
    public void removeValidator(Validator validator) {
        abstractField.removeValidator(validator);
    }

    @Override
    public Collection<Validator> getValidators() {
        return abstractField.getValidators();
    }

    @Override
    public boolean isValid() {
        return abstractField.isValid();
    }

    @Override
    public void validate() throws InvalidValueException {
        abstractField.validate();
    }

    @Override
    public boolean isInvalidAllowed() {
        return abstractField.isInvalidAllowed();
    }

    @Override
    public void setInvalidAllowed(boolean invalidValueAllowed) throws UnsupportedOperationException {
        abstractField.setInvalidAllowed(invalidValueAllowed);
    }

    @Override
    public Object getValue() {
        return abstractField.superGetValue();
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
        abstractField.superSetValue(newValue);
    }

    @Override
    public abstract Class<?> getType();

    @Override
    public void addListener(ValueChangeListener listener) {
        abstractField.addListener(listener);
    }

    @Override
    public void removeListener(ValueChangeListener listener) {
        abstractField.removeListener(listener);
    }

    @Override
    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
        abstractField.valueChange(event);
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        abstractField.setPropertyDataSource(newDataSource);
    }

    @Override
    public Property getPropertyDataSource() {
        return abstractField.getPropertyDataSource();
    }

    @Override
    public int getTabIndex() {
        return abstractField.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        abstractField.setTabIndex(tabIndex);
    }

    @Override
    public boolean isRequired() {
        return abstractField.isRequired();
    }

    @Override
    public void setRequired(boolean required) {
        abstractField.setRequired(required);
    }

    @Override
    public void setRequiredError(String requiredMessage) {
        abstractField.setRequiredError(requiredMessage);
    }

    @Override
    public String getRequiredError() {
        return abstractField.getRequiredError();
    }

    @Override
    public void focus() {
        super.focus();
    }

    @Override
    public String toString() {
        return abstractField.toString();
    }

}
