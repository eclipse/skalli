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

import java.io.Closeable;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.model.ExtensibleEntityBase;
import org.eclipse.skalli.services.entity.EntityService;
import org.eclipse.skalli.services.extension.ExtensionService;
import org.eclipse.skalli.services.extension.ExtensionServices;
import org.eclipse.skalli.services.extension.Indexer;
import org.eclipse.skalli.services.search.FacetedSearchResult;
import org.eclipse.skalli.services.search.IndexEntry;
import org.eclipse.skalli.services.search.PagingInfo;
import org.eclipse.skalli.services.search.QueryParseException;
import org.eclipse.skalli.services.search.SearchHit;
import org.eclipse.skalli.services.search.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneIndex<T extends EntityBase> {

    private static final Logger LOG = LoggerFactory.getLogger(LuceneIndex.class);

    private static final SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<em>", "</em>"); //$NON-NLS-1$//$NON-NLS-2$
    private static final String FIELD_UUID = "_uuid"; //$NON-NLS-1$
    private static final int NUMBER_BEST_FRAGMENTS = 3; //TODO this is a candidate for configuration

    private Directory directory = new RAMDirectory();
    private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);

    private final EntityService<T> entityService;

    public LuceneIndex(EntityService<T> entityService) {
        this.entityService = entityService;
    }

    public synchronized void reindexAll() {
        reindex(entityService.getAll());
    }

    private List<IndexEntry> indexEntity(T entity) {
        List<IndexEntry> fields = new LinkedList<IndexEntry>();

        Queue<EntityBase> queue = new LinkedList<EntityBase>();
        queue.add(entity);

        while (!queue.isEmpty()) {
            EntityBase currentEntity = queue.poll();

            for (ExtensionService<?> extensionService : ExtensionServices.getAll()) {
                if (currentEntity.getClass().equals(extensionService.getExtensionClass())) {
                    Indexer<?> indexer = extensionService.getIndexer();
                    if (indexer != null) {
                        indexer.indexEntity(fields, currentEntity);
                    }
                }
            }

            if (currentEntity instanceof ExtensibleEntityBase) {
                queue.addAll(((ExtensibleEntityBase) currentEntity).getAllExtensions());
            }
        }
        return fields;
    }

    private void addEntityToIndex(IndexWriter writer, T entity)
            throws IOException {
        List<IndexEntry> fields = indexEntity(entity);

        Document doc = LuceneUtil.fieldsToDocument(fields);
        doc.add(new Field(FIELD_UUID, entity.getUuid().toString(), Store.YES, Index.NOT_ANALYZED));
        writer.addDocument(doc);
    }

    List<SearchHit<T>> entitiesToHit(List<T> entities) {
        List<SearchHit<T>> ret = new LinkedList<SearchHit<T>>();
        for (T entity : entities) {
            ret.add(entityToHit(entity));
        }
        return ret;
    }

    SearchHit<T> entityToHit(T entity) {
        if (entity == null) {
            return null;
        }
        List<IndexEntry> fields = indexEntity(entity);
        Map<String, List<String>> storedValues = new HashMap<String, List<String>>();
        for (IndexEntry entry : fields) {
            List<String> list = storedValues.get(entry.getFieldName());
            if (list == null) {
                list = new LinkedList<String>();
                storedValues.put(entry.getFieldName(), list);
            }
            list.add(entry.getValue());
        }
        SearchHit<T> ret = new SearchHit<T>(entity, storedValues, storedValues);
        return ret;
    }

    private void addEntitiesToIndex(Collection<T> entities) {
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(directory, analyzer, IndexWriter.MaxFieldLength.UNLIMITED);
            for (T entity : entities) {
                if (!entity.isDeleted()) {
                    addEntityToIndex(writer, entity);
                }
            }
        } catch (LockObtainFailedException e) {
            LOG.error("Failed to add index entries due to Lucene lock", e);
        } catch (Exception e) {
            LOG.error("Failed to add index entries", e);
        } finally {
            closeQuietly(writer);
        }
    }

    public synchronized void reindex(final Collection<T> entities) {
        directory = new RAMDirectory();
        addEntitiesToIndex(entities);
    }

    private String doHighlight(final Highlighter highlighter, final List<String> fields, final String fieldName,
            String fieldContents) throws IOException {
        String highlighted = fieldContents;
        if (fieldContents != null && fields.contains(fieldName)) {
            try {
                String[] fragments = highlighter.getBestFragments(analyzer, fieldName, fieldContents,
                        NUMBER_BEST_FRAGMENTS);
                if (fragments != null && fragments.length > 0) {
                    highlighted = LuceneUtil.withEllipsis(fragments, fieldContents);
                }
            } catch (Exception e) {
                LOG.error(MessageFormat.format("Failed to highlight search result ''{0}''", fieldContents), e);
            }
        }
        return highlighted;
    }

    private ScoreDoc getDocByUUID(Searcher searcher, UUID uuid) throws IOException {
        Query query = null;
        try {
            QueryParser parser = new QueryParser(Version.LUCENE_30, FIELD_UUID, analyzer);
            query = parser.parse(StringUtils.lowerCase(uuid.toString()));
        } catch (ParseException e) {
            LOG.error(MessageFormat.format("Failed to create query from UUID {0}", uuid.toString()), e);
            return null;
        }
        TopScoreDocCollector collector = TopScoreDocCollector.create(2, false);
        searcher.search(query, collector);
        if (collector.getTotalHits() < 1) {
            return null;
        }
        if (collector.getTotalHits() > 1) {
            LOG.error(MessageFormat.format("Too many documents found with UUID {0}", uuid.toString()));
            return null;
        }
        ScoreDoc hit = collector.topDocs().scoreDocs[0];
        return hit;
    }

    public synchronized void remove(final Collection<T> entities) {
        IndexSearcher searcher = null;
        try {
            searcher = new IndexSearcher(directory, false);
            for (EntityBase entity : entities) {
                ScoreDoc hit = getDocByUUID(searcher, entity.getUuid());
                if (hit != null) {
                    searcher.getIndexReader().deleteDocument(hit.doc);
                }
            }
        } catch (LockObtainFailedException e) {
            LOG.error("Failed to remove index entries due to Lucene lock", e);
        } catch (Exception e) {
            LOG.error("Failed to remove index entries", e);
        } finally {
            closeQuietly(searcher);
        }
    }

    public synchronized void update(final Collection<T> entities) {
        // first delete entities from index
        remove(entities);

        // now add them again
        addEntitiesToIndex(entities);
    }

    private T getEntity(Document doc) {
        T ret = entityService.getByUUID(UUID.fromString(doc.get(FIELD_UUID)));
        return ret;
    }

    private SearchHit<T> getSearchHit(final Document doc, final List<String> fields, float score,
            final Highlighter highlighter) throws IOException {
        T entity = getEntity(doc);
        Map<String, List<String>> storedValues = new HashMap<String, List<String>>();
        Map<String, List<String>> highlightedValues = new HashMap<String, List<String>>();

        for (Fieldable f : doc.getFields()) {
            if (!f.isStored()) {
                continue;
            }
            String[] values = doc.getValues(f.name());
            List<String> fieldContents = Arrays.asList(values);
            List<String> highlightedFieldContents = Arrays.asList(values.clone());
            if (fields.contains(f.name())) {
                for (int i = 0; i < highlightedFieldContents.size(); i++) {
                    highlightedFieldContents.set(i,
                            doHighlight(highlighter, fields, f.name(), highlightedFieldContents.get(i)));
                }
            }
            storedValues.put(f.name(), fieldContents);
            highlightedValues.put(f.name(), highlightedFieldContents);
        }

        SearchHit<T> ret = new SearchHit<T>(entity, storedValues, score, highlightedValues);
        return ret;
    }

    public synchronized SearchResult<T> moreLikeThis(T entity, String[] fields, int count) {
        SearchResult<T> moreLikeThis = new SearchResult<T>();
        List<SearchHit<T>> searchHits = new LinkedList<SearchHit<T>>();
        IndexSearcher searcher = null;
        try {
            searcher = new IndexSearcher(directory, true);
            ScoreDoc baseDoc = getDocByUUID(searcher, entity.getUuid());
            if (baseDoc == null) {
                // entity!=null && baseDoc == null, can happen if deleted flag is set
                moreLikeThis.setPagingInfo(new PagingInfo(0, 0));
                moreLikeThis.setResultCount(0);
            } else {
                MoreLikeThis mlt = new MoreLikeThis(searcher.getIndexReader());
                mlt.setFieldNames(fields);
                mlt.setMinWordLen(2);
                mlt.setBoost(true);
                mlt.setMinDocFreq(0);
                mlt.setMinTermFreq(0);
                mlt.setAnalyzer(analyzer);
                Query query = mlt.like(baseDoc.doc);
                int numHits = Math.min(count + 1, entityService.size()); // count + 1: baseDoc will be one of the hits
                TopScoreDocCollector collector = TopScoreDocCollector.create(numHits, false);
                searcher.search(query, collector);

                List<String> fieldList = Arrays.asList(fields);
                Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));
                for (ScoreDoc hit : collector.topDocs().scoreDocs) {
                    if (hit.doc != baseDoc.doc) {
                        Document doc = searcher.doc(hit.doc);
                        SearchHit<T> searchHit = getSearchHit(doc, fieldList, hit.score, highlighter);
                        searchHits.add(searchHit);
                    }
                }
                moreLikeThis.setPagingInfo(new PagingInfo(0, count));
                moreLikeThis.setResultCount(collector.getTotalHits() - 1);
            }
        } catch (Exception e) {
            LOG.error(MessageFormat.format("Searching for entities similiar to ''{0}'' failed", entity.getUuid()), e);
            moreLikeThis.setPagingInfo(new PagingInfo(0, 0));
            moreLikeThis.setResultCount(0);
        } finally {
            closeQuietly(searcher);
        }

        moreLikeThis.setResult(searchHits);
        return moreLikeThis;
    }

    public synchronized SearchResult<T> search(final String[] fields, final String queryString, PagingInfo pagingInfo)
            throws QueryParseException {
        SearchResult<T> ret = new SearchResult<T>();
        search(fields, null, queryString, pagingInfo, ret);
        return ret;
    }

    public synchronized FacetedSearchResult<T> facetedSearch(final String[] fields, String[] facetFields,
            final String queryString, PagingInfo pagingInfo) throws QueryParseException {
        FacetedSearchResult<T> ret = new FacetedSearchResult<T>();
        search(fields, facetFields, queryString, pagingInfo, ret);
        return ret;
    }

    private <R extends SearchResult<T>> R search(final String[] fields, String facetFields[], final String queryString,
            PagingInfo pagingInfo, R ret) throws QueryParseException {
        long start = System.nanoTime();
        List<SearchHit<T>> resultList = new LinkedList<SearchHit<T>>();
        int totalHitCount = 0;
        if (pagingInfo == null) {
            pagingInfo = new PagingInfo(0, 10);
        }
        if (StringUtils.equals("*", queryString) || StringUtils.isEmpty(queryString)) { //$NON-NLS-1$
            List<T> allEntities = entityService.getAll();
            List<T> sublist = allEntities.subList(Math.min(pagingInfo.getStart(), allEntities.size()),
                    Math.min(pagingInfo.getStart() + pagingInfo.getCount(), allEntities.size()));
            resultList.addAll(entitiesToHit(sublist));
            totalHitCount = allEntities.size();
        } else {
            List<String> fieldList = Arrays.asList(fields);
            IndexSearcher searcher = null;
            try {
                searcher = new IndexSearcher(directory);
                QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_30, fields, analyzer);
                Query query = getQuery(parser, queryString);

                // it is not possible that we have more hits than projects!
                int maxHits = entityService.size();
                int numHits = pagingInfo.getStart() + pagingInfo.getCount();
                if (numHits < 0 || numHits > maxHits) {
                    numHits = maxHits;
                }
                if (numHits > 0) {
                    TopDocsCollector<ScoreDoc> collector;
                    if (facetFields == null) {
                        collector = TopScoreDocCollector.create(numHits, false);
                    } else {
                        collector = new FacetedCollector(facetFields, searcher.getIndexReader(), numHits);
                    }

                    searcher.search(query, collector);
                    Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));
                    TopDocs topDocs = collector.topDocs(pagingInfo.getStart(), pagingInfo.getCount());
                    for (ScoreDoc hit : topDocs.scoreDocs) {
                        Document doc = searcher.doc(hit.doc);
                        SearchHit<T> searchHit = getSearchHit(doc, fieldList, hit.score, highlighter);
                        resultList.add(searchHit);
                    }

                    totalHitCount = collector.getTotalHits();
                    if (collector instanceof FacetedCollector && ret instanceof FacetedSearchResult) {
                        ((FacetedSearchResult<T>) ret).setFacetInfo(((FacetedCollector) collector).getFacetsMap());
                    }
                }
            } catch (Exception e) {
                LOG.error(MessageFormat.format("Searching with query ''{0}'' failed", queryString), e);
            } finally {
                closeQuietly(searcher);
            }
        }

        long nanoDuration = System.nanoTime() - start;
        long milliDuration = Math.round(nanoDuration / 1000000d);
        ret.setPagingInfo(pagingInfo);
        ret.setQueryString(queryString);
        ret.setResultCount(totalHitCount);
        ret.setResult(resultList);
        ret.setDuration(milliDuration);
        return ret;
    }

    private Query getQuery(QueryParser parser, String queryString) throws QueryParseException {
        Query query = null;
        try {
            // first, allow full Lucene query syntax
            // http://lucene.apache.org/java/2_4_0/queryparsersyntax.html
            query = parser.parse(queryString);
        } catch (ParseException e) {
            // if the parsing failed because of invalid query syntax,
            // escape the query string and try again
            String escapedQueryString = QueryParser.escape(queryString);
            try {
                query = parser.parse(escapedQueryString);
            } catch (ParseException ex) {
                // if that fails, too, give up
                throw new QueryParseException(ex);
            }
        }
        return query;
    }

    private void closeQuietly(Closeable closable) {
        try {
            if (closable != null) {
                closable.close();
            }
        } catch (IOException e) {
            LOG.error(MessageFormat.format("Failed to close {0}", closable.getClass().getName()), e);
        }
    }
}
