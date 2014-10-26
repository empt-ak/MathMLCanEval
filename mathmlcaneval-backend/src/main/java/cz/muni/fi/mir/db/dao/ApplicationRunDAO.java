/* 
 * Copyright 2014 MIR@MU.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.muni.fi.mir.db.dao;

import java.util.List;

import cz.muni.fi.mir.db.domain.ApplicationRun;

/**
 *
 * @author Dominik Szalai - emptulik at gmail.com
 */
public interface ApplicationRunDAO extends GenericDAO<ApplicationRun, Long>
{

    /**
     * Method creates ApplicationRun inside database and calls flush after.
     *
     * @param applicationRun ApplicationRun to be persisted.
     */
    void createApplicationRunWithFlush(ApplicationRun applicationRun);

    /**
     * Method obtains all ApplicationRuns from database in <b>DESCENDING</b>
     * order. Newer ApplicationRuns are therefore in front and the old ones are
     * in the back.
     *
     * @return List of all ApplicationRuns in descending order. If there are no
     * ApplicationRuns yet, empty List is returned.
     */
    List<ApplicationRun> getAllApplicationRuns();

    /**
     * Method returns number of canonicalizations for given application run.
     * Reason for this is that canonic outputs are set to lazy mode, thus not
     * loaded during obtaining from database.
     *
     * @param applicationRun to be checked
     * @return number of canonic outputs
     */
    Integer getNumberOfCanonicalizations(ApplicationRun applicationRun);
}
