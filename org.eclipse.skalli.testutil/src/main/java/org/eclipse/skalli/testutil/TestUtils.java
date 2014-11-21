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
package org.eclipse.skalli.testutil;

import java.io.File;
import java.io.IOException;

public class TestUtils {

    public static File createTempDir(String prefix) throws IOException {
        File tmpFile = File.createTempFile(prefix, "-tmp", new File("target"));
        if (tmpFile.delete() && tmpFile.mkdirs()) {
            return tmpFile;
        }
        throw new IOException("Failed to create tmpDir " + tmpFile);
    }
}
