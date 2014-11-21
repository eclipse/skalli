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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

@SuppressWarnings("nls")
public class ExpressionTest {

    private static final String NAME = "foobar";
    private static final String[] ARGS = new String[]{"a", "b"};

    @Test
    public void testConstructor() throws Exception {
        Expression expr = new Expression(NAME);
        assertEquals(NAME, expr.getName());
        assertEquals(0, expr.getArguments().length);

        expr = new Expression(NAME, (String[])null);
        assertEquals(NAME, expr.getName());
        assertEquals(0, expr.getArguments().length);

        expr = new Expression(NAME, new String[0]);
        assertEquals(NAME, expr.getName());
        assertEquals(0, expr.getArguments().length);


        expr = new Expression(NAME, ARGS);
        assertEquals(NAME, expr.getName());
        assertTrue(Arrays.equals(ARGS, expr.getArguments()));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullName() throws Exception {
        new Expression(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullNameWithArgs() throws Exception {
        new Expression(null, ARGS);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBlankName() throws Exception {
        new Expression("");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBlankNameWithArgs() throws Exception {
        new Expression("", ARGS);
    }

    @Test
    public void testToString() throws Exception {
        assertEquals(NAME, new Expression(NAME).toString());
        assertEquals(NAME, new Expression(NAME, (String[])null).toString());
        assertEquals(NAME + "()", new Expression(NAME, new String[0]).toString());
        assertEquals(NAME + "(a)", new Expression(NAME, new String[]{"a"}).toString());
        assertEquals(NAME + "(a,b)", new Expression(NAME, new String[]{"a", "b"}).toString());
        assertEquals(NAME + "('a,b',b)", new Expression(NAME, new String[]{"a,b", "b"}).toString());
        assertEquals(NAME + "(' a,\tb','b\n')", new Expression(NAME, new String[]{" a,\tb", "b\n"}).toString());
        assertEquals(NAME + "(' a,\tb','b\n')", new Expression(NAME, new String[]{" a,\tb", "b\n"}).toString());
        assertEquals(NAME + "('a\\'b','b\"c')", new Expression(NAME, new String[]{"a'b", "b\"c"}).toString());
    }
}
