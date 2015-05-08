/*******************************************************************************
 * Copyright (c) 2010-2015 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.services.gerrit;

/**
 * Enumeration to express that a Gerrit project attribute should be either
 * <code>true</code>, <code>false</code> or inherited from the parent project.
 */
public enum InheritableBoolean {
    TRUE, FALSE, INHERIT;

    public static InheritableBoolean valueOf(boolean b) {
        return b? TRUE : FALSE;
    }
}
