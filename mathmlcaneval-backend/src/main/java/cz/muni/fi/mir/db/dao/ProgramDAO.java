/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mir.db.dao;

import cz.muni.fi.mir.db.domain.Program;
import java.util.List;

/**
 *
 * @author Empt
 */
public interface ProgramDAO
{
    void createProgram(Program program);
    void deleteProgram(Program program);
    void updateProgram(Program program);
    
    Program getProgramByID(Long id);
    
    List<Program> getProgramByName(String name);
    List<Program> getProgramByNameAndVersion(String name, String version);
    
    
    List<Program> getAllPrograms();    
}