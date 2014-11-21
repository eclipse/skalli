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
package org.eclipse.skalli.core.validation;

public enum ValidationAction {
    /**
     * Defines a scheduled action that queues all entities
     * of a given type for validation. The actual validation is
     * executed asynchronously.
     */
    QUEUE,

    /**
     * Defines a scheduled action that queues all entities
     * of a all types for validation. The actual validation is
     * executed asynchronously.
     */
    QUEUE_ALL,

    /**
     * Defines a scheduled action that validates all entities
     * of a given type synchronously.
     */
    VALIDATE,

    /**
     * Defines a scheduled action that validates all entities
     * of all types synchronously.
     */
    VALIDATE_ALL
}
