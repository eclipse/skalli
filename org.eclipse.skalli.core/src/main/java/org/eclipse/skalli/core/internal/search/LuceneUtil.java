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
package org.eclipse.skalli.core.internal.search;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.eclipse.skalli.services.search.IndexEntry;

public class LuceneUtil {

    static final String FRAGMENTS_SEPARATOR = "<em>...</em>"; //$NON-NLS-1$
    static final int COMPARE_LENGTH = 16;

    static Field.Index resolveIndexed(IndexEntry.Indexed indexed) {
        switch (indexed) {
        case NO:
            return Field.Index.NO;
        case UN_TOKENIZED:
            return Field.Index.NOT_ANALYZED;
        case TOKENIZED:
            return Field.Index.ANALYZED;
        default:
            return Field.Index.NO;
        }
    }

    static Field.Store resolveStored(IndexEntry.Stored stored) {
        switch (stored) {
        case NO:
            return Field.Store.NO;
        case YES:
            return Field.Store.YES;
        default:
            return Field.Store.NO;
        }
    }

    static Document fieldsToDocument(List<IndexEntry> fields) {
        Document ret = new Document();
        for (IndexEntry entry : fields) {
            Field.Index indexed = resolveIndexed(entry.getIndexed());
            Field.Store stored = resolveStored(entry.getStored());
            Field field = new Field(entry.getFieldName(), entry.getValue(), stored, indexed);
            ret.add(field);
        }
        return ret;
    }

    @SuppressWarnings("nls")
    static String withEllipsis(String[] fragments, String text) {
        StringBuilder sb = new StringBuilder();
        int len = text.length();
        String expectedPrefix = text.substring(0, Math.min(len, COMPARE_LENGTH));
        String expectedSuffix = text.substring(len < COMPARE_LENGTH ? 0 : len - COMPARE_LENGTH);
        for (int i = 0; i < fragments.length; ++i) {
            String normalized = fragments[i].replace("<em>", "").replace("</em>", "");
            len = normalized.length();
            if (i > 0) {
                sb.append(' ');
            } else if (!expectedPrefix.startsWith(normalized.substring(0, Math.min(len, COMPARE_LENGTH)))) {
                sb.append(FRAGMENTS_SEPARATOR).append(' ');
            }
            sb.append(fragments[i]);
            if (!expectedSuffix.endsWith(normalized.substring(len < COMPARE_LENGTH ? 0 : len - COMPARE_LENGTH))) {
                sb.append(' ').append(FRAGMENTS_SEPARATOR);
            }
        }
        return sb.toString();
    }
}
