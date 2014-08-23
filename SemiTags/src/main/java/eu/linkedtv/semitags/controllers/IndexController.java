package eu.linkedtv.semitags.controllers;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.configuration.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import cz.ilasek.namedentities.disambiguation.CandidatesGenerator;
import cz.ilasek.namedentities.disambiguation.Disambiguation;
import cz.ilasek.namedentities.models.DisambiguatedEntity;
import cz.ilasek.namedentities.recognition.DutchStanfordRecognizer;
import cz.ilasek.namedentities.recognition.EnglishStanfordRecognizer;
import cz.ilasek.namedentities.recognition.GermanStanfordRecognizer;
import cz.ilasek.namedentities.recognition.Recognizer;
import cz.ilasek.nlp.ner.StanfordEntity;

@Controller
public class IndexController {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private Settings settings;
    
    // Recognizer
    @Autowired
    private GermanStanfordRecognizer germanRecognizer;
    
    @Autowired
    private DutchStanfordRecognizer dutchRecognizer;
    
    @Autowired
    private EnglishStanfordRecognizer englishRecognizer;
    
    // Candidates generator
    @Autowired
    @Qualifier("englishCandidatesGenerator")
    private CandidatesGenerator englishCandidatesGenerator;
    
    @Autowired
    @Qualifier("germanCandidatesGenerator")
    private CandidatesGenerator germanCandidatesGenerator;
    
    @Autowired
    @Qualifier("dutchCandidatesGenerator")
    private CandidatesGenerator dutchCandidatesGenerator;    

    // Disambiguation
    @Autowired
    private Disambiguation englishDisambiguation;
    
    @Autowired
    private Disambiguation germanDisambiguation;
    
    @Autowired
    private Disambiguation dutchDisambiguation;
    
//    public void setSpotlightDisambiguation(SpotlightDisambiguation spotlightDisambiguation) {
//        this.spotlightDisambiguation = spotlightDisambiguation;
//    }

    @RequestMapping(method=RequestMethod.GET, value="/index")
    public ModelAndView showIndexPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        logger.debug("Settings " + settings.getRunningMode());
        logger.debug("Show index page.");
        ModelAndView mav = new ModelAndView("index");
        
        return mav;
    }
    
    @RequestMapping(method=RequestMethod.POST, value="/index")
    public ModelAndView showResults(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        logger.debug("Strating disambiguation..");
        request.setCharacterEncoding("UTF-8");
        String recText = request.getParameter("recText");
        String language = request.getParameter("lang");
        
        recText = recText.replaceAll("[^\\p{L}\\p{N}\\n\\.\\,-]", " ");
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("recText", recText);
        mav.addObject("selectedLanguage", language);
        
        logger.debug("Identifying entities for language: " + language);
        
        List<StanfordEntity> identifiedEntities = new LinkedList<StanfordEntity>();
        Recognizer recognizer;
        CandidatesGenerator candidatesGenerator;
        Disambiguation disambiguation;
        
        if (language.equals("nl")) {
            recognizer = dutchRecognizer;
            candidatesGenerator = dutchCandidatesGenerator;
            disambiguation = dutchDisambiguation;
        }
        else if (language.equals("de")) {
            recognizer = germanRecognizer;
            candidatesGenerator = germanCandidatesGenerator;
            disambiguation = germanDisambiguation;
        } else {
            recognizer = englishRecognizer;
            candidatesGenerator = englishCandidatesGenerator;
            disambiguation = englishDisambiguation;
        }
        
        identifiedEntities = recognizer.recognize(recText);
            
        List<DisambiguatedEntity> candidates = new LinkedList<DisambiguatedEntity>();
        
        logger.debug("Generating candidates...");
        int groupId = 1;
        Set<String> uniqueIdentifiedEntities = new HashSet<String>();
        
        for (StanfordEntity identifiedEntity : identifiedEntities) {
            if (!uniqueIdentifiedEntities.contains(identifiedEntity.getName())) {
                logger.debug("Candidates for " + identifiedEntity.getName());
                List<DisambiguatedEntity> candidatesForEntity = candidatesGenerator.getCandidates(identifiedEntity.getName(), groupId++);
                candidates.addAll(candidatesForEntity);
                logger.debug("Candidates count " + candidatesForEntity.size());
                uniqueIdentifiedEntities.add(identifiedEntity.getName());
            }
        }

        logger.debug("Disambiguating entities...");
        Collection<DisambiguatedEntity> disambiguatedEntities = disambiguation.listDisambiguatedEntities(candidates, recText);
        
        mav.addObject("identifiedEntities", identifiedEntities);
        mav.addObject("disambiguatedEntities", disambiguatedEntities);
        
        logger.debug("Finish.");            
        return mav;
    } 
    
    @ModelAttribute("languages")
    public String[] getLanguages() {
        String[] languages = {"de", "nl"};
        
        return languages;
    }    
}
