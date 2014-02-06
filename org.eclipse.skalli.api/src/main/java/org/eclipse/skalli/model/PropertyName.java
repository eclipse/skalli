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
 * Annotation to declare public properties of an entity or extension.
 * Public properties appear in forms in the UI and can be used
 * for REST queries like <tt>/api/projects?property=extension.property</tt>.
 * <p>
 * Example:
 * <pre>
 *   &#064;PropertyName public static final String PROPERTY_PROJECTID = "projectId";
 * </pre>
 * If a class defines such an annotated string constant, it must also declare a private
 * field with the same name and a corresponding getter method, e.g.
 * <pre>
 *    private String projectId = "";
 *    public String getProjectId() { ... }
 * </pre>
 * Note, this mechanism works also for collection-like properties (e.g. lists or sets).
 * A REST query like <tt>/api/projects?property=extension.col&pattern=item</tt>
 * would search for an entry <tt>"item"</tt> in the collection-like property
 * <tt>col></tt> of a given extension. For more complex data types you need
 * a property accessor, see annotation {@link Property}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertyName {

    /**
     * Returns the position of the property.
     *
     * This position will be used to sort a set of properties whenever the order matters
     * (e.g. in a form). The default value of -1 indicates that the order does not matter,
     * or the property should be ignored altogether.
     */
    int position() default -1;

}
