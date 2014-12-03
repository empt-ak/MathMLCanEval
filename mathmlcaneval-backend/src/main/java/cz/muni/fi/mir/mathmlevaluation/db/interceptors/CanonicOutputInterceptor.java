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
package cz.muni.fi.mir.mathmlevaluation.db.interceptors;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.muni.fi.mir.mathmlevaluation.db.domain.Annotation;
import cz.muni.fi.mir.mathmlevaluation.db.domain.AnnotationValue;
import cz.muni.fi.mir.mathmlevaluation.db.domain.CanonicOutput;
import cz.muni.fi.mir.mathmlevaluation.db.service.AnnotationValueSerivce;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Dominik Szalai - emptulik at gmail.com
 */
@Aspect
@Component
public class CanonicOutputInterceptor
{
    @Autowired private DatabaseEventService databaseEventService;
    @Autowired private DatabaseEventFactory databaseEventFactory;
    @Autowired private AnnotationValueSerivce annotationValueSerivce;
    private static final Pattern pattern = Pattern.compile("(#\\S+)");
    
    @Before("execution(* cz.muni.fi.mir.mathmlevaluation.db.service.CanonicOutputService.annotateCannonicOutput(..)) && args(canonicOutput,annotation)")
    public void annotateCanonicOutput(CanonicOutput canonicOutput, Annotation annotation)
    {
        databaseEventService.createDatabaseEvent(databaseEventFactory
                .newInstance(DatabaseEvent.Operation.UPDATE, 
                        canonicOutput, 
                        "Annotated canonicoutput with " + annotation.getAnnotationContent()
                )
        );
        
        Matcher m = pattern.matcher(annotation.getAnnotationContent());    
                
        while(m.find())
        {
            String match = m.group();
            AnnotationValue aValue = annotationValueSerivce.getAnnotationValueByValue(match);
            if(aValue == null)
            {
                aValue = new AnnotationValue();
                aValue.setValue(match);
                aValue.setType(AnnotationValue.Type.CANONICOUTPUT);
                
                annotationValueSerivce.createAnnotationValue(aValue);
            }
        }
    }

    @Before("execution(* cz.muni.fi.mir.mathmlevaluation.db.service.CanonicOutputService.deleteAnnotationFromCanonicOutput(..)) && args(canonicOutput,annotation)")
    public void deAnotateCanonicOutput(CanonicOutput canonicOutput, Annotation annotation)
    {
        databaseEventService.createDatabaseEvent(databaseEventFactory
                .newInstance(DatabaseEvent.Operation.DELETE,
                        canonicOutput, 
                        "Deleted annotation " + annotation.getAnnotationContent() + " from canonicoutput"
                )
        );
    }
}