/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.mir.scheduling;

/**
 *
 * @author emptak
 */
public abstract class LongRunningTaskFactory
{
    public abstract CanonicalizationTask createTask();
    public abstract ApplicationRunRemovalTask createAppTask();
    public abstract FormulaImportTask createImportTask();
}