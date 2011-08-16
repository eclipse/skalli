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
package org.eclipse.skalli.nexus.internal;

import java.net.URL;

import org.eclipse.skalli.nexus.NexusArtifact;
import org.eclipse.skalli.nexus.NexusClientException;
import org.w3c.dom.Element;

public class NexusArtifactImpl implements NexusArtifact {

    private URL resourceURI;
    private String groupId;
    private String artifactId;
    private String version;
    private String classifier;
    private String packaging;
    private String extension;
    private String repoId;
    private String contextId;
    private URL pomLink;
    private URL artifactLink;

    public NexusArtifactImpl(Element rootElement) throws NexusClientException {

        if (rootElement == null) {
            throw new IllegalArgumentException("Parameter rootElement must not be null.");
        }

        if (!"artifact".equals(rootElement.getNodeName())) {
            throw new IllegalArgumentException("rootElement.getNodeName() must be 'artifact'");
        }

        groupId = NexusResponseParser.getNodeTextContent(rootElement, "groupId");
        artifactId = NexusResponseParser.getNodeTextContent(rootElement, "artifactId");
        version = NexusResponseParser.getNodeTextContent(rootElement, "version");
        classifier = NexusResponseParser.getNodeTextContent(rootElement, "classifier");
        packaging = NexusResponseParser.getNodeTextContent(rootElement, "packaging");
        extension = NexusResponseParser.getNodeTextContent(rootElement, "extension");
        repoId = NexusResponseParser.getNodeTextContent(rootElement, "repoId");
        contextId = NexusResponseParser.getNodeTextContent(rootElement, "contextId");

        resourceURI = NexusResponseParser.getNodeTextContentAsURL(rootElement, "resourceURI");
        pomLink = NexusResponseParser.getNodeTextContentAsURL(rootElement, "pomLink");
        artifactLink = NexusResponseParser.getNodeTextContentAsURL(rootElement, "artifactLink");

    }

    @Override
    public URL getResourceURI() {
        return resourceURI;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public String getPackaging() {
        return packaging;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public String getRepoId() {
        return repoId;
    }

    @Override
    public String getContextId() {
        return contextId;
    }

    @Override
    public URL getPomLink() {
        return pomLink;
    }

    @Override
    public URL getArtifactLink() {
        return artifactLink;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
        result = prime * result + ((artifactLink == null) ? 0 : artifactLink.hashCode());
        result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
        result = prime * result + ((contextId == null) ? 0 : contextId.hashCode());
        result = prime * result + ((extension == null) ? 0 : extension.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((packaging == null) ? 0 : packaging.hashCode());
        result = prime * result + ((pomLink == null) ? 0 : pomLink.hashCode());
        result = prime * result + ((repoId == null) ? 0 : repoId.hashCode());
        result = prime * result + ((resourceURI == null) ? 0 : resourceURI.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NexusArtifactImpl other = (NexusArtifactImpl) obj;
        if (artifactId == null) {
            if (other.artifactId != null) {
                return false;
            }
        } else if (!artifactId.equals(other.artifactId)) {
            return false;
        }
        if (artifactLink == null) {
            if (other.artifactLink != null) {
                return false;
            }
        } else if (!artifactLink.equals(other.artifactLink)) {
            return false;
        }
        if (classifier == null) {
            if (other.classifier != null) {
                return false;
            }
        } else if (!classifier.equals(other.classifier)) {
            return false;
        }
        if (contextId == null) {
            if (other.contextId != null) {
                return false;
            }
        } else if (!contextId.equals(other.contextId)) {
            return false;
        }
        if (extension == null) {
            if (other.extension != null) {
                return false;
            }
        } else if (!extension.equals(other.extension)) {
            return false;
        }
        if (groupId == null) {
            if (other.groupId != null) {
                return false;
            }
        } else if (!groupId.equals(other.groupId)) {
            return false;
        }
        if (packaging == null) {
            if (other.packaging != null) {
                return false;
            }
        } else if (!packaging.equals(other.packaging)) {
            return false;
        }
        if (pomLink == null) {
            if (other.pomLink != null) {
                return false;
            }
        } else if (!pomLink.equals(other.pomLink)) {
            return false;
        }
        if (repoId == null) {
            if (other.repoId != null) {
                return false;
            }
        } else if (!repoId.equals(other.repoId)) {
            return false;
        }
        if (resourceURI == null) {
            if (other.resourceURI != null) {
                return false;
            }
        } else if (!resourceURI.equals(other.resourceURI)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

}
