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
package org.eclipse.skalli.model.ext.maven.internal.recommendedupdatesites;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.UUID;

import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.model.ext.maven.recommendedupdatesites.RecommendedUpdateSites;
import org.eclipse.skalli.model.ext.maven.recommendedupdatesites.UpdateSite;
import org.eclipse.skalli.services.persistence.PersistenceService;
import org.eclipse.skalli.testutil.HashMapStorageService;
import org.eclipse.skalli.testutil.TestUUIDs;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RecommendedUpdateSitesServiceImplTest {

    private static final String TEST_USER_ID = "User12345";

    private UUID projectUUid = TestUUIDs.TEST_UUIDS[1];

    private RecommendedUpdateSitesServiceImpl recommendedUpdateSitesService;

    HashMapStorageService hashMapStorageService = new HashMapStorageService();

    private UpdateSite createUpdatesite(String identifier) {
        UpdateSite us = new UpdateSite();
        us.setProjectUUID(projectUUid);
        us.setGroupId("groupId" + identifier);
        us.setArtifactId("artifactId" + identifier);
        us.setName("name" + identifier);
        us.setDescription("description" + identifier);
        return us;
    }

    @Before
    public void setup() {
        recommendedUpdateSitesService = new RecommendedUpdateSitesServiceImpl();
        recommendedUpdateSitesService.bindPersistenceService(getPersistenceServiceMock());
    }

    private PersistenceService getPersistenceServiceMock() {
        return null;
    }

    @Ignore("we cant initialize a getPersistenceServiceMock")
    @Test
    public void testPersistAndLoad() throws ValidationException {

        RecommendedUpdateSites testUpdateSites = new RecommendedUpdateSites();
        testUpdateSites.setUserId("owner123");
        testUpdateSites.setDescription("My Test recommendedUpdatesites");
        testUpdateSites.setUuid(TestUUIDs.TEST_UUIDS[0]);
        testUpdateSites.setId("updateSiteId1234");

        UpdateSite updateSite1 = createUpdatesite("1");
        UpdateSite updateSite2 = createUpdatesite("2");

        testUpdateSites.getUpdateSites().add(updateSite1);
        testUpdateSites.getUpdateSites().add(updateSite2);

        recommendedUpdateSitesService.persist(testUpdateSites, TEST_USER_ID);

        RecommendedUpdateSites loadedUpdateSites = recommendedUpdateSitesService.loadEntity(
                RecommendedUpdateSites.class, testUpdateSites.getUuid());
        assertThat(loadedUpdateSites.getLastModifiedBy(), is(TEST_USER_ID));
        assertNotNull(loadedUpdateSites.getLastModified());
        assertSame(loadedUpdateSites, testUpdateSites);

    }

    private void assertSame(RecommendedUpdateSites actual, RecommendedUpdateSites expected) {
        assertThat(actual.getUuid(), is(expected.getUuid()));
        assertThat(actual.getId(), is(expected.getId()));
        assertThat(actual.getDescription(), is(expected.getDescription()));
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getParentEntityId(), is(expected.getParentEntityId()));
        assertThat(actual.getUserId(), is(expected.getUserId()));
        assertThat(actual.getUpdateSites().size(), is(expected.getUpdateSites().size()));
        for (UpdateSite expectedUpdateSite : actual.getUpdateSites()) {
            assertThat(expected.getUpdateSites(), hasItem(expectedUpdateSite));
        }
    }

    @Ignore("we cant initialize a getPersistenceServiceMock")
    @Test
    public void testGetRecommendedUpdateSites() throws ValidationException
    {
        final String userId = "owner1234";
        final String updateSiteId = "Id1234";

        RecommendedUpdateSites testUpdateSites = new RecommendedUpdateSites();
        testUpdateSites.setUserId(userId);
        testUpdateSites.setDescription("My Test recommendedUpdatesites");
        testUpdateSites.setUuid(TestUUIDs.TEST_UUIDS[0]);
        testUpdateSites.setId(updateSiteId);

        UpdateSite updateSite1 = createUpdatesite("testGetRecommended");
        UpdateSite updateSite2 = createUpdatesite("2");

        testUpdateSites.getUpdateSites().add(updateSite1);
        testUpdateSites.getUpdateSites().add(updateSite2);

        recommendedUpdateSitesService.persist(testUpdateSites, TEST_USER_ID);
        RecommendedUpdateSites foundSite = recommendedUpdateSitesService.getRecommendedUpdateSites(userId, updateSiteId);
        assertSame(foundSite,testUpdateSites);
    }
}
