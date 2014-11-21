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
package org.eclipse.skalli.model.ext.maven.internal;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.skalli.model.ValidationException;

public interface MavenPomParser {

    /**
     * Reads and parses the given input stream and returns the content
     * as <code>MavenPom</code> instance.
     *
     * @param in  the stream to read and parse.
     *
     * @throws IOException  if an i/o error occured.
     * @throws ValidationException  if the given input stream contains no valid Maven POM.
     */
    public MavenPom parse(InputStream in) throws IOException, ValidationException;

}
