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

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

/**
 * Helper class for defining an access path for properties
 * of an {@link EntityBase entity}.
 */
public class Expression {

    private String name;
    private String[] arguments;

    /**
     * Creates an expression for a parameterless property accessor
     * or a simple property.
     *
     * @param name the name of the property to access.
     */
    public Expression(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("argument 'name' must not be null or blank");
        }
        this.name = name;
    }

    /**
     * Creates an expression for a property accessor with
     * given name and arguments.
     *
     * @param name the name of the property to access. This name
     * is mapped to a property accessor method annotated with {@link Property}
     * by capitalizing it and adding the prefix <tt>"get"</tt>.
     * @param arguments the arguments to pass to the property accessor method.
     */
    public Expression(String name, String... arguments) {
        this(name);
        this.arguments = arguments;
    }

    /**
     * Returns the name of the property to access.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the arguments to pass to the property accessor method.
     */
    public String[] getArguments() {
        return arguments != null? arguments : new String[0];
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(arguments);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Expression)) {
            return false;
        }
        Expression other = (Expression) obj;
        if (!Arrays.equals(arguments, other.arguments)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        if (arguments == null) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name);
        sb.append('(');
        for (int i = 0; i < arguments.length; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            String arg = arguments[i];
            boolean quoted = StringUtils.containsAny(arg, ", '\"\t\r\n\b\f\\");
            if (quoted) {
                sb.append('\'');
                sb.append(StringUtils.replace(arg, "'", "\\'"));
                sb.append('\'');
            } else {
                sb.append(arg);
            }
        }
        sb.append(')');
        return sb.toString();
    }
}