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
package org.eclipse.skalli.services.extension.rest;

import com.thoughtworks.xstream.converters.Converter;

public interface RestConverter extends Converter {

    public String getAlias();

    public Class<?> getConversionClass();

    public String getApiVersion();

    public String getNamespace();

    public String getXsdFileName();

    public String getHost();
}
