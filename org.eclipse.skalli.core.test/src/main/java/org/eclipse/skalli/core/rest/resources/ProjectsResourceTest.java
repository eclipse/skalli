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
package org.eclipse.skalli.core.rest.resources;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import org.eclipse.skalli.model.Expression;
import org.eclipse.skalli.model.NoSuchPropertyException;
import org.eclipse.skalli.model.Project;
import org.eclipse.skalli.services.search.QueryParseException;
import org.eclipse.skalli.services.search.SearchQuery;
import org.eclipse.skalli.testutil.TestExtension;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Test;

@SuppressWarnings("nls")
public class ProjectsResourceTest {

    private static final String PREFIX = "testext.";

    @Test
    public void testMatches() throws Exception {
        SearchQuery queryParams = new SearchQuery();
        queryParams.setPattern("fo.+", false);
        Pattern pattern = queryParams.getPattern();
        assertTrue(ProjectsResource.matches("foobar", pattern));
        assertFalse(ProjectsResource.matches("", pattern));
        assertFalse(ProjectsResource.matches(null, pattern));
        assertTrue(ProjectsResource.matches(Arrays.asList("foo", "foobar"), pattern));
        assertFalse(ProjectsResource.matches(Arrays.asList("a", "b"), pattern));
        assertFalse(ProjectsResource.matches((Collection<?>)null, pattern));
        assertFalse(ProjectsResource.matches(Collections.emptyList(), pattern));
        assertTrue(ProjectsResource.matches(
                new StringBuilder("foobar"), pattern)); // StringBuilder#toString("foobar") == "foobar" => true
        assertFalse(ProjectsResource.matches(
                new String[]{ "foobar" }, pattern)); // String[] is not iterable and toString() == "[foobar]" => false
    }

    @Test
    public void testMatchesLineEndings() throws Exception {
        SearchQuery queryParams = new SearchQuery();
        queryParams.setPattern(".+", false);
        assertTrue(ProjectsResource.matches("\nfoobar", queryParams.getPattern()));
    }

    @Test
    public void testMatchesIgnoreCase() throws Exception {
        SearchQuery queryParams = new SearchQuery();
        queryParams.setPattern("FoO.+", false);
        assertFalse(ProjectsResource.matches("foobar", queryParams.getPattern()));
        queryParams.setPattern("FoO.+", true);
        assertTrue(ProjectsResource.matches("foobar", queryParams.getPattern()));
    }

    @Test
    public void testIsBlank() throws Exception {
        assertTrue(ProjectsResource.isBlank(null));
        assertTrue(ProjectsResource.isBlank(""));
        assertFalse(ProjectsResource.isBlank("foobar"));
        assertTrue(ProjectsResource.isBlank(Collections.emptyList()));
        assertFalse(ProjectsResource.isBlank(Arrays.asList("foobar")));
        assertFalse(ProjectsResource.isBlank(Arrays.asList((String)null)));
        assertFalse(ProjectsResource.isBlank(Arrays.asList("")));
        assertFalse(ProjectsResource.isBlank(new Integer(4711)));
    }

    @Test
    public void testMatchesPropertyQueryOfProject() throws Exception {
        ProjectsResource resource = new ProjectsResource();
        Project project = createProject();
        SearchQuery queryParams = new SearchQuery();

        // properties of the project
        project.setDescription("foobar");
        queryParams.setProperty(Project.PROPERTY_DESCRIPTION);
        queryParams.setPattern("foo.+", false);
        assertTrue(resource.matchesPropertyQuery(project, project, queryParams));

        // !property
        queryParams.setProperty("!" + Project.PROPERTY_DESCRIPTION);
        assertFalse(resource.matchesPropertyQuery(project, project, queryParams));
        project.setDescription("");
        assertTrue(resource.matchesPropertyQuery(project, project, queryParams));
        project.setDescription(null);
        assertTrue(resource.matchesPropertyQuery(project, project, queryParams));
        queryParams.setNegate(false);
    }

