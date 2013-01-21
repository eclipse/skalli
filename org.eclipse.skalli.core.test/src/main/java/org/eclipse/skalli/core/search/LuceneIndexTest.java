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
package org.eclipse.skalli.core.search;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.skalli.core.search.LuceneIndex;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.search.FacetedSearchResult;
import org.eclipse.skalli.services.search.PagingInfo;
import org.eclipse.skalli.services.search.SearchResult;
import org.eclipse.skalli.testutil.BundleManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

@SuppressWarnings("nls")
public class LuceneIndexTest {

    public static final String FIELD = "value";
    public static final String FACET = "facet";

    @SuppressWarnings("rawtypes")
    private ServiceRegistration<ExtensionService> serviceRegistration;

    private TestEntity entity1;
    private TestEntity entity2;
    private TestEntity entity3;
    private TestEntity entity4;
    private TestEntity entity5;
    private List<TestEntity> entities;
    private LuceneIndex<TestEntity> idx;

    @Before
    public void setup() throws Exception {
        serviceRegistration = BundleManager.registerService(ExtensionService.class, new TestExtensionService(), null);
        Assert.assertNotNull(serviceRegistration);

        entity1 = new TestEntity("bob", "firstname");
        entity2 = new TestEntity("alice", "firstname");
        entity3 = new TestEntity("alice smith", "fullname");
        entity4 = new TestEntity("alice in wonderland", "sentence");
        entity5 = new TestEntity("alice is used in many examples", "sentence");
        entities = new LinkedList<TestEntity>();
        entities.add(entity1);
        entities.add(entity2);
        entities.add(entity3);
        entities.add(entity4);
        entities.add(entity5);
        idx = new LuceneIndex<TestEntity>(new TestEntityService(entities));
        idx.reindexAll();
    }

