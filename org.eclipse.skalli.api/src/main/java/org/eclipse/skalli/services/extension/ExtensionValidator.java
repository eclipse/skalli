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
package org.eclipse.skalli.services.extension;

import java.util.SortedSet;
import java.util.UUID;

import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;

/**
 * Service interface for validators of model extensions (and projects).
 * <p>
 * Extension validators perform checks on whole extension instances, e.g. for
 * execution by a batch job. In contrast to {@link PropertyValidator property validators} that can
 * only check the value of a certain property, extension validators can perform consistency checks
 * on multiple properties simultaneously.
 * <p>
 * Extension validators <em>must</em> be stateless.
 */
public interface ExtensionValidator<T extends ExtensionEntityBase> {

    /**
     * Returns the class of the model extension supported by this validator.
     */
    public Class<T> getExtensionClass();

    /**
     * Checks whether the given model extension instance has validation issues equal to or more
     * serious than the given severity. The result set is sorted according to {@link Issue#compareTo(Issue)}.
     * <p>
     * An extension validator <em>should</em> perform only those checks that are required or appropriate for
     * the given {@link Severity severity} level. The persistence service calls all extension validators
     * assigned to a model extension with {@link Severity#FATAL} when an entity having such an extension
     * is {@link org.eclipse.skalli.services.projects.EntityService#persist(T,String) persisted}.<br>
     * <em>An entity with fatal validation issues cannot be persisted unless these issues are resolved!</em>
     * <p>
     * Furthermore, extension validators searching for {@link Severity#FATAL fatal} issues
     * <em>should not</em> perform time-consuming or potentially blocking operations,
     * such as contacting remote servers. Otherwise the validation might block the UI potentially
     * for a long period of time.
     * <p>
     * Implementations of this method must be thread-safe.
     *
     * @param entity  the unique identifier of the entity to validate.
     * @param extension  the model extension instance to check.
     * @param minSeverity  the minimal severity of issues to report.
     * @return  a sorted set of validation issues, or an empty set.
     */
    public SortedSet<Issue> validate(UUID entity, ExtensionEntityBase extension, Severity minSeverity);
}