    @Test
    public void testMatchesPropertyQueryOfExtension() throws Exception {
        ProjectsResource resource = new ProjectsResource();
        Project project = createProject();
        TestExtension ext = project.getExtension(TestExtension.class);
        SearchQuery queryParams = new SearchQuery();

        // non-collection property
        queryParams.setProperty(PREFIX + TestExtension.PROPERTY_STR);
        queryParams.setPattern("foo", false);
        assertFalse(resource.matchesPropertyQuery(project, ext, queryParams));
        queryParams.setPattern("foo.+", false);
        assertTrue(resource.matchesPropertyQuery(project, ext, queryParams));

        // !property
        queryParams.setProperty("!" + PREFIX + TestExtension.PROPERTY_STR);
        assertFalse(resource.matchesPropertyQuery(project, ext, queryParams));
        queryParams.setNegate(false);

        // !property with null value
        ext.setStr(null);
        queryParams.setProperty(PREFIX + TestExtension.PROPERTY_STR);
        assertFalse(resource.matchesPropertyQuery(project, ext, queryParams));
        queryParams.setProperty("!" + PREFIX + TestExtension.PROPERTY_STR);queryParams.setNegate(true);
        assertTrue(resource.matchesPropertyQuery(project, ext, queryParams));
        queryParams.setNegate(false);

        // property with collection-like value
        queryParams.setProperty(PREFIX + TestExtension.PROPERTY_ITEMS);
        queryParams.setPattern("b", false);
        assertTrue(resource.matchesPropertyQuery(project, ext, queryParams));
        queryParams.setPattern("b.*", false);
        assertTrue(resource.matchesPropertyQuery(project, ext, queryParams));
        queryParams.setPattern("x", false);
        assertFalse(resource.matchesPropertyQuery(project, ext, queryParams));

        // property with accessor method
        queryParams.setProperty(PREFIX + "inner(a).str");
        queryParams.setPattern("x", false);
        assertTrue(resource.matchesPropertyQuery(project, ext, queryParams));
        queryParams.setPattern("z", false);
        assertFalse(resource.matchesPropertyQuery(project, ext, queryParams));

        queryParams.setProperty(PREFIX + "inner('b').str()");
        queryParams.setPattern(".y.", false);
        assertTrue(resource.matchesPropertyQuery(project, ext, queryParams));

        // inner() returns null, if no inner with the given argument exists
        // thus, the negated expression should match
        queryParams.setProperty("!" + PREFIX + "inner(c).str()");
        assertTrue(resource.matchesPropertyQuery(project, ext, queryParams));
        queryParams.setProperty("!" + PREFIX + "inner('').str()");
        assertTrue(resource.matchesPropertyQuery(project, ext, queryParams));
        queryParams.setProperty("!" + PREFIX + "inner(' ').str()");
        assertTrue(resource.matchesPropertyQuery(project, ext, queryParams));
    }

    @Test
    public void testNoSuchProperty() throws Exception {
        assertQueryParseException("inner", new Expression("inner"));
        assertQueryParseException("inner()", new Expression("inner"));
        assertQueryParseException("unknown", new Expression("unknown"));
        assertQueryParseException("unpublished", new Expression("unpublished"));
    }

    private void assertQueryParseException(String query, Expression expr) {
        ProjectsResource resource = new ProjectsResource();
        Project project = createProject();
        TestExtension ext = project.getExtension(TestExtension.class);
        SearchQuery queryParams = new SearchQuery();
        try {
            queryParams.setProperty(PREFIX + query);
            resource.matchesPropertyQuery(project, ext, queryParams);
            fail("QueryParseException expected");
        } catch (QueryParseException e) {
            NoSuchPropertyException cause = (NoSuchPropertyException)e.getCause();
            assertEquals(expr, cause.getExpression());
        }
    }


    private Project createProject() {
        Project project = new Project("bla.blubb", null, "Blubber");
        project.setUuid(TestUUIDs.TEST_UUIDS[0]);
        TestExtension ext = new TestExtension();
        ext.setStr("foobar");
        ext.addItem("a");
        ext.addItem("b");
        ext.addItem("c");
        ext.putInner("a", new TestExtension.Inner("x"));
        ext.putInner("b", new TestExtension.Inner("xyz"));
        project.addExtension(ext);
        return project;
    }
}
