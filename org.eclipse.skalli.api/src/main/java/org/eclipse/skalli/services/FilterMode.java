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
package org.eclipse.skalli.services;

public enum FilterMode {
    /**
     * Applies all given filters from left to right to all bundles.
     */
    ALL,

    /**
     * Applies all given filters from left to right to all bundles
     * and stops when one of the filters accepted
     */
    SHORT_CIRCUIT,

    /**
     * Applies all given filters from left to right to all bundles
     * and stops, when the first
     */
    FIRST_MATCHING
}
