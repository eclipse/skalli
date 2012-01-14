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
package org.eclipse.skalli.model.ext.maven;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class MavenVersionsComparatorTest {

    private MavenVersionsComparator comparator;
    private MavenVersionsComparator comparatorLastAsLowest;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        comparator = new MavenVersionsComparator();
        comparatorLastAsLowest = new MavenVersionsComparator(MavenVersionsComparator.SortOrder.DESCENDING);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        comparator = null;
    }

    @Test
    public void testCompare() {

        assertEquals(0, comparator.compare("0.0.1", "0.0.1"));
        assertEquals(0, comparatorLastAsLowest.compare("0.0.1", "0.0.1"));

        String[][] lessValues = new String[][] {
                { "0.0.1", "0.0.2" }, //
                { "0.0.2", "0.0.10" }, //
                { "0.1.0", "0.2.0" }, //
                { "0.2.0", "0.11.0" }, //
                { "1.0.0", "2.0.0" }, //
                { "2.0.0", "10.0.0" }, //
                { "2.999.0", "10.0.0" }, //
                { "0.1.2-SNAPSHOT", "0.1.10-SNAPSHOT"},//
                { "2.0", "2.0.0" }, //
                { "2.0", "2.1.0" }, //

        };
        for (String[] versions : lessValues) {
            assertThat( "ASCENDING sort: '" + versions[0] + "'.compare('" + versions[1] + "')",
                    comparator.compare(versions[0], versions[1]), is(-1));
            assertThat("ASCENDING sort: '" + versions[1] + "'.compare('" + versions[0] + "')",
                    comparator.compare(versions[1], versions[0]), is(+1));

            assertThat("DESCENDING sort: '" + versions[0] + "'.compare('" + versions[1] + "')",
                    comparatorLastAsLowest.compare(versions[0], versions[1]), is(+1));
            assertThat("DESCENDING sort: '" + versions[1] + "'.compare('" + versions[0] + "')",
                    comparatorLastAsLowest.compare(versions[1], versions[0]), is(-1));
        }
    }

}
