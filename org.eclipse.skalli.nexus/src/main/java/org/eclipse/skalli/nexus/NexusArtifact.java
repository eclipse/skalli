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
package org.eclipse.skalli.nexus;

import java.net.URI;

/**
 * Interface that represents an artifact in a Nexus search response.
 *
 * @see https://repository.sonatype.org/nexus-indexer-lucene-plugin/default/docs/data_ns0.html#nexus-artifact
 */
public interface NexusArtifact {

    /**
     * Returns an URL that can be used to retrieve the artifact.
     */
    public URI getResourceURI();

    /**
     * Returns the group id of the artifact.
     */
    public String getGroupId();

    /**
     * Returns the artifact id of the artifact.
     */
    public String getArtifactId();

    /**
     * Returns the version of the artifact.
     */
    public String getVersion();

    /**
     * Returns the classifier of the artifact, or <code>null</code>
     * if the artifact has no classifier.
     */
    public String getClassifier();

    /**
     * Returns the packaging type of the artifact.
     */
    public String getPackaging();

    /**
     * Returns the (file) extension of the artifact.
     */
    public String getExtension();

    /**
     * Returns the repository id where the artifact is stored.
     */
    public String getRepoId();

    /**
     * Returns the indexing context where the artifact is indexed.
     */
    public String getContextId();

    /**
     * Returns an URL that can be used to retrieve the POM of the artifact.
     */
    public URI getPomLink();

    /**
     * Returns an URL that can be used to retrieve the file of the artifact.
     */
    public URI getArtifactLink();
}
