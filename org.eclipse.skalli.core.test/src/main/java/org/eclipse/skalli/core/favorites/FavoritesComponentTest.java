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
package org.eclipse.skalli.core.favorites;

import static org.easymock.EasyMock.*;

import org.easymock.IAnswer;
import org.eclipse.skalli.core.favorites.FavoritesComponent;
import org.eclipse.skalli.core.project.ProjectComponentTest;
import org.eclipse.skalli.services.favorites.Favorites;
import org.eclipse.skalli.services.favorites.FavoritesService;
import org.eclipse.skalli.services.persistence.PersistenceService;
import org.eclipse.skalli.testutil.AssertUtils;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;

public class FavoritesComponentTest extends ProjectComponentTest {

    private static class TestFavoritesServiceImpl extends FavoritesComponent {
        private final PersistenceService persistenceService;

        public TestFavoritesServiceImpl(PersistenceService persistenceService) {
            this.persistenceService = persistenceService;
        }

        @Override
        protected PersistenceService getPersistenceService() {
            return persistenceService;
        }
    }

    private static final String USERID1 = "Homer";
    private static final String USERID2 = "Marge";

    private Favorites favorites1;
    private Favorites favorites2;
    private FavoritesService fs;

    @Override
    @Before
    public void setup() throws BundleException {
        super.setup();
        favorites1 = new Favorites(USERID1);
        favorites1.addProject(uuids[1]);
        favorites1.addProject(uuids[2]);
        favorites1.addProject(uuids[3]);
        favorites2 = new Favorites(USERID2);
        favorites2.addProject(uuids[2]);
        favorites2.addProject(uuids[5]); // reference to a deleted project!
        fs = new TestFavoritesServiceImpl(mockIPS);
    }

    @Override
    protected void recordMocks() {
        super.recordMocks();

        mockIPS.getEntity(eq(Favorites.class), isA(FavoritesComponent.FavoritesFilter.class));
        expectLastCall().andAnswer(new IAnswer<Favorites>() {
            @Override
            public Favorites answer() throws Throwable {
                Object[] args = getCurrentArguments();
                String userId = ((FavoritesComponent.FavoritesFilter) args[1]).getUserId();
                if (USERID1.equals(userId)) {
                    return favorites1;
                }
                if (USERID2.equals(userId)) {
                    return favorites2;
                }
                return null;
            }
        }).anyTimes();
    }

    @Test
    public void testGetFavorites() {
        Favorites fav1 = fs.getFavorites(USERID1);
        AssertUtils.assertEquals("favorites of " + USERID1, favorites1.getProjects(), fav1.getProjects());

        verify(mocks);
    }
}
