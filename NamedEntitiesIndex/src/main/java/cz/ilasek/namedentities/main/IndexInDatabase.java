package cz.ilasek.namedentities.main;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.WikiXMLParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.SAXException;

import cz.ilasek.namedentities.articlefilter.DatabaseArticleFilter;
import cz.ilasek.namedentities.index.dao.sql.LanguageDaoManager;
import cz.ilasek.namedentities.index.dao.sql.LanguageDaoManager.Language;


/**
 * Hello world!
 *
 */
public class IndexInDatabase 
{
    public static void main( String[] args ) throws UnsupportedEncodingException, FileNotFoundException, IOException, SAXException
    {
        ApplicationContext context =
                new ClassPathXmlApplicationContext(new String[] {"appconfig.xml"});
        
        File directory = new File(args[0]);
        LanguageDaoManager langManager = context.getBean(LanguageDaoManager.class);
        langManager.setLanguage(Language.DUTCH);
        for (String file : directory.list()) {
            System.out.println("Reading: " + directory.getAbsolutePath() + "/" + file);
            InputStream in = new FileInputStream(directory.getAbsolutePath() + "/" + file);
            IArticleFilter handler = context.getBean(DatabaseArticleFilter.class);
            WikiXMLParser wxp = new WikiXMLParser(in, handler);
            wxp.parse();
        }

    }
}
