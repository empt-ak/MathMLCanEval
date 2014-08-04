/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.mir.controllers;

import cz.muni.fi.mir.db.service.ApplicationRunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author emptak
 */
@Controller
@RequestMapping("/appruns")
public class ApplicationRunController
{
    
    @Autowired 
    private ApplicationRunService applicationRunService;
    
    @RequestMapping(value = {"/","/list","/list/"},method = RequestMethod.GET)
    public ModelAndView list()
    {
        ModelMap mm = prepareModelMap();
        mm.addAttribute("apprunList", applicationRunService.getAllApplicationRuns());
        
        
        return new ModelAndView("apprun_list",mm);
    }
    
    
    
    
    private ModelMap prepareModelMap()
    {
        ModelMap mm = new ModelMap();
        
        return mm;
    }
}