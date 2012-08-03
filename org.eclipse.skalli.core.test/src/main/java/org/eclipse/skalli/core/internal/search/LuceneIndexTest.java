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
package org.eclipse.skalli.core.internal.search;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

public class LuceneIndexTest {

    public static final String FIELD = "value"; //$NON-NLS-1$
    public static final String FACET = "facet"; //$NON-NLS-1$

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

        entity1 = new TestEntity("bob", "firstname"); //$NON-NLS-1$ //$NON-NLS-2$
        entity2 = new TestEntity("alice", "firstname"); //$NON-NLS-1$ //$NON-NLS-2$
        entity3 = new TestEntity("alice smith", "fullname"); //$NON-NLS-1$ //$NON-NLS-2$
        entity4 = new TestEntity("alice in wonderland", "sentence"); //$NON-NLS-1$ //$NON-NLS-2$
        entity5 = new TestEntity("alice is used in many examples", "sentence"); //$NON-NLS-1$ //$NON-NLS-2$
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
        SearchResult<TestEntity> res = idx.search(new String[] { FIELD }, "bob", null); //$NON-NLS-1$
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.getResultCount());
        Assert.assertNotNull(res.getResult());
        Assert.assertEquals(1, res.getResult().size());
        Assert.assertEquals("bob", res.getResult().get(0).getEntity().getValue()); //$NON-NLS-1$
        Assert.assertEquals("bob", res.getResult().get(0).getValue(FIELD, false)); //$NON-NLS-1$
        Assert.assertEquals("<em>bob</em>", res.getResult().get(0).getValue(FIELD, true)); //$NON-NLS-1$
    }

    @Test
    public void testSearch_specialChars() throws Exception {
        SearchResult<TestEntity> res = idx.search(new String[] { FIELD }, "(", null); //$NON-NLS-1$
        Assert.assertNotNull(res);
        Assert.assertEquals(0, res.getResultCount());
        Assert.assertNotNull(res.getResult());
        Assert.assertEquals(0, res.getResult().size());
    }

    @Test
    public void testSearch_specialChars_asterisk() throws Exception {
        SearchResult<TestEntity> res = idx.search(new String[] { FIELD }, "*", null); //$NON-NLS-1$
        Assert.assertNotNull(res);
        Assert.assertEquals(entities.size(), res.getResultCount());
        Assert.assertNotNull(res.getResult());
        Assert.assertEquals(entities.size(), res.getResult().size());
    }

    @Test
    public void testSearch_pagination() throws Exception {
        PagingInfo pi1 = new PagingInfo(0, 2);
        SearchResult<TestEntity> res1 = idx.search(new String[] { FIELD }, "alice", pi1); //$NON-NLS-1$
        Assert.assertNotNull(res1);
        Assert.assertEquals(4, res1.getResultCount());
        Assert.assertNotNull(res1.getResult());
        Assert.assertEquals(2, res1.getResult().size());
        Assert.assertEquals("alice", res1.getResult().get(0).getEntity().getValue()); //$NON-NLS-1$
        Assert.assertEquals("alice", res1.getResult().get(0).getValue(FIELD, false)); //$NON-NLS-1$
        Assert.assertEquals("<em>alice</em>", res1.getResult().get(0).getValue(FIELD, true)); //$NON-NLS-1$
        Assert.assertEquals("alice smith", res1.getResult().get(1).getEntity().getValue()); //$NON-NLS-1$
        Assert.assertEquals("alice smith", res1.getResult().get(1).getValue(FIELD, false)); //$NON-NLS-1$
        Assert.assertEquals("<em>alice</em> smith", res1.getResult().get(1).getValue(FIELD, true)); //$NON-NLS-1$

        PagingInfo pi2 = new PagingInfo(1, 2);
        SearchResult<TestEntity> res2 = idx.search(new String[] { FIELD }, "alice", pi2); //$NON-NLS-1$
        Assert.assertNotNull(res2);
        Assert.assertEquals(4, res2.getResultCount());
        Assert.assertNotNull(res2.getResult());
        Assert.assertEquals(2, res2.getResult().size());
        Assert.assertEquals("alice smith", res2.getResult().get(0).getEntity().getValue()); //$NON-NLS-1$
        Assert.assertEquals("alice smith", res2.getResult().get(0).getValue(FIELD, false)); //$NON-NLS-1$
        Assert.assertEquals("<em>alice</em> smith", res2.getResult().get(0).getValue(FIELD, true)); //$NON-NLS-1$
        Assert.assertEquals("alice in wonderland", res2.getResult().get(1).getEntity().getValue()); //$NON-NLS-1$
        Assert.assertEquals("alice in wonderland", res2.getResult().get(1).getValue(FIELD, false)); //$NON-NLS-1$
        Assert.assertEquals("<em>alice</em> in wonderland", res2.getResult().get(1).getValue(FIELD, true)); //$NON-NLS-1$
    }

    @Test
    public void testUpdate() throws Exception {
        // first ensure there is no hit for "tiffy"
        SearchResult<TestEntity> res1 = idx.search(new String[] { FIELD }, "tiffy", null); //$NON-NLS-1$
        Assert.assertNotNull(res1);
        Assert.assertEquals(0, res1.getResultCount());
        Assert.assertNotNull(res1.getResult());
        Assert.assertEquals(0, res1.getResult().size());

        // now change entity value from "bob" to "tiffy" and update the index
        entity1.setValue("tiffy"); //$NON-NLS-1$
        idx.update(Collections.singleton(entity1));

        // check there is now a hit for "tiffy"
        SearchResult<TestEntity> res2 = idx.search(new String[] { FIELD }, "tiffy", null); //$NON-NLS-1$
        Assert.assertNotNull(res2);
        Assert.assertEquals(1, res2.getResultCount());
        Assert.assertNotNull(res2.getResult());
        Assert.assertEquals(1, res2.getResult().size());
        Assert.assertEquals("tiffy", res2.getResult().get(0).getEntity().getValue()); //$NON-NLS-1$
        Assert.assertEquals("tiffy", res2.getResult().get(0).getValue(FIELD, false)); //$NON-NLS-1$
        Assert.assertEquals("<em>tiffy</em>", res2.getResult().get(0).getValue(FIELD, true)); //$NON-NLS-1$

        // verify that "bob" is not found anymore
        SearchResult<TestEntity> res3 = idx.search(new String[] { FIELD }, "bob", null); //$NON-NLS-1$
        Assert.assertNotNull(res3);
        Assert.assertEquals(0, res3.getResultCount());
        Assert.assertNotNull(res3.getResult());
        Assert.assertEquals(0, res3.getResult().size());
    }

    @Test
    public void testUpdate_deleted() throws Exception {
        // first ensure there is a hit for "bob"
        SearchResult<TestEntity> res1 = idx.search(new String[] { FIELD }, "bob", null); //$NON-NLS-1$
        Assert.assertNotNull(res1);
        Assert.assertEquals(1, res1.getResultCount());
        Assert.assertNotNull(res1.getResult());
        Assert.assertEquals(1, res1.getResult().size());

        // mark "bob" as deleted and update the index
        entity1.setDeleted(true);
        idx.update(Collections.singleton(entity1));

        // verify that "bob" is not found anymore
        SearchResult<TestEntity> res3 = idx.search(new String[] { FIELD }, "bob", null); //$NON-NLS-1$
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
        Assert.assertEquals(Integer.valueOf(2), map.get("sentence")); //$NON-NLS-1$
        Assert.assertEquals(Integer.valueOf(1), map.get("fullname")); //$NON-NLS-1$
        Assert.assertEquals(Integer.valueOf(1), map.get("firstname")); //$NON-NLS-1$
    }

}
