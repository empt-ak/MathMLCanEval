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
package cz.muni.fi.mir.db.dao.impl;

import java.util.List;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import cz.muni.fi.mir.db.dao.AnnotationValueDAO;
import cz.muni.fi.mir.db.domain.AnnotationValue;

/**
 *
 * @author Dominik Szalai - emptulik at gmail.com
 */
@Repository
public class AnnotationValueDAOImpl extends GenericDAOImpl<AnnotationValue, Long> implements AnnotationValueDAO
{
    private static final Logger logger = Logger.getLogger(AnnotationValueDAOImpl.class);
    
    public AnnotationValueDAOImpl()
    {
        super(AnnotationValue.class);
    }

    @Override
    public List<AnnotationValue> getAll()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AnnotationValue getByValue(String value)
    {
        AnnotationValue result = null;
        try
        {
            result = entityManager.createQuery("SELECT av FROM annotationValue av WHERE av.value = :value", AnnotationValue.class)
                    .setParameter("value", value).getSingleResult();
        }
        catch(NoResultException ex)
        {
            logger.info(ex);
        }
        
        return result;
    }
}
