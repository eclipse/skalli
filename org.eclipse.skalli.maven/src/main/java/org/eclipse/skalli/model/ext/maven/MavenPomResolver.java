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

import java.io.IOException;
import java.util.UUID;

import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.model.ext.maven.internal.MavenPom;

public interface MavenPomResolver {

    /**
     * Returns <code>true</code> if this POM resolver is applicable to the given SCM location.
     * @param scmLocation  the SCM location to check.
     */
    public boolean canResolve(String scmLocation);

    public MavenPom getMavenPom(UUID project, String scmLocation, String relativePath) throws IOException, ValidationException;

}
