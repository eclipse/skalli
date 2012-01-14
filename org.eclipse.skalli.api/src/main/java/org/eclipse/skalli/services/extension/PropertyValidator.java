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
package org.eclipse.skalli.services.extension;

import java.util.SortedSet;
import java.util.UUID;

import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;

/**
 * Service interface for validators of properties of model extensions (and projects).
 * <p>
 * Property validators perform checks on individual properties of extensions used, amongst others,
 * for validation of input elements in the UI and to check entities to be persisted.
 * <p>
 * Property validators <em>must</em> be stateless.
 */
public interface PropertyValidator {

    /**
     * Checks whether the given value causes validation issues  equals to or more serious
     * than the given severity. The result set is sorted according to {@link Issue#compareTo(Issue)}.
     * <p>
     * A property validator <em>should</em> perform only those checks that are required or appropriate for
     * the given {@link Severity severity} level. The persistence service calls all property validators with
     * {@link Severity#FATAL} that are relevant for the properties of a certain entity to determine
     * whether that entity can be
     * {@link org.eclipse.skalli.services.projects.EntityService#persist(T,String) persisted}.<br>
     * <em>An entity with fatal validation issues cannot be persisted unless these issues are resolved!</em>
     * <p>
     * Furthermore, property validators searching for {@link Severity#FATAL fatal} issues
     * <em>should not</em> perform time-consuming or potentially blocking operations,
     * such as contacting remote servers. Property validators are used, amongst others, in the UI to
     * check data entered by a user for fatal issues, e.g. missing or malformed URLs, and therefore
     * might be called frequently. In order to perserve a smooth user experience property
     * validators for fatal issues should perform the smallest possible set of checks only!
     * <p>
     * Implementations of this method must be thread-safe.
     *
     * @param entity  the unique identifier of the entity to validate.
     * @param value  the property value to validate.
     * @param minSeverity  the minimal severity of issues to report.
     * @return  a sorted set of validation issues, or an empty set.
     */
    public SortedSet<Issue> validate(UUID entity, Object value, Severity minSeverity);
}
