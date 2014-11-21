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
package org.eclipse.skalli.view.ext.impl.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    private BundleContext context;
    private static Activator instance;

    @Override
    public void start(BundleContext context) throws Exception {
        instance = this;
        this.context = context;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        instance = null;
    }

    public static Activator getDefault() {
        return instance;
    }

    public BundleContext getContext() {
        return context;
    }

}
