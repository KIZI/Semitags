package eu.linkedtv.semitags.controllers;

import java.io.IOException;

import javax.servlet.ServletException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class StaticController {
    @RequestMapping(method=RequestMethod.GET, value="/static/{page}")
    public ModelAndView showIndexPage(@PathVariable String page)
            throws ServletException, IOException 
    {
        ModelAndView mav = new ModelAndView("static/" + page);
        
        return mav;
    }
}
