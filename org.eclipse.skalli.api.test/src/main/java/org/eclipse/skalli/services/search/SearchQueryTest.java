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
package org.eclipse.skalli.services.search;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.skalli.model.Expression;
import org.junit.Test;

@SuppressWarnings("nls")
public class SearchQueryTest {

    @Test
    public void testDefaults() throws Exception {
        SearchQuery query = new SearchQuery();
        assertNull(query.getQuery());
        assertNull(query.getUser());
        assertNull(query.getTag());
        assertNull(query.getExtensions());
        assertNull(query.getShortName());
        assertNull(query.getPropertyName());
        assertEquals(0, query.getStart());
        assertEquals(Integer.MAX_VALUE, query.getCount());
        assertNull(query.getPattern());
    }

    @Test
    public void testPropertyQueryDefaults() throws Exception {
        Map<String,String> testParams = new HashMap<String,String>();
        testParams.put(SearchQuery.PARAM_PROPERTY, "name");

        SearchQuery query = new SearchQuery(testParams);
        assertEquals(SearchQuery.DEFAULT_SHORTNAME, query.getShortName());
        assertEquals(".+", query.getPattern().pattern());
    }

    @Test(expected = QueryParseException.class)
    public void testInvalidProperty() throws Exception {
        Map<String,String> testParams = new HashMap<String,String>();
        testParams.put(SearchQuery.PARAM_PROPERTY, "x#y#z");
        new SearchQuery(testParams);
    }

    @Test(expected = QueryParseException.class)
    public void testInvalidPattern() throws Exception {
        Map<String,String> testParams = new HashMap<String,String>();
        testParams.put(SearchQuery.PARAM_PROPERTY, "name");
        testParams.put(SearchQuery.PARAM_PATTERN, "[a-");
        new SearchQuery(testParams);
    }

    @Test
    public void testNegate() throws Exception {
        Map<String,String> testParams = new HashMap<String,String>();
        testParams.put(SearchQuery.PARAM_PROPERTY, "!devInf.scmLocations");

        SearchQuery query = new SearchQuery(testParams);
        assertEquals("devInf", query.getShortName());
        assertEquals("scmLocations", query.getPropertyName());
        assertTrue(query.isNegate());
    }

    @Test
    public void testPageInfo() throws Exception {
        Map<String,String> testParams = new HashMap<String,String>();

        testParams.put(SearchQuery.PARAM_START, "abc");
        SearchQuery query = new SearchQuery(testParams);
        assertEquals(0, query.getStart());

        testParams.put(SearchQuery.PARAM_START, "-4711");
        query = new SearchQuery(testParams);
        assertEquals(0, query.getStart());

        testParams.put(SearchQuery.PARAM_COUNT, "abc");
        query = new SearchQuery(testParams);
        assertEquals(Integer.MAX_VALUE, query.getCount());

        testParams.put(SearchQuery.PARAM_COUNT, "-4711");
        query = new SearchQuery(testParams);
        assertEquals(Integer.MAX_VALUE, query.getCount());
    }

    @Test
    public void testMapConstructor() throws Exception {
        Map<String,String> testParams = new HashMap<String,String>();
        testParams.put(SearchQuery.PARAM_QUERY, "*");
        testParams.put(SearchQuery.PARAM_USER, "hugo");
        testParams.put(SearchQuery.PARAM_TAG, "foobar");
        testParams.put(SearchQuery.PARAM_EXTENSIONS, "devInf,info,xyz");
        testParams.put(SearchQuery.PARAM_PROPERTY, "!devInf.scmLocations");
        testParams.put(SearchQuery.PARAM_COUNT, "4711");
        testParams.put(SearchQuery.PARAM_START, "123");
        testParams.put(SearchQuery.PARAM_PATTERN, "abc.+");

        SearchQuery query = new SearchQuery(testParams);
        assertEquals("*", query.getQuery());
        assertEquals("hugo", query.getUser());
        assertEquals("foobar", query.getTag());
        assertArrayEquals(new String[]{"devInf", "info", "xyz"}, query.getExtensions());
        assertEquals("devInf", query.getShortName());
        assertEquals("scmLocations", query.getPropertyName());
        assertEquals("devInf.scmLocations", query.getProperty());
        assertEquals(123, query.getStart());
        assertEquals(123, query.getPagingInfo().getStart());
        assertEquals(4711, query.getCount());
        assertEquals(4711, query.getPagingInfo().getCount());
        assertEquals("abc.+", query.getPattern().pattern());
        assertTrue(query.isNegate());
    }

