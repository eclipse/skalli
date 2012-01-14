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
package org.eclipse.skalli.model.ext.misc.internal;

import org.eclipse.skalli.model.ext.misc.ProjectRating;
import org.eclipse.skalli.model.ext.misc.ReviewEntry;
import org.eclipse.skalli.model.ext.misc.ReviewProjectExt;
import org.eclipse.skalli.services.extension.rest.RestConverterBase;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

class ReviewConverter extends RestConverterBase<ReviewProjectExt> {

    public static final String API_VERSION = "1.0"; //$NON-NLS-1$
    public static final String NAMESPACE = "http://www.eclipse.org/skalli/2010/API/Extension-Review"; //$NON-NLS-1$

    public ReviewConverter(String host) {
        super(ReviewProjectExt.class, "reviews", host); //$NON-NLS-1$
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        ReviewProjectExt ext = (ReviewProjectExt) source;
        if (ext.getReviews().size() > 0) {
            for (ReviewEntry entry : ext.getReviews()) {
                writeNode(writer, entry);
            }
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return iterateNodes(null, reader, context);
    }

    private void writeNode(HierarchicalStreamWriter writer, ReviewEntry entry) {
        writer.startNode("review"); //$NON-NLS-1$
        writeNode(writer, "voter", entry.getVoter()); //$NON-NLS-1$
        writeNode(writer, "comment", entry.getComment()); //$NON-NLS-1$
        writeNode(writer, "timestamp", entry.getTimestamp()); //$NON-NLS-1$
        writeNode(writer, "rating", entry.getRating().toString()); //$NON-NLS-1$
        writer.endNode();
    }

    private ReviewProjectExt iterateNodes(ReviewProjectExt ext, HierarchicalStreamReader reader,
            UnmarshallingContext context) {
        if (ext == null) {
            ext = new ReviewProjectExt();
        }

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            String field = reader.getNodeName();
            String value = reader.getValue();

            ReviewEntry entry = null;
            if ("reviews".equals(field)) { //$NON-NLS-1$
                iterateNodes(ext, reader, context);
            } else if ("review".equals(field)) { //$NON-NLS-1$
                iterateNodes(ext, reader, context);
            } else {
                if (entry == null) {
                    entry = new ReviewEntry();
                    ext.addReview(entry);
                }
                if ("voter".equals(field)) { //$NON-NLS-1$
                    entry.setVoter(value);
                } else if ("comment".equals(field)) { //$NON-NLS-1$
                    entry.setComment(value);
                } else if ("timestamp".equals(field)) { //$NON-NLS-1$
                    entry.setTimestamp(Long.valueOf(value));
                } else if ("rating".equals(field)) { //$NON-NLS-1$
                    entry.setRating(ProjectRating.valueOf(value));
                }
            }
            reader.moveUp();
        }
        return ext;
    }

    @Override
    public String getApiVersion() {
        return API_VERSION;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getXsdFileName() {
        return "extension-review.xsd";
    }
}
