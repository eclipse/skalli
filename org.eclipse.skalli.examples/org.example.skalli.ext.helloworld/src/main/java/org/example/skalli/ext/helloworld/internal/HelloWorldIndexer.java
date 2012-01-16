/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/edl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.example.skalli.ext.helloworld.internal;

import org.eclipse.skalli.services.extension.IndexerBase;
import org.example.skalli.model.ext.helloworld.HelloWorldProjectExt;

class HelloWorldIndexer extends IndexerBase<HelloWorldProjectExt> {

    @Override
    protected void indexFields(HelloWorldProjectExt entity) {
        addField(HelloWorldProjectExt.PROPERTY_NAME, entity.getName(), true, false);
    }

}
