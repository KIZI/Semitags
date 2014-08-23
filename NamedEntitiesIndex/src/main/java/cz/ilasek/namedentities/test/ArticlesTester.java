package cz.ilasek.namedentities.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import cz.ilasek.namedentities.index.models.EntityMentionInParagraph;
import cz.ilasek.namedentities.reader.CSVReader;
import cz.ilasek.namedentities.reader.ReaderException;

public class ArticlesTester {

    private class PayLoad {
        private final String articleId;
        private final Set<String> entitiesInArticle;

        public PayLoad(String articleId, Set<String> entitiesInArticle) {
            this.articleId = articleId;
            this.entitiesInArticle = new HashSet<String>(entitiesInArticle);
        }

        public String getArticleId() {
            return articleId;
        }

        public Set<String> getEntitiesInArticle() {
            return entitiesInArticle;
        }

    }

    private static final Logger logger = LoggerFactory.getLogger(ArticlesTester.class);

    private final CSVReader testingReader;
    private final String articleFolder;

    private Set<String> entitiesInArticle = new HashSet<String>();
    String prevArticleId = null;

    public ArticlesTester(String testingFolder, String articleFolder) throws IOException {
        testingReader = new CSVReader(testingFolder);
        this.articleFolder = articleFolder;
    }

    public void testArticles(int countArticles) throws ReaderException, IOException {
        PayLoad payLoad = getArticleEntities();
        double totalPrecision = 0;
        double totalRecall = 0;
        
        for (int i = 0; (i < countArticles) && (payLoad != null); i++) {
            if (payLoad.getArticleId() != null) {
                String article = readArticle(payLoad.getArticleId());

                Client client = Client.create();
                WebResource webResource = client.resource("http://ner.vse.cz/SemiTags/rest/v1/recognize");
                MultivaluedMap formData = new MultivaluedMapImpl();
                formData.add("language", "en");
                formData.add("text", article);
                ClientResponse response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, formData);

                if (response.getStatus() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
                }
                
                List<NamedEntity> entities = response.getEntity(new GenericType<List<NamedEntity>>() {});
                
                int correctRetrieved = 0;
                Set<String> entitiesInArticle = payLoad.getEntitiesInArticle();
                
                for (NamedEntity entity : entities) {
                    if (entitiesInArticle.contains(entity.getWikipediaUri())) {
                        correctRetrieved++;
                    }
                }
                
                
                if (entitiesInArticle.size() > 0) {
                    System.out.println("Article: " + payLoad.articleId);
                    System.out.println("Correct retrieved " + correctRetrieved);
                    System.out.println("Incorrect retrieved " + (entities.size() - correctRetrieved));
                    double precision = 0;
                    double recall = 0;
                    
                    if (entities.size() > 0) {
                        precision = (double)  correctRetrieved / entities.size();
                    }
                    
                    if (entitiesInArticle.size() > 0) {
                        recall = (double)  correctRetrieved / entitiesInArticle.size();
                    }
                    
                    totalPrecision += precision;
                    totalRecall += recall;
                    
                    System.out.println("Precision " + precision);
                    System.out.println("Recall " + recall);
                    System.out.println("Average precision " + (totalPrecision / (i + 1)));
                    System.out.println("Average recall " + (totalRecall / (i + 1)));
                }
            }

            payLoad = getArticleEntities();
        }
    }

    private String readArticle(String articleId) throws IOException {
        FileInputStream fis = new FileInputStream(new File(articleFolder + "/" + articleId + ".txt"));

        byte[] b = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while (fis.read(b) > 0) {
            sb.append(new String(b));
        }
        fis.close();

        return sb.toString();
    }

    private PayLoad getArticleEntities() throws ReaderException {
        EntityMentionInParagraph emip;
        String articleId = null;

        while ((emip = testingReader.readEntityMention()) != null) {
            String[] paragraphId = emip.getParagraphUri().split("#");
            articleId = paragraphId[0];
            if (prevArticleId == null) {
                prevArticleId = articleId;
            }

            if (prevArticleId.equals(articleId)) {
                entitiesInArticle.add(emip.getEntityUri());
            } else {
                PayLoad result = new PayLoad(prevArticleId, entitiesInArticle);
                entitiesInArticle = new HashSet<String>();
                entitiesInArticle.add(emip.getEntityUri());
                prevArticleId = articleId;

                return result;
            }

            prevArticleId = articleId;
        }

        if (articleId == null) {
            return null;
        } else {
            return new PayLoad(articleId, entitiesInArticle);
        }
    }
}
