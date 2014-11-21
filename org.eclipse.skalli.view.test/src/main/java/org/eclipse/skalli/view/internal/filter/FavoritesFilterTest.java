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
package org.eclipse.skalli.view.internal.filter;

import static org.easymock.EasyMock.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.skalli.model.User;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.Services;
import org.eclipse.skalli.services.favorites.Favorites;
import org.eclipse.skalli.services.favorites.FavoritesService;
import org.eclipse.skalli.testutil.BundleManager;
import org.eclipse.skalli.view.Consts;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("nls")
public class FavoritesFilterTest {

    private static final String USERID = "Homer";
    private static final User USER = new User(USERID, USERID, "Simpson", "homer@example.org");

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private FilterChain mockChain;
    private Object[] mocks;

    private Favorites favorites;

    @Before
    public void setup() throws Exception {
        BundleManager.startBundles();

        persistFavorites();

        mockRequest = createMock(HttpServletRequest.class);
        mockResponse = createMock(HttpServletResponse.class);
        mockChain = createMock(FilterChain.class);
        mocks = new Object[] { mockRequest, mockResponse, mockChain };

        reset(mocks);
        recordMocks();
        replay(mocks);
    }

    private void persistFavorites() throws ValidationException {
        favorites = new Favorites(USERID);
        favorites.addProject(UUID.fromString("0a9dc902-5350-4744-a30e-f8aad616a943"));
        favorites.addProject(UUID.fromString("234d9ad0-1bf2-4cee-b10f-6fc6bee165c3"));
        favorites.addProject(UUID.fromString("37c64257-9355-40b5-8f3c-87513a03d197"));
        favorites.addProject(UUID.fromString("5bf88924-9469-4980-8834-199ac4f20cb8"));
        favorites.addProject(UUID.fromString("8b53fbd7-dae2-43e7-8208-2b45e9ec718a"));
        FavoritesService favoritesService = Services.getRequiredService(FavoritesService.class);
        favoritesService.persist(favorites, USERID);
    }

    private void recordMocks() throws Exception {
        expect(mockRequest.getAttribute(Consts.ATTRIBUTE_USER)).andReturn(USER);
        expect(mockRequest.getParameter(Consts.PARAM_START)).andReturn("0");
        expect(mockRequest.getParameter(Consts.PARAM_COUNT)).andReturn("");
        mockRequest.setAttribute(eq("title"), eq("Favorites for " + USER.getDisplayName()));
        mockRequest.setAttribute(eq("projects"), isA(List.class));
        mockRequest.setAttribute(eq("natures"), isA(Map.class));
        mockRequest.setAttribute(eq("parents"), isA(Map.class));
        mockRequest.setAttribute(eq("parentChains"), isA(Map.class));
        mockRequest.setAttribute(eq("subprojects"), isA(Map.class));
        mockRequest.setAttribute(eq("favorites"), eq(favorites.asMap()));
        mockRequest.setAttribute(eq("duration"), eq(0L));
        mockRequest.setAttribute(eq("start"), eq(0));
        mockRequest.setAttribute(eq("viewSize"), eq(10));
        mockRequest.setAttribute(eq("resultSize"), eq(5));
        mockRequest.setAttribute(eq("currentPage"), eq(1));
        mockRequest.setAttribute(eq("pages"), eq(1));
        mockRequest.setAttribute(eq("user"), eq(USER));

        mockChain.doFilter(eq(mockRequest), eq(mockResponse));
    }

    private class FavoritesFilterWrapper extends FavoritesFilter {
        @Override
        protected User getUser(HttpServletRequest request) {
            return USER;
        }
    }

    // ignore this test at the moment, refactoring necessary:
    // - test doesn't rely on projects located in this bundle when running maven build
    // - persisted favorites are not cleaned up after test execution
    @Ignore
    @Test
    public void testDoFilter() throws Exception {
        FavoritesFilterWrapper filter = new FavoritesFilterWrapper();
        filter.doFilter(mockRequest, mockResponse, mockChain);
        verify(mocks);
    }

}
