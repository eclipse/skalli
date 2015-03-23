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
package org.eclipse.skalli.services.extension.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.restlet.engine.Method;
import org.restlet.representation.Representation;

/**
 * Annotation for methods that provide partial resource modification.
 *
 * Its semantics is equivalent to HTTP PATCH method described in RFC 5789.
 * The annotated method must have one {@link Representation entity} parameter.
 * <p>
 * Example:
 *
 * <pre>
 * &#064;Patch
 * public Representation patch(Representation entity);
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Method("PATCH")
public @interface Patch {

    /**
     * Specifies the media type extension of the response entity.
     *
     * @return The result media types.
     */
    String value() default "";
}
