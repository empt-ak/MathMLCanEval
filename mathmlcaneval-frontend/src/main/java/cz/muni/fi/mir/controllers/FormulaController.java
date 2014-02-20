/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mir.controllers;

import cz.muni.fi.mir.db.domain.Formula;
import cz.muni.fi.mir.db.service.FormulaService;
import cz.muni.fi.mir.db.service.ProgramService;
import cz.muni.fi.mir.db.service.SourceDocumentService;
import cz.muni.fi.mir.db.service.UserService;
import cz.muni.fi.mir.forms.FormulaForm;
import cz.muni.fi.mir.forms.UserForm;
import cz.muni.fi.mir.wrappers.SecurityContextFacade;
import javax.validation.Valid;
import org.dozer.Mapper;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
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
 *
 * @author siska
 */
@Controller
@RequestMapping(value ="/formula")
public class FormulaController
{
    @Autowired private FormulaService formulaService;
    @Autowired private ProgramService programService;
    @Autowired private UserService userService;
    @Autowired private SourceDocumentService sourceDocumentService;

    @Autowired private SecurityContextFacade securityContext;
    @Autowired private Mapper mapper;


    @RequestMapping(value={"/create","/create/"},method = RequestMethod.GET)
    public ModelAndView createFormula()
    {
        ModelMap mm = new ModelMap();
        mm.addAttribute("formulaForm", new FormulaForm());
        mm.addAttribute("programFormList", programService.getAllPrograms());
        mm.addAttribute("sourceDocumentFormList", sourceDocumentService.getAllDocuments());

        return new ModelAndView("formula_create",mm);
    }

    @RequestMapping(value={"/create","/create/"},method = RequestMethod.POST)
    public ModelAndView createFormulaSubmit(@Valid @ModelAttribute("formulaForm") FormulaForm formulaForm, BindingResult result, Model model)
    {
        if(result.hasErrors())
        {
            ModelMap mm = new ModelMap();
            mm.addAttribute("formulaForm", formulaForm);
            mm.addAttribute(model);
            mm.addAttribute("programFormList", programService.getAllPrograms());
            mm.addAttribute("sourceDocumentFormList", sourceDocumentService.getAllDocuments());

            return new ModelAndView("formula_create",mm);
        }
        else
        {
            //get logged in username
            String name = securityContext.getLoggedUser();
            formulaForm.setUserForm(mapper.map(userService.getUserByUsername(name), UserForm.class));
            formulaForm.setInsertTime(DateTime.now());

            formulaService.createFormula(mapper.map(formulaForm, Formula.class));

            return new ModelAndView("redirect:/");
        }
    }


    @RequestMapping(value={"/view/{id}","/view/{id}/"},method = RequestMethod.GET)
    public ModelAndView viewFormula(@PathVariable Long id)
    {
        ModelMap mm = new ModelMap();
        mm.addAttribute("formulaEntry", formulaService.getFormulaByID(id));

        return new ModelAndView("formula_view",mm);
    }
}
