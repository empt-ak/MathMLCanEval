/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mir.controllers;

import cz.muni.fi.mir.db.domain.Formula;
import cz.muni.fi.mir.db.domain.SourceDocument;
import cz.muni.fi.mir.db.service.FormulaService;
import cz.muni.fi.mir.db.service.ProgramService;
import cz.muni.fi.mir.db.service.SourceDocumentService;
import cz.muni.fi.mir.db.service.UserService;
import cz.muni.fi.mir.forms.FormulaForm;
import cz.muni.fi.mir.forms.UserForm;
import cz.muni.fi.mir.services.FormulaCreator;
import cz.muni.fi.mir.wrappers.SecurityContextFacade;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.dozer.Mapper;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author siska
 */
@Controller
@RequestMapping(value = "/formula")
public class FormulaController
{

    @Autowired
    private FormulaService formulaService;
    @Autowired
    private ProgramService programService;
    @Autowired
    private UserService userService;
    @Autowired
    private SourceDocumentService sourceDocumentService;
    @Autowired
    private SecurityContextFacade securityContext;
    @Autowired
    private Mapper mapper;
    
    @Autowired
    private FormulaCreator formulaCreator;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(UserController.class);

    @RequestMapping(value = {"/create", "/create/"}, method = RequestMethod.GET)
    public ModelAndView createFormula()
    {
        ModelMap mm = new ModelMap();
        mm.addAttribute("formulaForm", new FormulaForm());
        mm.addAttribute("programFormList", programService.getAllPrograms());
        mm.addAttribute("sourceDocumentFormList", sourceDocumentService.getAllDocuments());

        return new ModelAndView("formula_create", mm);
    }

    @Secured("ROLE_USER")
    @RequestMapping(value = {"/create", "/create/"}, method = RequestMethod.POST)
    public ModelAndView createFormulaSubmit(@Valid @ModelAttribute("formulaForm") FormulaForm formulaForm, BindingResult result, Model model) throws IOException
    {
        if (result.hasErrors())
        {
            ModelMap mm = new ModelMap();
            mm.addAttribute("formulaForm", formulaForm);
            mm.addAttribute("programFormList", programService.getAllPrograms());
            mm.addAttribute("sourceDocumentFormList", sourceDocumentService.getAllDocuments());
            mm.addAttribute(model);

            return new ModelAndView("formula_create", mm);
        } else
        {
            String user = securityContext.getLoggedUser();

            formulaForm.setUserForm(mapper.map(userService.getUserByUsername(user), UserForm.class));
            formulaForm.setInsertTime(DateTime.now());
            if (!StringUtils.isBlank(formulaForm.getXml()))
            {
                formulaService.createFormula(mapper.map(formulaForm, Formula.class));
            }

            for (MultipartFile file : formulaForm.getUploadedFiles())
            {
                try
                {
                    // check validity of file content
                    if (file.getSize() > 0)
                    {
                        formulaForm.setXml(new String(file.getBytes(), "UTF-8"));
                        formulaForm.setInsertTime(DateTime.now());
                        formulaService.createFormula(mapper.map(formulaForm, Formula.class));
                    }
                } catch (IOException ex)
                {
                    // ignore wrong uploads
                    logger.info(ex);
                }
            }

            return new ModelAndView("redirect:/");
        }
    }
    
    @RequestMapping(value={"/create/sourcedocument/{sourceID}","/create/sourcedocument/{sourceID}/"},method = RequestMethod.GET)
    public ModelAndView createFormulaFromSourceDocument(@PathVariable Long sourceID)
    {
        SourceDocument sd = sourceDocumentService.getSourceDocumentByID(sourceID);
        Formula f = null;
        try
        {
            f = formulaCreator.extractFormula(sd);
        }
        catch (IOException ex)
        {
            logger.error(ex);
        }
        
        FormulaForm ff = new FormulaForm();
        List<String> contents = Collections.emptyList();
        StringBuilder sb = new StringBuilder();
        try
        {
            contents = Files.readAllLines(FileSystems.getDefault().getPath(sd.getDocumentPath()), Charset.forName("UTF-8"));
        }
        catch (IOException ex)
        {
            logger.error(ex);
            sb.append(ex);
        }
        
        if(sb.length() == 0)
        {
            for(String s : contents)
            {
                sb.append(s);
            }
        }
        
        
        ff.setXml(sb.toString());
        ModelMap mm = new ModelMap();
        
        mm.addAttribute("programFormList", programService.getAllPrograms());
        mm.addAttribute("sourceDocumentFormList", Arrays.asList(sd));
        
        mm.addAttribute("formulaForm", ff);
        
        return new ModelAndView("formula_create", mm);
    }

    @RequestMapping(value = {"/view/{id}", "/view/{id}/"}, method = RequestMethod.GET)
    public ModelAndView viewFormula(@PathVariable Long id)
    {
        ModelMap mm = new ModelMap();
        mm.addAttribute("formulaEntry", formulaService.getFormulaByID(id));

        return new ModelAndView("formula_view", mm);
    }

    //TODO: threaded
    @Secured("ROLE_USER")
    @RequestMapping(value = {"/run/{id}", "/run/{id}/"}, method = RequestMethod.GET)
    public ModelAndView canonicalizeFormula(@PathVariable Long id)
    {
        ModelMap mm = new ModelMap();
        mm.addAttribute("formulaEntry", formulaService.getFormulaByID(id));

        return new ModelAndView("formula_view", mm);
    }

    @Secured("ROLE_USER")
    @RequestMapping(value={"/delete/{id}","/delete/{id}/"},method = RequestMethod.GET)
    public ModelAndView deleteFormula(@PathVariable Long id, HttpServletRequest request)
    {
        Formula f = formulaService.getFormulaByID(id);
        if (request.isUserInRole("ROLE_ADMINISTRATOR") || f.getUser().getUsername().equals(securityContext.getLoggedUser()))
        {
            formulaService.deleteFormula(f);
        } else
        {
            logger.info(String.format("Blocked unauthorized deletion of formula %d triggered by user %s.", id, securityContext.getLoggedUser()));
        }
        return new ModelAndView("redirect:/");
    }
}
