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
package org.eclipse.skalli.core.internal.favorites;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ValidationException;
import org.eclipse.skalli.services.entity.EntityServiceBase;
import org.eclipse.skalli.services.extension.DataMigration;
import org.eclipse.skalli.services.favorites.Favorites;
import org.eclipse.skalli.services.favorites.FavoritesService;
import org.eclipse.skalli.services.persistence.EntityFilter;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FavoritesServiceImpl extends EntityServiceBase<Favorites> implements FavoritesService {

    private static final Logger LOG = LoggerFactory.getLogger(FavoritesServiceImpl.class);

    private static final int CURRENT_MODEL_VERISON = 20;

    @Override
    protected void activate(ComponentContext context) {
        LOG.info(MessageFormat.format("[FavoritesService] {0} : activated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    protected void deactivate(ComponentContext context) {
        LOG.info(MessageFormat.format("[FavoritesService] {0} : deactivated",
                (String) context.getProperties().get(ComponentConstants.COMPONENT_NAME)));
    }

    @Override
    public Class<Favorites> getEntityClass() {
        return Favorites.class;
    }


    @Override
    public int getModelVersion() {
        return CURRENT_MODEL_VERISON;
    }

    @Override
    public Map<String, Class<?>> getAliases() {
        Map<String, Class<?>> aliases = super.getAliases();
        aliases.put("entity-favorites", Favorites.class); //$NON-NLS-1$
        return aliases;
    }

    @Override
    public Set<DataMigration> getMigrations() {
        Set<DataMigration> migrations = super.getMigrations();
        migrations.add(new FavoritesDataMigration19());
        return migrations;
    }

    @Override
    public Favorites getFavorites(String userId) {
        Favorites favorites = getPersistenceService().getEntity(Favorites.class, new FavoritesFilter(userId));
        return favorites != null ? favorites : new Favorites(userId);
    }

    @Override
    public void addFavorite(String userId, UUID project) throws ValidationException {
        Favorites favorites = getFavorites(userId);
        favorites.addProject(project);
        persist(favorites, userId);
    }

    @Override
    public void removeFavorite(String userId, UUID project) throws ValidationException {
        Favorites favorites = getFavorites(userId);
        favorites.removeProject(project);
        persist(favorites, userId);
    }

    protected static class FavoritesFilter implements EntityFilter<Favorites> {
        private String userId;

        public FavoritesFilter(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }

        @Override
        public boolean accept(Class<Favorites> entityClass, Favorites entity) {
            return entity.getUserId().equals(userId);
        }
    }

    @Override
    protected void validateEntity(Favorites entity) throws ValidationException {
        SortedSet<Issue> issues = validate(entity, Severity.FATAL);
        if (issues.size() > 0) {
            throw new ValidationException("Favorites could not be saved due to the following reasons:", issues);
        }
    }

    @Override
    protected SortedSet<Issue> validateEntity(Favorites entity, Severity minSeverity) {
        return new TreeSet<Issue>();
    }
}
