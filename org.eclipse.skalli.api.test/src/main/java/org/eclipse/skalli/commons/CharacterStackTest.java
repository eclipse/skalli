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
package org.eclipse.skalli.commons;

import static org.junit.Assert.*;

import java.util.EmptyStackException;

import org.junit.Test;

public class CharacterStackTest {

    @Test
    public void testDefaultConstructor() throws Exception {
        CharacterStack stack = new CharacterStack();
        assertEmpty(stack);
    }

    @Test
    public void testPushPopPeek() throws Exception {
        CharacterStack stack = new CharacterStack();
        stack.push('x');
        assertEquals(1, stack.size());
        assertFalse(stack.isEmpty());
        assertEquals('x', stack.peek());
        assertEquals('x', stack.pop());
        assertEmpty(stack);
    }

    @Test
    public void testResize() throws Exception {
        CharacterStack stack = new CharacterStack(0);
        assertTrue(stack.isEmpty());
        for (int i=0; i<100; ++i) {
            assertEquals(i, stack.size());
            stack.push((char)i);
            assertEquals(i+1, stack.size());
        }
        assertEquals(100, stack.size());
        for (int i=99; i>=0; --i) {
            assertEquals(i+1, stack.size());
            assertEquals((char)i, stack.peek());
            assertEquals((char)i, stack.pop());
            assertEquals(i, stack.size());
        }
        assertEmpty(stack);
    }

    @Test
    public void testClear() throws Exception {
        CharacterStack stack = new CharacterStack();
        stack.push('x');
        assertEquals(1, stack.size());
        stack.clear();
        assertEmpty(stack);
    }

    private void assertEmpty(CharacterStack stack) {
        assertEquals(0, stack.size());
        assertTrue(stack.isEmpty());
        try {
            stack.peek();
            stack.pop();
            fail("EmptyStackException expected");
        } catch (EmptyStackException e) {
            // expected
        }
    }
}
