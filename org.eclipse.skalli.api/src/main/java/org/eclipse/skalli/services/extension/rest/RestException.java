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

/**
 * Exception for reporting parsing issues detected by the REST API.
 */
public class RestException extends Exception {

    private static final long serialVersionUID = -3423250944505957992L;

    private int line = -1;
    private int column = -1;

    /**
     * Constructs a new exception with the specified detail message.
     * The line/column, where the issue occuredm, is unknown.
     *
     * @param  message the detail message describing the parsing issue.
     */
    public RestException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message.
     * The line/column, where the issue occuredm, is unknown.
     *
     * @param message the detail message describing the parsing issue.
     * @param cause  the root cause of the issue.
     */
    public RestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message and line/column information.
     *
     * @param message the detail message describing the parsing issue.
     * @param line  the line of the input, where the issue was found.
     * @param column  the column of the input, where the issue was found.
     */
    public RestException(String message, int line, int column) {
        this(message, null, line, column);
    }

    /**
     * Constructs a new exception with the specified root cause and line/column information.
     *
     * @param cause  the root cause of the issue.
     * @param line  the line of the input, where the issue was found.
     * @param column  the column of the input, where the issue was found.
     */
    public RestException(Throwable cause, int line, int column) {
        this(null, cause, line, column);
    }

    /**
     * Constructs a new exception with the specified message, root cause and line/column information.
     *
     * @param message  the detail message describing the parsing issue.
     * @param cause  the root cause of the issue.
     * @param line  the line of the input, where the issue was found.
     * @param column  the column of the input, where the issue was found.
     */
    public RestException(String message, Throwable cause, int line, int column) {
        super(message, cause);
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the line of the input, where the issue was found,
     * or -1 if the line is unknown.
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the column of the input, where the issue was found,
     * or -1 if the column is unknown.
     */
    public int getColumn() {
        return column;
    }

}
