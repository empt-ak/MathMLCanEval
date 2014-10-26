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
package cz.muni.fi.mir.db.service;

import cz.muni.fi.mir.db.domain.Annotation;

/**
 *
 * @author Dominik Szalai - emptulik at gmail.com
 */
public interface AnnotationService
{

    /**
     * Method obtains given annotation based on input id.
     *
     * @param id of annotation to be obtained
     * @return annotation with given id, null if there is no such annotation
     * with input id.
     * @throws IllegalArgumentException if id is null or less than one.
     */
    Annotation getAnnotationByID(Long id) throws IllegalArgumentException;
}
