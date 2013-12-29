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
package org.eclipse.skalli.commons;

import java.util.Arrays;
import java.util.EmptyStackException;

/**
 * Simple stack implementation for characters that grows automatically,
 * but never shrinks. The default initial size is 16 characters, and
 * the stack grows in chunks of 16 characters.
 */
public class CharacterStack {

    private static int CHUNK_SIZE = 16;

    private char[] stack;
    private int next;

    /**
     * Creates a character stack with the default initial size of
     * 16 characters.
     */
    public CharacterStack() {
        this(CHUNK_SIZE);
    }

    /**
     * Creates a character stack with the given initial size.
     * @param size  the initial size of the stack. If zero or a negative
     * number is specified the stack will have an initial size of zero.
     *
     */
    public CharacterStack(int size) {
        stack = new char[size >= 0 ? size : 0];
        next = 0;
    }

    /**
     * Pushes the given character on top of the stack.
     * @param c  the character to push.
     */
    public void push(char c) {
        if (next == stack.length) {
            stack = Arrays.copyOf(stack, stack.length + CHUNK_SIZE);
        }
        stack[next] = c;
        ++next;
    }

    /**
     * Removes the top from the stack and returns it.
     * @return the top of the stack.
     * @throws EmptyStackException  if the stack is already empty.
     */
    public char pop() {
        if (next == 0) {
            throw new EmptyStackException();
        }
        --next;
        return stack[next];
    }

    /**
     * Returns the top of the stack without removing it.
     * @return the top of the stack.
     * @throws EmptyStackException  if the stack is already empty.
     */
    public char peek() {
        if (next == 0) {
            throw new EmptyStackException();
        }
        return stack[next-1];
    }

    /**
     * Returns <code>true</code>, if the stack is empty.
     */
    public boolean isEmpty() {
        return next == 0;
    }

    /**
     * Returns the current number of stacked characters.
     */
    public int size() {
        return next;
    }

    /**
     * Removes all elements from the stack. The stack afterwards
     * will be {@link #isEmpty() empty}.
     */
    public void clear() {
        next = 0;
    }
}