    @After
    public void tearDown() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    @Test
    public void testSearch() throws Exception {
        SearchResult<TestEntity> res = idx.search(new String[] { FIELD }, "bob", null);
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.getResultCount());
        Assert.assertNotNull(res.getResult());
        Assert.assertEquals(1, res.getResult().size());
        Assert.assertEquals("bob", res.getResult().get(0).getEntity().getValue());
        Assert.assertEquals("bob", res.getResult().get(0).getValue(FIELD, false));
        Assert.assertEquals("<em>bob</em>", res.getResult().get(0).getValue(FIELD, true));
    }

    @Test
    public void testSearch_specialChars() throws Exception {
        SearchResult<TestEntity> res = idx.search(new String[] { FIELD }, "(", null);
        Assert.assertNotNull(res);
        Assert.assertEquals(0, res.getResultCount());
        Assert.assertNotNull(res.getResult());
        Assert.assertEquals(0, res.getResult().size());
    }

    @Test
    public void testSearch_specialChars_asterisk() throws Exception {
        SearchResult<TestEntity> res = idx.search(new String[] { FIELD }, "*", null);
        Assert.assertNotNull(res);
        Assert.assertEquals(entities.size(), res.getResultCount());
        Assert.assertNotNull(res.getResult());
        Assert.assertEquals(entities.size(), res.getResult().size());
    }

    @Test
    public void testSearch_pagination() throws Exception {
        PagingInfo pi1 = new PagingInfo(0, 2);
        SearchResult<TestEntity> res1 = idx.search(new String[] { FIELD }, "alice", pi1);
        Assert.assertNotNull(res1);
        Assert.assertEquals(4, res1.getResultCount());
        Assert.assertNotNull(res1.getResult());
        Assert.assertEquals(2, res1.getResult().size());
        Assert.assertEquals("alice", res1.getResult().get(0).getEntity().getValue());
        Assert.assertEquals("alice", res1.getResult().get(0).getValue(FIELD, false));
        Assert.assertEquals("<em>alice</em>", res1.getResult().get(0).getValue(FIELD, true));
        Assert.assertEquals("alice smith", res1.getResult().get(1).getEntity().getValue());
        Assert.assertEquals("alice smith", res1.getResult().get(1).getValue(FIELD, false));
        Assert.assertEquals("<em>alice</em> smith", res1.getResult().get(1).getValue(FIELD, true));

        PagingInfo pi2 = new PagingInfo(1, 2);
        SearchResult<TestEntity> res2 = idx.search(new String[] { FIELD }, "alice", pi2);
        Assert.assertNotNull(res2);
        Assert.assertEquals(4, res2.getResultCount());
        Assert.assertNotNull(res2.getResult());
        Assert.assertEquals(2, res2.getResult().size());
        Assert.assertEquals("alice smith", res2.getResult().get(0).getEntity().getValue());
        Assert.assertEquals("alice smith", res2.getResult().get(0).getValue(FIELD, false));
        Assert.assertEquals("<em>alice</em> smith", res2.getResult().get(0).getValue(FIELD, true));
        Assert.assertEquals("alice in wonderland", res2.getResult().get(1).getEntity().getValue());
        Assert.assertEquals("alice in wonderland", res2.getResult().get(1).getValue(FIELD, false));
        Assert.assertEquals("<em>alice</em> in wonderland", res2.getResult().get(1).getValue(FIELD, true));
    }

    @Test
    public void testUpdate() throws Exception {
        // first ensure there is no hit for "tiffy"
        SearchResult<TestEntity> res1 = idx.search(new String[] { FIELD }, "tiffy", null);
        Assert.assertNotNull(res1);
        Assert.assertEquals(0, res1.getResultCount());
        Assert.assertNotNull(res1.getResult());
        Assert.assertEquals(0, res1.getResult().size());

        // now change entity value from "bob" to "tiffy" and update the index
        entity1.setValue("tiffy"); //$NON-NLS-1$
        idx.update(Collections.singleton(entity1));

        // check there is now a hit for "tiffy"
        SearchResult<TestEntity> res2 = idx.search(new String[] { FIELD }, "tiffy", null);
        Assert.assertNotNull(res2);
        Assert.assertEquals(1, res2.getResultCount());
        Assert.assertNotNull(res2.getResult());
        Assert.assertEquals(1, res2.getResult().size());
        Assert.assertEquals("tiffy", res2.getResult().get(0).getEntity().getValue());
        Assert.assertEquals("tiffy", res2.getResult().get(0).getValue(FIELD, false));
        Assert.assertEquals("<em>tiffy</em>", res2.getResult().get(0).getValue(FIELD, true));

        // verify that "bob" is not found anymore
        SearchResult<TestEntity> res3 = idx.search(new String[] { FIELD }, "bob", null);
        Assert.assertNotNull(res3);
        Assert.assertEquals(0, res3.getResultCount());
        Assert.assertNotNull(res3.getResult());
        Assert.assertEquals(0, res3.getResult().size());
    }

    @Test
    public void testUpdate_deleted() throws Exception {
        // first ensure there is a hit for "bob"
        SearchResult<TestEntity> res1 = idx.search(new String[] { FIELD }, "bob", null);
        Assert.assertNotNull(res1);
        Assert.assertEquals(1, res1.getResultCount());
        Assert.assertNotNull(res1.getResult());
        Assert.assertEquals(1, res1.getResult().size());

        // mark "bob" as deleted and update the index
        entity1.setDeleted(true);
        idx.update(Collections.singleton(entity1));

        // verify that "bob" is not found anymore
        SearchResult<TestEntity> res3 = idx.search(new String[] { FIELD }, "bob", null);
        Assert.assertNotNull(res3);
        Assert.assertEquals(0, res3.getResultCount());
        Assert.assertNotNull(res3.getResult());
        Assert.assertEquals(0, res3.getResult().size());
    }

    @Test
    public void testMoreLikeThis() throws Exception {
        SearchResult<TestEntity> res1 = idx.moreLikeThis(entity2, new String[] { FIELD }, 5);
        Assert.assertNotNull(res1);
        Assert.assertEquals(3, res1.getResultCount());
        Assert.assertEquals(3, res1.getResult().size());
        Assert.assertEquals(entity3.getValue(), res1.getResult().get(0).getEntity().getValue());
        Assert.assertEquals(entity4.getValue(), res1.getResult().get(1).getEntity().getValue());
        Assert.assertEquals(entity5.getValue(), res1.getResult().get(2).getEntity().getValue());

        SearchResult<TestEntity> res2 = idx.moreLikeThis(entity2, new String[] { FIELD }, 2);
        Assert.assertNotNull(res2);
        Assert.assertEquals(3, res2.getResultCount());
        Assert.assertEquals(2, res2.getResult().size());
        Assert.assertEquals(entity3.getValue(), res2.getResult().get(0).getEntity().getValue());
        Assert.assertEquals(entity4.getValue(), res2.getResult().get(1).getEntity().getValue());
    }

    @Test
    public void testFacetedSearch() throws Exception {
        FacetedSearchResult<TestEntity> res = idx.facetedSearch(new String[] { FIELD }, new String[] { FACET },
                "alice", null); //$NON-NLS-1$
        Assert.assertNotNull(res);
        Assert.assertEquals(4, res.getResultCount());
        Assert.assertNotNull(res.getResult());
        Assert.assertEquals(4, res.getResult().size());

        Map<String, Integer> map = res.getFacetInfo().get(FACET);
        Assert.assertNotNull(map);
        Assert.assertEquals(Integer.valueOf(2), map.get("sentence"));
        Assert.assertEquals(Integer.valueOf(1), map.get("fullname"));
        Assert.assertEquals(Integer.valueOf(1), map.get("firstname"));
    }

    @Test
    public void testGetExtendedQuery() throws Exception {
        Assert.assertEquals("(\"foobar\" foobar* foobar~)", LuceneIndex.getExtendedQuery("foobar"));
        Assert.assertEquals("(\"foo\" foo* foo~) (\"bar\" bar* bar~)", LuceneIndex.getExtendedQuery("foo bar"));
        Assert.assertEquals("foobar*", LuceneIndex.getExtendedQuery("foobar*"));
        Assert.assertEquals("foobar~", LuceneIndex.getExtendedQuery("foobar~"));
        Assert.assertEquals("foobar~0.8", LuceneIndex.getExtendedQuery("foobar~0.8"));
        Assert.assertEquals("foo*bar", LuceneIndex.getExtendedQuery("foo*bar"));
        Assert.assertEquals("foo?bar", LuceneIndex.getExtendedQuery("foo?bar"));
        Assert.assertEquals("+foobar", LuceneIndex.getExtendedQuery("+foobar"));
        Assert.assertEquals("-foobar", LuceneIndex.getExtendedQuery("-foobar"));
        Assert.assertEquals("+foobar (\"foobar\" foobar* foobar~)", LuceneIndex.getExtendedQuery("+foobar foobar"));
        Assert.assertEquals("\"foobar\"", LuceneIndex.getExtendedQuery("\"foobar\""));
        Assert.assertEquals("\"foobar\"~10", LuceneIndex.getExtendedQuery("\"foobar\"~10"));
        Assert.assertEquals("\"foo\"^3 \"bar\"", LuceneIndex.getExtendedQuery("\"foo\"^3 \"bar\""));
        Assert.assertEquals("(foobar)", LuceneIndex.getExtendedQuery("(foobar)"));
        Assert.assertEquals("[foobar]", LuceneIndex.getExtendedQuery("[foobar]"));
        Assert.assertEquals("{foobar}", LuceneIndex.getExtendedQuery("{foobar}"));
        Assert.assertEquals(")foobar(", LuceneIndex.getExtendedQuery(")foobar("));
        Assert.assertEquals(")foo bar(", LuceneIndex.getExtendedQuery(")foo bar("));
        Assert.assertEquals("(\"foobar\")", LuceneIndex.getExtendedQuery("(\"foobar\")"));
        Assert.assertEquals("(foobar) (\"foobar\" foobar* foobar~)",
                LuceneIndex.getExtendedQuery("(foobar) foobar"));
        Assert.assertEquals("(\"foo\" foo* foo~) (foo OR bar) (\"bar\" bar* bar~)",
                LuceneIndex.getExtendedQuery("foo (foo OR bar) bar"));
        Assert.assertEquals("\"foobar\" (\"foobar\" foobar* foobar~)",
                LuceneIndex.getExtendedQuery("\"foobar\" foobar"));
        Assert.assertEquals("title:\"foo\" AND text:bar",
                LuceneIndex.getExtendedQuery("title:\"foo\" AND text:bar"));
        // (uppercase) "AND" is a Lucene keyword , but (lowercase) "and" is not !
        Assert.assertEquals("title:\"foo\" (\"and\" and* and~) text:bar",
                LuceneIndex.getExtendedQuery("title:\"foo\" and text:bar"));
        Assert.assertEquals("title:\"foo\" OR (\"foobar\" foobar* foobar~) NOT text:bar",
                LuceneIndex.getExtendedQuery("title:\"foo\" OR foobar NOT text:bar"));
        Assert.assertEquals("title:foo (\"bar\" bar* bar~)",
                LuceneIndex.getExtendedQuery("title:foo bar"));
        Assert.assertEquals("title:[foo TO bar]",
                LuceneIndex.getExtendedQuery("title:[foo TO bar]"));
        Assert.assertEquals("title:(+foo +\"\bar\")",
                LuceneIndex.getExtendedQuery("title:(+foo +\"\bar\")"));
    }

}
