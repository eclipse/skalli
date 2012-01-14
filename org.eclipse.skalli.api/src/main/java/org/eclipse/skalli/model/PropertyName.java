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
 * Annotation to characterize a string constant as property name. Example:
 * <pre>
 *   @PropertyName public static final String PROPERTY_PROJECTID = "projectId";
 *   @PropertyName public static final String PROPERTY_STRINGS = "strings";
 * </pre>
 * If a class defines such a property name, it must also declare a private field
 * with the same name and corresponding getter/setter methods, e.g.
 * <pre>
 *    private String projectId = "";
 *    ...
 *    public String getProjectId() {
 *      return projectId;
 *    }
 *
 *    public void setProjectId(String projectId) {
 *      this.projectId = projectId;
 *    }
 * </pre>
 * For a property of a type implementing {@link java.lang.Collection} or any subclasses,
 * the class must define an adder method while the setter is optional, e.g.
 * <pre>
 *    private TreeSet&lt;String&gt; strings = new TreeSet&lt;String&gt;();
 *    ...
 *    public void addString(Collection&lt;String&gt; strings) {
 *      this.strings = new TreeSet&lt;String&gt;(strings);
 *    }
 * </pre>
 * The name of the adder method must be "add" + the singular form of the the string constant value,
 * e.g. "strings" becomes "addString" and "entries" becomes "entry".
 * <p>
 * Note, that if a property is annotated additionally with {@link Derived}
 * then it must declare a getter method only. The setter/adder methods and the
 * private field are optional in this case.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertyName {

    /**
     * Returns the position of the property.
     *
     * This position will be used to sort a set of properties whenever the order matters
     * (e.g. when determining the properties to be rendered in the user interface automatically).
     */
    int position() default -1;

}
