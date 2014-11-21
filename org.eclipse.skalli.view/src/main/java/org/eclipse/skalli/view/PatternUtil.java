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
package org.eclipse.skalli.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("nls")
public class PatternUtil {

    public static String adjustProjectName(String projectName) {

        Pattern pattern = Pattern.compile("[\\s]");
        Matcher matcher = pattern.matcher(projectName);

        return matcher.replaceAll("_");
    }
}
