/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to characterize properties as derived properties. A class annotated
 * with <code>@Derived</code> must declare all its properties as derived.
 * <pre>
 *   @PropertyName @Derived public static final String PROPERTY_FULLNAME = "fullName";
 * </pre>
 * If a class defines such a property name, it must declare a getter method,
 * but is no longer required to declare a private field with the same name
 * or a corrsponding setter methods (but it may).
 * <pre>
 *    private String fullName= ""; // OPTIONAL
 *    ...
 *    public String getFullName() { // REQUIRED
 *      return fullName;
 *    }
 *
 *    public void setFullName(String fullName) { //OPTIONAL
 *      this.fullName = fullName;
 *    }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface Derived {
}
