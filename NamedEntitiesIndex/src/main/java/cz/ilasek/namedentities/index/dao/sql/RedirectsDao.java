package cz.ilasek.namedentities.index.dao.sql;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Repository;

import cz.ilasek.namedentities.index.callback.RedirectHandler;
import cz.ilasek.namedentities.index.models.Article;
import cz.ilasek.namedentities.reader.CSVReader;
import cz.ilasek.namedentities.reader.ReaderException;

@Repository
public class RedirectsDao {
    
    private static final Logger logger = LoggerFactory.getLogger(RedirectsDao.class);
    
    private JdbcTemplate jdbcTemplate;
    private Map<String, String> redirects;
    
    private String wikiBaseUrl;
    
    @Autowired
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }    

    public void init(CSVReader csvReader, String wikiBaseUrl) {
        if (!wikiBaseUrl.endsWith("/")) {
            this.wikiBaseUrl = wikiBaseUrl + "/";
        } else {
            this.wikiBaseUrl = wikiBaseUrl;
        }
        redirects = mapRedirects(loadRedirectsFromDatabase(), csvReader);
    }
    
    public boolean isRedirected(String uri) {
        return redirects.containsKey(uri);
    }
    
    public String getTarget(String uri) {
        return redirects.get(uri);
    }
    
    private Map<Long, String> loadRedirectsFromDatabase() {
        RedirectHandler redirectHandler = new RedirectHandler();
        
        logger.info("Fetching redirects from database...");
        jdbcTemplate.query(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps =
                            connection.prepareStatement("SELECT rd_from, CONVERT(rd_title USING utf8) AS rdc_title FROM redirect");
                        ps.setFetchSize(Integer.MIN_VALUE);
                        return ps;
                    }
                }, 
                redirectHandler
            );
        
        logger.info("Redirects fetched...");
        return redirectHandler.getRedirects();
    }
    
    private Map<String, String> mapRedirects(Map<Long, String> longRedirects, CSVReader reader) {
        Map<String, String> strRedirects = new HashMap<String, String>();
        
        Article article;
        try {
            while((article = reader.readArticle()) != null) {
                if (longRedirects.containsKey(new Long(article.getArticleId()))) {
                    try {
                        strRedirects.put(wikiBaseUrl + URLEncoder.encode(article.getArticleTitle().replace(" ", "_"), "UTF-8"), wikiBaseUrl + URLEncoder.encode(longRedirects.get(new Long(article.getArticleId())).replace(" ", "_"), "UTF-8"));
                    } catch (NumberFormatException e) {
                        logger.error("Problem encoding url " + article.getArticleTitle().replace(" ", "_"), e);
                    } catch (UnsupportedEncodingException e) {
                        logger.error("Problem encoding url " + article.getArticleTitle().replace(" ", "_"), e);
                    }
                }
            }
        } catch (ReaderException e) {
            logger.error("Problem reading article from CSV", e);
        }
        
        return strRedirects;
    }
}
