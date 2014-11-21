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
package org.eclipse.skalli.model.ext.maven;

import org.eclipse.skalli.model.Derived;
import org.eclipse.skalli.model.ExtensionEntityBase;

@Derived
public class MavenReactorProjectExt extends ExtensionEntityBase {

    public static final String MODEL_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/Model/Extension-MavenReactor"; //$NON-NLS-1$

    @Derived
    public static final String PROPERTY_MAVEN_REACTOR = "mavenReactor"; //$NON-NLS-1$

    private MavenReactor mavenReactor;

    public MavenReactor getMavenReactor() {
        return mavenReactor;
    }

    public void setMavenReactor(MavenReactor mavenReactor) {
        this.mavenReactor = mavenReactor;
    }
}
