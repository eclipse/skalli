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
package org.eclipse.skalli.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining a <i>property accessor</i>, i.e. a method that grants access
 * to the properties of an entity or extension. A property accessor allows complex parameterized
 * REST queries like <tt>/api/projects?property=extension.property("param1", "param2)</tt>.
 * Example:
 * <pre>
 *    &#064;Property public Object getProperty(String param1, String param2) { ... }
 * </pre>
 * Note that the accessor method must start with the prefix <tt>get</tt> followed by the
 * capitalized property name. The method may have an arbitrary number of string parameters
 * (or none) and may return a result of arbitrary type. This allows to concatenate queries to
 * explore more complex data structures. Example:
 * <pre>
 *   /api/projects?property=extension.property("param1", "param2).key("key")&pattern=foobar
 * </pre>
 * This query will first search <tt>extension</tt> for a property accessor
 * <tt>getProperty(String, String)</tt> and invoke it. Then it will search the result
 * for a property accessor <tt>getKey(String)</tt> and invoke that. The result of
 * <tt>getKey</tt> will then be matched with the given <tt>pattern</tt>. Note, that
 * for the pattern matching the result of the last property accessor in the query
 * is converted to a string with {@link Object#toString()}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Property {
}
