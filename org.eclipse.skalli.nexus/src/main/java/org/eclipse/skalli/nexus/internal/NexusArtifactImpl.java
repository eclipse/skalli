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
package org.eclipse.skalli.nexus.internal;

import java.net.URI;

import org.eclipse.skalli.nexus.NexusArtifact;
import org.eclipse.skalli.nexus.NexusClientException;
import org.w3c.dom.Element;

public class NexusArtifactImpl implements NexusArtifact {

    private URI resourceURI;
    private String groupId;
    private String artifactId;
    private String version;
    private String classifier;
    private String packaging;
    private String extension;
    private String repoId;
    private String contextId;
    private URI pomLink;
    private URI artifactLink;

    public NexusArtifactImpl(Element rootElement) throws NexusClientException {
        if (rootElement == null) {
            throw new IllegalArgumentException("argument 'rootElement' must not be null."); //$NON-NLS-1$
        }
        if (!"artifact".equals(rootElement.getNodeName())) { //$NON-NLS-1$
            throw new IllegalArgumentException("root element must be 'artifact'"); //$NON-NLS-1$
        }

        groupId = NexusResponseParser.getNodeTextContent(rootElement, "groupId"); //$NON-NLS-1$
        artifactId = NexusResponseParser.getNodeTextContent(rootElement, "artifactId"); //$NON-NLS-1$
        version = NexusResponseParser.getNodeTextContent(rootElement, "version"); //$NON-NLS-1$
        classifier = NexusResponseParser.getNodeTextContent(rootElement, "classifier"); //$NON-NLS-1$
        packaging = NexusResponseParser.getNodeTextContent(rootElement, "packaging"); //$NON-NLS-1$
        extension = NexusResponseParser.getNodeTextContent(rootElement, "extension"); //$NON-NLS-1$
        repoId = NexusResponseParser.getNodeTextContent(rootElement, "repoId"); //$NON-NLS-1$
        contextId = NexusResponseParser.getNodeTextContent(rootElement, "contextId"); //$NON-NLS-1$

        resourceURI = NexusResponseParser.getNodeTextContentAsURI(rootElement, "resourceURI"); //$NON-NLS-1$
        pomLink = NexusResponseParser.getNodeTextContentAsURI(rootElement, "pomLink"); //$NON-NLS-1$
        artifactLink = NexusResponseParser.getNodeTextContentAsURI(rootElement, "artifactLink"); //$NON-NLS-1$

    }

    @Override
    public URI getResourceURI() {
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
    public URI getPomLink() {
        return pomLink;
    }

    @Override
    public URI getArtifactLink() {
        return artifactLink;
    }

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
