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
package org.eclipse.skalli.model.ext.linkgroups;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.skalli.commons.Link;
import org.eclipse.skalli.model.ExtensionEntityBase;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.services.extension.validators.HostReachableValidator;

public class LinkGroupValidator extends HostReachableValidator {

    public LinkGroupValidator(Class<? extends ExtensionEntityBase> extension, String propertyId) {
        super(extension, propertyId);
    }

    @Override
    public SortedSet<Issue> validate(UUID entityId, Object value, Severity minSeverity) {
        final SortedSet<Issue> issues = new TreeSet<Issue>();

        // Do not participate in checks with Severity.FATAL & ignore null
        if (minSeverity.equals(Severity.FATAL) || value == null) {
            return issues;
        }

        if (value instanceof Collection) {
            int item = 0;
            for (Object collectionEntry : (Collection<?>) value) {
                if (collectionEntry == null || !(collectionEntry instanceof LinkGroup)) {
                    continue;
                }
                for (Object groupEntry : LinkGroup.class.cast(collectionEntry).getItems()) {
                    if (groupEntry == null || !(groupEntry instanceof Link)) {
                        continue;
                    }
                    validate(issues, entityId, groupEntry, minSeverity, item);
                    ++item;
                }
            }
        }

        return issues;
    }

}
