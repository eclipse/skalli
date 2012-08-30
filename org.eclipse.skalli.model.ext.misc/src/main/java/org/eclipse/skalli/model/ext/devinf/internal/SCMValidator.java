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
package org.eclipse.skalli.model.ext.devinf.internal;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.skalli.commons.HtmlUtils;
import org.eclipse.skalli.model.Issue;
import org.eclipse.skalli.model.Issuer;
import org.eclipse.skalli.model.Severity;
import org.eclipse.skalli.model.ext.devinf.DevInfProjectExt;
import org.eclipse.skalli.services.extension.PropertyValidator;

public class SCMValidator implements PropertyValidator, Issuer {

    private static final Pattern PATTERN_PERFORCE = Pattern
            .compile("^scm:perforce:perforce(\\d+)(\\.wdf\\.sap\\.corp)?:\\1://.*/$"); //$NON-NLS-1$
    private static final Pattern PATTERN_GIT = Pattern.compile("^scm:git:\\w*://.*\\.git$"); //$NON-NLS-1$
    private static final Pattern PATTERN_SVN = Pattern.compile("^scm:svn:\\w*://.*\\$"); //$NON-NLS-1$

    private String caption;

    public SCMValidator(String caption) {
        this.caption = caption;
    }

    /**
     * Checks whether the given value is a valid SCM location or a collection of SCM locations.
     * Currently supports Perforce, Git and Subversion SCM location schemas.
     */
    @Override
    public SortedSet<Issue> validate(UUID entity, Object value, Severity minSeverity) {
        TreeSet<Issue> issues = new TreeSet<Issue>();
        if (value != null) {
            if (value instanceof Collection) {
                int item = 0;
                for (Object entry : (Collection<?>) value) {
                    validate(entity, entry.toString(), item, issues);
                    ++item;
                }
            } else {
                validate(entity, value.toString(), 0, issues);
            }
        }
        return issues;
    }

    private void validate(UUID entity, String scmLocation, int item, TreeSet<Issue> issues) {
        if (StringUtils.isNotBlank(scmLocation)) {
            if (scmLocation.startsWith("scm:perforce:")) { //$NON-NLS-1$
                if (!PATTERN_PERFORCE.matcher(scmLocation).matches()) {
                    issues.add(new Issue(
                            Severity.ERROR,
                            SCMValidator.class,
                            entity,
                            DevInfProjectExt.class,
                            DevInfProjectExt.PROPERTY_SCM_LOCATIONS,
                            item,
                            HtmlUtils.formatEscaped(
                                    "''{0}'' is not a valid value for {1}. For Perforce repositories the value "  +
                                    "must match the regular expression {2}. Ensure that a port is specified and that the URL ends with a slash.",
                                    scmLocation, caption, PATTERN_PERFORCE.pattern())
                            ));
                }
            } else if (scmLocation.startsWith("scm:git:")) {//$NON-NLS-1$
                if (!PATTERN_GIT.matcher(scmLocation).matches()) {
                    issues.add(new Issue(Severity.ERROR, SCMValidator.class, entity,
                            DevInfProjectExt.class, DevInfProjectExt.PROPERTY_SCM_LOCATIONS, item,
                            HtmlUtils.formatEscaped(
                                    "''{0}'' is not a valid value for {1}. For Git repositories the value " +
                                            "must match the regular expression {2}.",
                                    scmLocation, caption, PATTERN_GIT.pattern())
                            ));
                }
            }
            else if (scmLocation.startsWith("scm:svn:")) {//$NON-NLS-1$
                if (!PATTERN_SVN.matcher(scmLocation).matches()) {
                    issues.add(new Issue(Severity.ERROR, SCMValidator.class, entity,
                            DevInfProjectExt.class, DevInfProjectExt.PROPERTY_SCM_LOCATIONS, item,
                            HtmlUtils.formatEscaped(
                                    "''{0}'' is not a valid value for {1}. For Subversion repositories the value " +
                                            "must match the regular expression {2}.",
                                    scmLocation, caption, PATTERN_SVN.pattern())
                            ));
                }
            }
            else {
                issues.add(new Issue(Severity.FATAL, SCMValidator.class, entity,
                        DevInfProjectExt.class, DevInfProjectExt.PROPERTY_SCM_LOCATIONS, item,
                        HtmlUtils.formatEscaped(
                                "''{0}'' is not a valid value for {1}. It must start with 'scm:&lt;repotype&gt;:' "
                                        + "where &lt;repotype&gt; is one of git, perforce, or svn", scmLocation,
                                caption)
                        ));
            }
        }
    }
}
