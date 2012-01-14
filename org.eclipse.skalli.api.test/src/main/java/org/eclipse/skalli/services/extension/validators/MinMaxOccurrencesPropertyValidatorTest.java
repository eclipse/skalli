/*******************************************************************************
 * Copyright (c) 2010 - 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.services.extension.validators;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.testutil.PropertyHelperUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MinMaxOccurrencesPropertyValidatorTest {

    class TestExtension extends ExtensibleEntityBase {
        Set<String> items = new HashSet<String>();

        @PropertyName
        public static final String PROPERTY_ITEMS = "items";

        public Set<String> getItems() {
            return items;
        }
    }

    private Set<String> items;

    @Before
    public void before() {
        items = new HashSet<String>();
        items.add("item1");
        items.add("item2");
    }

    private MinMaxOccurrencesPropertyValidator createValidator(int minExpectedOccurrences,
            int maxAllowedOccurrences)
    {
        return new MinMaxOccurrencesPropertyValidator(Severity.WARNING, TestExtension.class, "foo",
                "bar", minExpectedOccurrences, maxAllowedOccurrences);
    }

    @Test
    public void testValidateServerityIsLess() {
        SortedSet<Issue> itmes = createValidator(0, 1).validate(PropertyHelperUtils.TEST_UUIDS[0], items,
                Severity.INFO);
        Assert.assertEquals(1, itmes.size());
    }

    @Test
    public void testValidateServerityIsEquals() {
        SortedSet<Issue> itmes = createValidator(0, 1)
                .validate(PropertyHelperUtils.TEST_UUIDS[0], items, Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
    }

    @Test
    public void testValidateServerityIsGreater() {
        SortedSet<Issue> itmes = createValidator(0, 1)
                .validate(PropertyHelperUtils.TEST_UUIDS[0], items, Severity.FATAL);
        Assert.assertEquals(0, itmes.size());
    }

    @Test
    public void testValidateMax() {
        SortedSet<Issue> itmes = createValidator(0, 1)
                .validate(PropertyHelperUtils.TEST_UUIDS[0], items, Severity.WARNING);
        Issue itme1 = itmes.first();
        Assert.assertTrue("Property 'bar' of extension 'foo' should not have more than 1 values, but it currently has 2 values."
                .compareTo(itme1.getMessage()) == 0);
    }

    @Test
    public void testValidateMin() {
        SortedSet<Issue> itmes = createValidator(5, Integer.MAX_VALUE)
                .validate(PropertyHelperUtils.TEST_UUIDS[0], items, Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
        Issue itme1 = itmes.first();
        Assert.assertTrue("Property 'bar' of extension 'foo' should have at least 5 values, but it currently has only 2 values."
                .compareTo(itme1.getMessage()) == 0);
    }

    @Test
    public void testValidateMinMaxIssues() {
        SortedSet<Issue> itmes = createValidator(5, 5)
                .validate(PropertyHelperUtils.TEST_UUIDS[0], items, Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
        int minIssues = 0;
        int maxIssues = 0;
        for (Issue issue : itmes) {
            if (issue.getMessage().contains("should have at least")) {
                minIssues++;
            }
            if (issue.getMessage().contains("should not have more than")) {
                maxIssues++;
            }
        }
        Assert.assertEquals(1, minIssues);
        Assert.assertEquals(0, maxIssues);
    }

    @Test
    public void testValidateMinMaxNoIssues() {
        SortedSet<Issue> itmes = createValidator(2, 2)
                .validate(PropertyHelperUtils.TEST_UUIDS[0], items, Severity.WARNING);
        Assert.assertEquals(0, itmes.size());
    }

    @Test
    public void testValidateMinValue() {
        SortedSet<Issue> itmes = createValidator(1, Integer.MAX_VALUE)
                .validate(PropertyHelperUtils.TEST_UUIDS[0], null, Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
    }

    @Test
    public void testValidatePropertyOptional() {
        SortedSet<Issue> itmes = createValidator(0, Integer.MAX_VALUE)
                .validate(PropertyHelperUtils.TEST_UUIDS[0], null, Severity.WARNING);
        Assert.assertEquals(0, itmes.size());
    }

    @Test
    public void testValidatePropertyNotOptionalButOccurrenceIs0() {
        SortedSet<Issue> itmes = createValidator(1, Integer.MAX_VALUE)
                .validate(PropertyHelperUtils.TEST_UUIDS[0], null, Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
    }

    @Test
    public void testValidateMinMaxValueNoIssue() {
        SortedSet<Issue> itmes = createValidator(1, 1)
                .validate(PropertyHelperUtils.TEST_UUIDS[0], "a single value", Severity.WARNING);
        Assert.assertEquals(0, itmes.size());
    }

    @Test
    public void testValidateMinMaxValueOneIssue() {
        SortedSet<Issue> itmes = createValidator(2, 2)
                .validate(PropertyHelperUtils.TEST_UUIDS[0], "a single value", Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
    }

}
