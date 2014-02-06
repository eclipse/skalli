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

import java.text.MessageFormat;


public class NoSuchPropertyException extends RuntimeException {

    private static final long serialVersionUID = -5333642036204491847L;

    private EntityBase entity;
    private Expression expression;

    public NoSuchPropertyException() {
        super();
    }

    public NoSuchPropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchPropertyException(String message) {
        super(message);
    }

    public NoSuchPropertyException(Throwable cause) {
        super(cause);
    }

    public NoSuchPropertyException(EntityBase entity, String propertyName) {
        this(entity, new Expression(propertyName), null);
    }

    public NoSuchPropertyException(EntityBase entity, Expression expression) {
        this(entity, expression, null);
    }

    public NoSuchPropertyException(EntityBase entity, String propertyName, Throwable cause) {
        this(entity, new Expression(propertyName), null);
    }

    public NoSuchPropertyException(EntityBase entity, Expression expression, Throwable cause) {
        super(getMessage(entity, expression), cause);
        this.expression = expression;
        this.entity = entity;
    }

    public String getPropertyName() {
        return expression != null? expression.getName() : null;
    }

    public Expression getExpression() {
        return expression;
    }

    public EntityBase getEntity() {
        return entity;
    }

    private static String getMessage(EntityBase entity, Expression expression) {
        if (entity instanceof Project) {
            return MessageFormat.format("Failed to retrieve property \"{0}\" of project \"{1}\"",
                expression, entity);
        } else if (entity instanceof ExtensionEntityBase) {
            return MessageFormat.format("Failed to retrieve property \"{0}\" of extension \"{1}\" of project \"{2}\"",
                    expression, entity.getClass().getSimpleName(),
                    ((ExtensionEntityBase)entity).getExtensibleEntity());
        } else {
            return MessageFormat.format("Failed to retrieve property \"{0}\" of entity \"{1}\"",
                    expression, entity);
        }
    }
}
