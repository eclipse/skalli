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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.PropertyName;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("nls")
public class MinMaxSizeValidatorTest {

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
        items.add("item3");
    }

    private MinMaxSizeValidator createValidator(int minExpectedOccurrences, int maxAllowedOccurrences) {
        return new MinMaxSizeValidator(Severity.WARNING, TestExtension.class, TestExtension.PROPERTY_ITEMS,
                "Items", minExpectedOccurrences, maxAllowedOccurrences);
    }

    private MinMaxSizeValidator createValidatorNoCaption(int minExpectedOccurrences, int maxAllowedOccurrences) {
        return new MinMaxSizeValidator(Severity.WARNING, TestExtension.class, TestExtension.PROPERTY_ITEMS,
                null, minExpectedOccurrences, maxAllowedOccurrences);
    }

    @Test
    public void testValidateServerityIsLess() {
        SortedSet<Issue> itmes = createValidator(0, 1).validate(TestUUIDs.TEST_UUIDS[0], items,
                Severity.INFO);
        Assert.assertEquals(1, itmes.size());
    }

    @Test
    public void testValidateServerityIsEquals() {
        SortedSet<Issue> itmes = createValidator(0, 1)
                .validate(TestUUIDs.TEST_UUIDS[0], items, Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
    }

    @Test
    public void testValidateServerityIsGreater() {
        SortedSet<Issue> itmes = createValidator(0, 1)
                .validate(TestUUIDs.TEST_UUIDS[0], items, Severity.FATAL);
        Assert.assertEquals(0, itmes.size());
    }

    @Test
    public void testValidateAtMost() {
        SortedSet<Issue> itmes = createValidator(0, 2)
                .validate(TestUUIDs.TEST_UUIDS[0], items, Severity.WARNING);
        Issue itme1 = itmes.first();
        Assert.assertEquals("Items should have at most 2 entries, but it currently has 3",
                itme1.getMessage());
    }

    @Test
    public void testValidateAtMostNoCaption() throws Exception {
        SortedSet<Issue> itmes = createValidatorNoCaption(0, 2)
                .validate(TestUUIDs.TEST_UUIDS[0], items, Severity.WARNING);
        Issue itme1 = itmes.first();
        Assert.assertEquals("Property 'items' should have at most 2 entries, but it currently has 3",
                itme1.getMessage());
    }

    @Test
    public void testValidateAtMostOne() {
        SortedSet<Issue> itmes = createValidator(0, 1)
                .validate(TestUUIDs.TEST_UUIDS[0], items, Severity.WARNING);
        Issue itme1 = itmes.first();
        Assert.assertEquals("Items should have at most one entry, but it currently has 3",
                itme1.getMessage());
    }

    @Test
    public void testValidateAtMostOneNoCaption() throws Exception {
        SortedSet<Issue> itmes = createValidatorNoCaption(0, 1)
                .validate(TestUUIDs.TEST_UUIDS[0], items, Severity.WARNING);
        Issue itme1 = itmes.first();
        Assert.assertEquals("Property 'items' should have at most one entry, but it currently has 3",
                itme1.getMessage());
    }

    @Test
    public void testValidateAtLeast() {
        SortedSet<Issue> itmes = createValidator(5, Integer.MAX_VALUE)
                .validate(TestUUIDs.TEST_UUIDS[0], items, Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
        Issue itme1 = itmes.first();
        Assert.assertEquals("Items should have at least 5 entries, but it currently has only 3",
                itme1.getMessage());
    }

    @Test
    public void testValidateAtLeastNoCaption() {
        SortedSet<Issue> itmes = createValidatorNoCaption(5, Integer.MAX_VALUE)
                .validate(TestUUIDs.TEST_UUIDS[0], items, Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
        Issue itme1 = itmes.first();
        Assert.assertEquals("Property 'items' should have at least 5 entries, but it currently has only 3",
                itme1.getMessage());
    }

    @Test
    public void testValidateAtLeastOne() {
        SortedSet<Issue> itmes = createValidator(1, Integer.MAX_VALUE)
                .validate(TestUUIDs.TEST_UUIDS[0], Collections.emptySet(), Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
        Issue itme1 = itmes.first();
        Assert.assertEquals("Items should have at least one entry",
                itme1.getMessage());
    }

    @Test
    public void testValidateAtLeastOneNoCaption() {
        SortedSet<Issue> itmes = createValidatorNoCaption(1, Integer.MAX_VALUE)
                .validate(TestUUIDs.TEST_UUIDS[0], Collections.emptySet(), Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
        Issue itme1 = itmes.first();
        Assert.assertEquals("Property 'items' should have at least one entry",
                itme1.getMessage());
    }

    @Test
    public void testValidateMinMaxIssues() {
        SortedSet<Issue> itmes = createValidator(5, 5)
                .validate(TestUUIDs.TEST_UUIDS[0], items, Severity.WARNING);
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
        SortedSet<Issue> itmes = createValidator(3, 3)
                .validate(TestUUIDs.TEST_UUIDS[0], items, Severity.WARNING);
        Assert.assertEquals(0, itmes.size());
    }

    @Test
    public void testValidateMinValue() {
        SortedSet<Issue> itmes = createValidator(1, Integer.MAX_VALUE)
                .validate(TestUUIDs.TEST_UUIDS[0], null, Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
    }

    @Test
    public void testValidatePropertyOptional() {
        SortedSet<Issue> itmes = createValidator(0, Integer.MAX_VALUE)
                .validate(TestUUIDs.TEST_UUIDS[0], null, Severity.WARNING);
        Assert.assertEquals(0, itmes.size());
    }

    @Test
    public void testValidatePropertyNotOptionalButOccurrenceIs0() {
        SortedSet<Issue> itmes = createValidator(1, Integer.MAX_VALUE)
                .validate(TestUUIDs.TEST_UUIDS[0], null, Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
    }

    @Test
    public void testValidateMinMaxValueNoIssue() {
        SortedSet<Issue> itmes = createValidator(1, 1)
                .validate(TestUUIDs.TEST_UUIDS[0], "a single value", Severity.WARNING);
        Assert.assertEquals(0, itmes.size());
    }

    @Test
    public void testValidateMinMaxValueOneIssue() {
        SortedSet<Issue> itmes = createValidator(2, 2)
                .validate(TestUUIDs.TEST_UUIDS[0], "a single value", Severity.WARNING);
        Assert.assertEquals(1, itmes.size());
    }

}