    @Test
    public void testSetters() throws Exception {
        SearchQuery query = new SearchQuery();
        query.setQuery("*");
        assertEquals("*", query.getQuery());
        query.setUser("hugo");
        assertEquals("hugo", query.getUser());
        query.setTag("foobar");
        assertEquals("foobar", query.getTag());
        query.setExtensions(new String[]{"devInf", "info", "xyz"});
        assertArrayEquals(new String[]{"devInf", "info", "xyz"}, query.getExtensions());
        query.addExtension("abc");
        assertArrayEquals(new String[]{"devInf", "info", "xyz", "abc"}, query.getExtensions());
        assertTrue(query.hasExtension("abc"));
        query.setExtensions(null);
        assertNull(query.getExtensions());
        assertFalse(query.hasExtension("abc"));
        query.setPagingInfo(new PagingInfo(123, 4711));
        assertEquals(123, query.getStart());
        assertEquals(123, query.getPagingInfo().getStart());
        assertEquals(4711, query.getCount());
        assertEquals(4711, query.getPagingInfo().getCount());
        query.setPagingInfo(null);
        assertEquals(0, query.getStart());
        assertEquals(Integer.MAX_VALUE, query.getCount());
    }

    @Test
    public void testQueryToExpression() throws Exception {
        assertExpression("property", "property", null);
        assertExpression("property()", "property", null);
        assertExpression("property('')", "property", new String[]{""});
        assertExpression("property(\"\")", "property", new String[]{""});
        assertExpression("property( \t \n )", "property", null);
        assertExpression("property(\"a\")", "property", new String[]{"a"});
        assertExpression("property('a')", "property", new String[]{"a"});
        assertExpression("property('a\"b')", "property", new String[]{"a\"b"});
        assertExpression("property(\"a'b\")", "property", new String[]{"a'b"});
        assertExpression("property(\"a\"\"b\")", "property", new String[]{"a\"b"});
        assertExpression("property('a''b')", "property", new String[]{"a'b"});
        assertExpression("property(\"a\",\"b\")", "property", new String[]{"a", "b"});
        assertExpression("property('a','b')", "property", new String[]{"a", "b"});
        assertExpression("property(\"a\",'b')", "property", new String[]{"a", "b"});
        assertExpression("property(\t\"a\"  ,\n  'b' )", "property", new String[]{"a", "b"});
        assertExpression("property(a)", "property", new String[]{"a"});
        assertExpression("property(a,b)", "property", new String[]{"a", "b"});
        assertExpression("property(a,' b ')", "property", new String[]{"a", " b "});
        assertExpression("property( a\t , \n b  )", "property", new String[]{"a", "b"});
        assertExpression("property(a,'\"b')", "property", new String[]{"a", "\"b"});
        assertExpression("property(\"a'b\",b)", "property", new String[]{"a'b", "b"});
        assertExpression("property(a\",\"b)", "property", new String[]{"a\"", "b"});
    }

    @Test
    public void testInvalidQueryToExpressions() throws Exception {
        assertInvalidExpression("()");
        assertInvalidExpression("( \n)");
        assertInvalidExpression(" \t()");
        assertInvalidExpression("()\n  ");
        assertInvalidExpression("('a')");
        assertInvalidExpression("(a)");
        assertInvalidExpression("inva%lid");
        assertInvalidExpression("inva%lid('x')");
        assertInvalidExpression("property(");
        assertInvalidExpression("property(]");
    }

