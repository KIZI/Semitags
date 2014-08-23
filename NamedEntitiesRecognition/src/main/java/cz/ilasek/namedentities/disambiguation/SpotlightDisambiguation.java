package cz.ilasek.namedentities.disambiguation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import cz.ilasek.namedentities.index.dao.sql.EntityMentionsDao;
import cz.ilasek.namedentities.index.models.Entity;
import cz.ilasek.namedentities.models.DisambiguatedEntity;

public class SpotlightDisambiguation {
    
    private static final String CONTEXT_FIELD = "context";
    private static final String ENTITY_URI_FIELD = "entity_uri";
    private static final String ENTITY_ID_FIELD = "entity_id";
    
    private final EntityMentionsDao entityMetionsDao;
    
    private StandardAnalyzer analyzer; 
    private Directory index;
    
    public SpotlightDisambiguation(EntityMentionsDao entityMetionsDao) {
        this.entityMetionsDao = entityMetionsDao;
        analyzer = new StandardAnalyzer(Version.LUCENE_35);
    }    
    
    public List<DisambiguatedEntity> disambiguateEntity(String entityName, String context, int limit) {
        List<Entity> candidates = entityMetionsDao.findCandidates(entityName);
        
        try {
            createNewIndex();
            IndexWriter w = getIndexWriter();
//            System.out.println("============================");
//            System.out.println("Candidates for " + entityName);
//            System.out.println("============================");
            for (Entity candidate : candidates) {
//                System.out.println(candidate.getUri());
                StringBuilder sb = new StringBuilder();
                List<String> paragraphs = entityMetionsDao.getEntityMentionParagraphs(candidate.getId(), 100);
                for (String paragraph : paragraphs) {
                    sb.append(" ");
                    sb.append(paragraph);
                }
                addDoc(w, candidate.getId(), candidate.getUri(), sb.toString());
            }
            w.close();
            
            try {
                return findBestMatches(entityName, context, limit);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (CorruptIndexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LockObtainFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
    
    private Directory createNewIndex() {
        index = new RAMDirectory();
        return index;
    }
    
    private IndexWriter getIndexWriter() throws CorruptIndexException, LockObtainFailedException, IOException {

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
        
        return new IndexWriter(index, config);        
    }
    
    private IndexSearcher getIndexSearcher() throws CorruptIndexException, IOException {
        IndexReader reader = IndexReader.open(index);
        
        return new IndexSearcher(reader);
    }
    
    private List<DisambiguatedEntity> findBestMatches(String entityName, String context, int limit) throws ParseException, CorruptIndexException, IOException {
        List<DisambiguatedEntity> disambiguations = new LinkedList<DisambiguatedEntity>();
        
        Query q = new QueryParser(Version.LUCENE_35, CONTEXT_FIELD, analyzer).
                parse(context);
        
        TopScoreDocCollector collector = TopScoreDocCollector.create(limit, true);
        IndexSearcher searcher = getIndexSearcher();
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        for (int i = 0; (i < hits.length) && (i < limit); i++) {
            int bestId = hits[i].doc;
            Document d = searcher.doc(bestId);
            DisambiguatedEntity entity = new DisambiguatedEntity();
            entity.setEntityId(new Long(d.get(ENTITY_ID_FIELD)));
            entity.setUri(d.get(ENTITY_URI_FIELD));
            entity.setName(entityName);
            entity.setStringScore(hits[i].score);
            
            disambiguations.add(entity);
            searcher.close();
        }
        
        return disambiguations;
    }
    
    private static void addDoc(IndexWriter w, long entityId, String entityUri, String context) throws CorruptIndexException, IOException {
        Document doc = new Document();
        doc.add(new Field(ENTITY_ID_FIELD, "" + entityId, Field.Store.YES, Field.Index.NO));
        doc.add(new Field(ENTITY_URI_FIELD, entityUri, Field.Store.YES, Field.Index.NO));
        doc.add(new Field(CONTEXT_FIELD, context, Field.Store.YES, Field.Index.ANALYZED));
        w.addDocument(doc);
    }
}