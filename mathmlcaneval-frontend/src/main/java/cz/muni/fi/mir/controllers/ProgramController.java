package cz.muni.fi.mir.controllers;

import cz.muni.fi.mir.db.domain.Program;
import cz.muni.fi.mir.db.service.ProgramService;
import cz.muni.fi.mir.forms.ProgramForm;
import cz.muni.fi.mir.tools.EntityFactory;
import javax.validation.Valid;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * This class serves for handling requests for Program objects with requests starting with <b>/program</b>
 * path.
 * @author Dominik Szalai
 * @version 1.0
 * @since 1.0
 * TODO null handling
 */
@Controller
@RequestMapping(value ="/program")
public class ProgramController
{
    @Autowired private ProgramService programService;
    @Autowired private Mapper mapper;
    
    @RequestMapping(value={"/","/list","/list/"},method = RequestMethod.GET)
    public ModelAndView list()
    {
        ModelMap mm = new ModelMap();
        mm.addAttribute("programList", programService.getAllPrograms());
        
        return new ModelAndView("program_list",mm);
    }
    
    @RequestMapping(value={"/create","/create/"},method = RequestMethod.GET)
    public ModelAndView createProgram()
    {
        ModelMap mm = new ModelMap();
        mm.addAttribute("programForm", new ProgramForm());
        
        return new ModelAndView("program_create",mm);
    }
    
    @RequestMapping(value={"/create","/create/"},method = RequestMethod.POST)
    public ModelAndView createProgramSubmit(@Valid @ModelAttribute("programForm") ProgramForm programForm, BindingResult result, Model model)
    {
        if(result.hasErrors())
        {
            ModelMap mm = new ModelMap();
            mm.addAttribute("programForm", programForm);
            mm.addAttribute(model);
            
            return new ModelAndView("program_create",mm);
        }
        else
        {
            programService.createProgram(mapper.map(programForm,Program.class));
            return new ModelAndView("redirect:/program/list/");
        }
    }
    
    
    @RequestMapping(value={"/delete/{id}","/delete/{id}/"},method = RequestMethod.GET)
    public ModelAndView deleteProgram(@PathVariable Long id)
    {
        programService.deleteProgram(EntityFactory.createProgram(id));
        
        return new ModelAndView("redirect:/program/list/");
    }
    
    @RequestMapping(value ={"/edit/{id}","/edit/{id}/"},method = RequestMethod.GET)
    public ModelAndView editProgram(@PathVariable Long id)
    {
        ModelMap mm = new ModelMap();
        mm.addAttribute("programForm", mapper.map(programService.getProgramByID(id),ProgramForm.class));
        
        return new ModelAndView("program_edit",mm);
    }
    
    @RequestMapping(value={"/edit/","/edit/"}, method = RequestMethod.POST)
    public ModelAndView editProgramSubmit(@Valid @ModelAttribute("programForm") ProgramForm programForm, BindingResult result, Model model)
    {
        if(result.hasErrors())
        {
            ModelMap mm = new ModelMap();
            mm.addAttribute("programForm", programForm);
            mm.addAttribute(model);
            
            return new ModelAndView("program_edit",mm);
        }
        else
        {
            programService.updateProgram(mapper.map(programForm, Program.class));
            
            return new ModelAndView("redirect:/program/list/");
        }
    }
}