    @Test
    public void testSetProperty() throws Exception {
        assertProperty("a", SearchQuery.DEFAULT_SHORTNAME, new Expression("a"));
        assertProperty(".a", SearchQuery.DEFAULT_SHORTNAME, new Expression("a"));
        assertProperty(" .a", SearchQuery.DEFAULT_SHORTNAME, new Expression("a"));
        assertProperty(" . a ", SearchQuery.DEFAULT_SHORTNAME, new Expression("a"));
        assertProperty("a()", SearchQuery.DEFAULT_SHORTNAME, new Expression("a"));
        assertProperty(".a()", SearchQuery.DEFAULT_SHORTNAME, new Expression("a"));
        assertProperty("a('a')", SearchQuery.DEFAULT_SHORTNAME, new Expression("a", new String[]{"a"}));
        assertProperty(".a('a')", SearchQuery.DEFAULT_SHORTNAME, new Expression("a", new String[]{"a"}));
        assertProperty("ext.a", "ext", new Expression("a"));
        assertProperty("ext#4711.a", "ext#4711", new Expression("a")); // extensions shortName can be any string!
        assertProperty("ext. a", "ext", new Expression("a"));
        assertProperty("ext .a", "ext", new Expression("a"));
        assertProperty("ext.a.b", "ext", new Expression("a"), new Expression("b"));
        assertProperty(" \r\next\r\n. a\t\t . b ", "ext", new Expression("a"), new Expression("b"));
        assertProperty("ext.a().b", "ext", new Expression("a"), new Expression("b"));
        assertProperty("ext. a(   ) .b", "ext", new Expression("a"), new Expression("b"));
        assertProperty("ext.a.b()", "ext", new Expression("a"), new Expression("b"));
        assertProperty("ext.a().b()", "ext", new Expression("a"), new Expression("b"));
        assertProperty("ext.a(x,y).b(z)", "ext",
                new Expression("a", new String[]{"x","y"}), new Expression("b", new String[]{"z"}));
        assertProperty(" ext . a(   x   ,\"  y  \") . b('\r\nz')", "ext",
                new Expression("a", new String[]{"x","  y  "}), new Expression("b", new String[]{"\r\nz"}));
        assertProperty("ext.a(x.y).b(z)", "ext", new Expression("a", new String[]{"x.y"}),
                new Expression("b", new String[]{"z"}));
        assertProperty("ext.a('x.y').b(z)", "ext", new Expression("a", new String[]{"x.y"}),
                new Expression("b", new String[]{"z"}));
        assertProperty("ext.a(\"x.y\").b(z)", "ext", new Expression("a", new String[]{"x.y"}),
                new Expression("b", new String[]{"z"}));
    }

    @Test
    public void testSetInvalidProperty() throws Exception {
        assertInvalidProperty("a.");
        assertInvalidProperty("a . ");
        assertInvalidProperty("a..b");
        assertInvalidProperty("a;b");
        assertInvalidProperty("ext#4711.a#0815"); // extensions shortName can be any string,
    }

    private void assertInvalidExpression(String queryString) throws Exception {
        try {
            assertExpression(queryString, null, (String[]) null);
            fail("QueryParseException expected");
        } catch (QueryParseException e) {
        }
    }

    private void assertInvalidProperty(String queryString) throws Exception {
        try {
            assertProperty(queryString, null, (Expression[]) null);
            fail("QueryParseException expected");
        } catch (QueryParseException e) {
        }
    }

    private void assertExpression(String queryString, String name, String[] args) throws Exception {
        SearchQuery query = new SearchQuery();
        Expression expr = query.asExpression(queryString);
        assertEquals(name, expr.getName());
        assertNotNull(expr.getArguments());
        if (args == null) {
            args = new String[0];
        }
        assertTrue(Arrays.equals(args, expr.getArguments()));
    }

    private void assertProperty(String queryString, String shortName, Expression...expressions) throws Exception {
        SearchQuery query = new SearchQuery();
        query.setProperty(queryString);
        assertTrue(Arrays.equals(expressions, query.getExpressions()));
        assertEquals(shortName, query.getShortName());
    }
}

