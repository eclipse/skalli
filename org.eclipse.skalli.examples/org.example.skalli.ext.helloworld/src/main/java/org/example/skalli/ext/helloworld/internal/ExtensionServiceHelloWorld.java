/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/edl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.example.skalli.ext.helloworld.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.skalli.commons.CollectionUtils;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.ExtensionServiceBase;
import org.eclipse.skalli.services.extension.Indexer;
import org.eclipse.skalli.services.extension.PropertyValidator;
import org.eclipse.skalli.services.extension.rest.RestConverter;
import org.eclipse.skalli.services.extension.validators.StringLengthValidator;
import org.example.skalli.model.ext.helloworld.HelloWorldProjectExt;

public class ExtensionServiceHelloWorld extends ExtensionServiceBase<HelloWorldProjectExt> {

    private static final Map<String, String> CAPTIONS = CollectionUtils.asMap(new String[][] {
            { HelloWorldProjectExt.PROPERTY_NAME, "Your name" },
            { HelloWorldProjectExt.PROPERTY_FRIENDS, "Your friends" } });

    @Override
    public Class<HelloWorldProjectExt> getExtensionClass() {
        return HelloWorldProjectExt.class;
    }

    @Override
    public HelloWorldProjectExt newExtension() {
        return new HelloWorldProjectExt();
    }

    @Override
    public String getModelVersion() {
        return HelloWorldProjectExt.MODEL_VERSION;
    }

    @Override
    public String getNamespace() {
        return HelloWorldProjectExt.NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "HelloWorldProjectExt.xsd";
    }

    @Override
    public String getShortName() {
        return "Hello";
    }

    @Override
    public String getCaption() {
        return "Hello World Extension";
    }

    @Override
    public String getCaption(String propertyName) {
        String caption = CAPTIONS.get(propertyName);
        if (caption == null) {
            caption = super.getCaption(propertyName);
        }
        return caption;
    }

    @Override
    public String getDescription() {
        return "Hello World Extension - a simple example.";
    }

    @Override
    public List<PropertyValidator> getPropertyValidators(String propertyName, String caption) {
        if (HelloWorldProjectExt.PROPERTY_NAME.equals(propertyName)) {
            return getNameValidators(caption);
        } else {
            return super.getPropertyValidators(propertyName, caption);
        }
    }

    private List<PropertyValidator> getNameValidators(String caption) {
        List<PropertyValidator> validators = new ArrayList<PropertyValidator>();
        validators.add(getLengthValidator(caption));
        return validators;
    }

    private StringLengthValidator getLengthValidator(String caption) {
        StringLengthValidator stringLengthValidator = new StringLengthValidator(Severity.ERROR,
                HelloWorldProjectExt.class,
                HelloWorldProjectExt.PROPERTY_NAME, caption, 2, 200);
        stringLengthValidator.setValueRequired(true);
        return stringLengthValidator;
    }

    @Deprecated
    @Override
    public RestConverter<?> getRestConverter(String host) {
        return new HelloWorldConverter(host);
    }

    @Override
    public RestConverter<HelloWorldProjectExt> getRestConverter() {
        return new HelloWorldConverter();
    }

    @Override
    public Indexer<HelloWorldProjectExt> getIndexer() {
        return new HelloWorldIndexer();
    }
}