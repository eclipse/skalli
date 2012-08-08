package org.eclipse.skalli.services.search;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

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
        testParams.put(SearchQuery.PARAM_PROPERTY, "x.y.z");
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
    public void testGetSetProperty() throws Exception {
        SearchQuery query = new SearchQuery();
        query.setProperty("a.b");
        assertEquals("a.b", query.getProperty());
        assertEquals("a", query.getShortName());
        assertEquals("b", query.getPropertyName());
        query.setPropertyName("c");
        assertEquals("a.c", query.getProperty());
        query.setShortName("d");
        assertEquals("d.c", query.getProperty());
        query.setPropertyName(null);
        assertNull(query.getProperty());
        query.setPropertyName("");
        assertNull(query.getProperty());
        query.setPropertyName("x");
        query.setShortName(null);
        assertEquals(SearchQuery.PROJECT_PREFIX + "x", query.getProperty());
        query.setShortName("");
        assertEquals(SearchQuery.PROJECT_PREFIX + "x", query.getProperty());
        query.setShortName("y");
        assertEquals("y.x", query.getProperty());
    }

}