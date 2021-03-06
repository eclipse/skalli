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
 * Enumeration representing the various submit types that can
 * be assigned to a Gerrit project. The default is <code>MERGE_IF_NECESSARY</code>.
 */
public enum SubmitType {
    MERGE_IF_NECESSARY, REBASE_IF_NECESSARY, FAST_FORWARD_ONLY, MERGE_ALWAYS, CHERRY_PICK
}