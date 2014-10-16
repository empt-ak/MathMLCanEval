/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.mir.db.dao.impl;

import cz.muni.fi.mir.db.dao.FormulaDAO;
import cz.muni.fi.mir.db.domain.Annotation;
import cz.muni.fi.mir.db.domain.CanonicOutput;
import cz.muni.fi.mir.db.domain.Element;
import cz.muni.fi.mir.db.domain.Formula;
import cz.muni.fi.mir.db.domain.FormulaSearchRequest;
import cz.muni.fi.mir.db.domain.FormulaSearchResponse;
import cz.muni.fi.mir.db.domain.Pagination;
import cz.muni.fi.mir.db.domain.Program;
import cz.muni.fi.mir.db.domain.SourceDocument;
import cz.muni.fi.mir.db.domain.User;
import cz.muni.fi.mir.db.service.FormulaService;
import cz.muni.fi.mir.similarity.SimilarityFormConverter;
import cz.muni.fi.mir.similarity.SimilarityForms;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.apache.lucene.search.Query;
import org.hibernate.Hibernate;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 *
 * @author Empt
 */
@Repository(value = "formulaDAO")
public class FormulaDAOImpl implements FormulaDAO
{

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private SimilarityFormConverter similarityFormConverter;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(FormulaDAOImpl.class);
    private static final Pattern p = Pattern.compile("\\d+");

    @Override
    public void createFormula(Formula formula)
    {
        entityManager.persist(formula);
    }

    @Override
    public void updateFormula(Formula formula)
    {
        entityManager.merge(formula);
    }

    @Override
    public void deleteFormula(Formula formula)
    {
        Formula toDelete = entityManager.find(Formula.class, formula.getId());
        if (toDelete != null)
        {
            if (toDelete.getSimilarFormulas() != null && !toDelete.getSimilarFormulas().isEmpty())
            {
                toDelete.setSimilarFormulas(null);

                entityManager.merge(toDelete);
            }

            List<Formula> haveReference = entityManager
                    .createQuery("SELECT f FROM formula f where :formula MEMBER OF f.similarFormulas", Formula.class)
                    .setParameter("formula", formula)
                    .getResultList();

            if (!haveReference.isEmpty())
            {
                logger.info("References has been found");
                for (Formula referenced : haveReference)
                {
                    List<Formula> newSimilar = referenced.getSimilarFormulas();

                    for (Formula similar : newSimilar)
                    {
                        if (similar.equals(formula))
                        {
                            newSimilar.remove(similar);
                            break;
                        }
                    }

                    referenced.setSimilarFormulas(newSimilar);

                    entityManager.merge(referenced);
                }
            }
            logger.info("Deleting formula [" + formula.getId() + ", " + formula.getHashValue() + "]");
            entityManager.remove(toDelete);
        }
        else
        {
            logger.info("Trying to delete Formula with ID that has not been found. The ID is [" + formula.getId().toString() + "]");
        }
    }

    @Override
    public Formula getFormulaByID(Long id)
    {
        Formula f = entityManager.find(Formula.class, id);
        if (f != null)
        {
            Hibernate.initialize(f);
        }

        return f;
    }

    @Override
    public List<Formula> getFormulasBySourceDocument(SourceDocument sourceDocument)
    {
        List<Formula> resultList = Collections.emptyList();
        try
        {
            resultList = entityManager.createQuery("SELECT f FROM formula f WHERE f.sourceDocument = :sourceDocument", Formula.class)
                    .setParameter("sourceDocument", sourceDocument).getResultList();
        }
        catch (NoResultException nre)
        {
            logger.debug(nre);
        }

        return resultList;
    }

    @Override
    public List<Formula> getFormulasByProgram(Program program)
    {
        List<Formula> resultList = Collections.emptyList();
        try
        {
            resultList = entityManager.createQuery("SELECT f FROM formula f WHERE f.program = :program", Formula.class)
                    .setParameter("program", program).getResultList();
        }
        catch (NoResultException nre)
        {
            logger.debug(nre);
        }

        return resultList;
    }

    @Override
    public List<Formula> getFormulasByUser(User user)
    {
        List<Formula> resultList = Collections.emptyList();
        try
        {
            resultList = entityManager.createQuery("SELECT f FROM formula f WHERE f.user = :user", Formula.class)
                    .setParameter("user", user).getResultList();
        }
        catch (NoResultException nre)
        {
            logger.debug(nre);
        }

        return resultList;
    }

    @Override
    public Formula getFormulaByAnnotation(Annotation annotation)
    {
        Formula result = null;

        try
        {
            result = entityManager.createQuery("SELECT f FROM formula f WHERE :annotationID MEMBER OF f.annotations", Formula.class)
                    .setParameter("annotationID", annotation.getId()).getSingleResult();
        }
        catch(NoResultException nre)
        {
            logger.debug(nre);
        }
        return result;
    }

    @Override
    public List<Formula> getAllFormulas()
    {
        List<Formula> resultList = Collections.emptyList();
        try
        {
            resultList = entityManager.createQuery("SELECT f FROM formula f ORDER BY f.id DESC", Formula.class).getResultList();
        }
        catch (NoResultException nre)
        {
            logger.debug(nre);
        }

        return resultList;
    }

    @Override
    public List<Formula> getAllFormulas(Pagination pagination)
    {
        List<Formula> resultList = Collections.emptyList();
        try
        {
            resultList = entityManager.createQuery("SELECT f FROM formula f ORDER BY f.id DESC", Formula.class)
                    .setFirstResult(pagination.getPageSize() * (pagination.getPageNumber() - 1))
                    .setMaxResults(pagination.getPageSize())
                    .getResultList();
        }
        catch (NoResultException nre)
        {
            logger.debug(nre);
        }

        return resultList;
    }

    @Override
    public int getNumberOfRecords()
    {
        int result = 0;
        try
        {
            result = entityManager.createQuery("SELECT COUNT(f) FROM formula f", Long.class).getSingleResult().intValue();
        }
        catch (NoResultException nre)
        {
            logger.error(nre);
        }

        return result;
    }

    @Override
    public Long exists(String hash)
    {
        Formula f = getFormulaByHash(hash);

        return f == null ? null : f.getId();
    }

    @Override
    public Formula getFormulaByHash(String hash)
    {
        Formula f = null;
        try
        {
            f = entityManager.createQuery("SELECT f FROM formula f WHERE f.hashValue = :hashValue", Formula.class).setParameter("hashValue", hash).getSingleResult();
        }
        catch (NoResultException nre)
        {
            logger.debug(nre);
        }

        return f;
    }

    @Override
    public List<Formula> getAllForHashing()
    {
        List<Formula> resultList = Collections.emptyList();
        try
        {
            resultList = entityManager.createQuery("SELECT f FROM formula f WHERE f.hashValue IS NULL OR f.hashValue <> ''", Formula.class)
                    .getResultList();
        }
        catch (NoResultException nre)
        {
            logger.debug(nre);
        }

        return resultList;
    }

    @Override
    public List<Formula> getFormulasByElements(Collection<Element> collection, int start, int end)
    {
        List<Formula> resultList = Collections.emptyList();
        try
        {
            resultList = entityManager.createQuery("SELECT f FROM formula f WHERE f IN ("
                    + "SELECT ff from formula ff "
                    + "INNER JOIN ff.elements ffe "
                    + "WHERE ffe IN (:elements) "
                    + "GROUP BY ff "
                    + "HAVING COUNT(DISTINCT ff) = (:elementsSize))", Formula.class)
                    .setParameter("elements", collection).setParameter("elementsSize", collection.size())
                    .setFirstResult(start).setMaxResults(end)
                    .getResultList();
        }
        catch (NoResultException nre)
        {
            logger.debug(nre);
        }

        return resultList;
    }

    @Override
    public List<Formula> getAllFormulas(boolean force)
    {
        List<Formula> resultList = Collections.emptyList();
        if (force)
        {
            resultList = getAllFormulas();
        }
        else
        {
            try
            {
                resultList = entityManager.createQuery("SELECT f FROM formula f WHERE f.elements IS EMPTY", Formula.class)
                        .getResultList();
            }
            catch (NoResultException nre)
            {
                logger.debug(nre);
            }
        }

        return resultList;
    }

    @Override
    public void reindex()
    {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        try
        {
            logger.info("Reindexing has started");
            fullTextEntityManager.createIndexer(Formula.class).startAndWait();
        }
        catch (InterruptedException ex)
        {
            logger.fatal(ex);
        }
    }

    @Override
    public FormulaSearchResponse findSimilar(Formula formula, Map<String, String> properties, boolean override, boolean directWrite, Pagination pagination)
    {        
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        org.hibernate.search.jpa.FullTextQuery ftq = null;
        
        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Formula.class)
                .overridesForField("co.element", "keywordAnalyzer")
                .get();
        
        BooleanJunction<BooleanJunction> junction = qb.bool();
        
        SimilarityForms sf = similarityFormConverter.process(formula.getOutputs().get(0));
        
        if(Boolean.valueOf(properties.get(FormulaService.USE_DISTANCE)))
        {
            junction.must(qb.keyword()
                    .fuzzy()
                    .withThreshold(Float.valueOf(properties.get(FormulaService.VALUE_DISTANCEMETHOD)))
                    .withPrefixLength(1)
                    .onField("co.distanceForm")
                    .ignoreFieldBridge()
                    .matching(sf.getDistanceForm())
                    .createQuery());
        }
        
        if(Boolean.valueOf(properties.get(FormulaService.USE_COUNT)))
        {
            if("must".equalsIgnoreCase(properties.get(FormulaService.CONDITION_COUNT)))
            {
                BooleanJunction<BooleanJunction> junctionElements = qb.bool();
                if("must".equalsIgnoreCase(properties.get(FormulaService.VALUE_COUNTELEMENTMETHOD)))
                {
                    for(String stringElement : sf.getCountForm().keySet())
                    {
                        junctionElements.must(qb.keyword()
                                .onField("co.element")
                                .ignoreFieldBridge()
                                .matching(stringElement+"="+sf.getCountForm()
                                        .get(stringElement))
                                .createQuery());
                    }
                }
                else
                {
                    for(String stringElement : sf.getCountForm().keySet())
                    {
                        junctionElements.should(qb.keyword()
                                .onField("co.element")
                                .ignoreFieldBridge()
                                .matching(stringElement+"="+sf.getCountForm()
                                        .get(stringElement))
                                .createQuery());
                    }
                }
                
                junction.must(junctionElements.createQuery());
            }
            else if("should".equalsIgnoreCase(properties.get(FormulaService.CONDITION_COUNT)))
            {
                BooleanJunction<BooleanJunction> junctionElements = qb.bool();
                if("must".equalsIgnoreCase(properties.get(FormulaService.VALUE_COUNTELEMENTMETHOD)))
                {
                    for(String stringElement : sf.getCountForm().keySet())
                    {
                        junctionElements.must(qb.keyword()
                                .onField("co.element")
                                .ignoreFieldBridge()
                                .matching(stringElement+"="+sf.getCountForm()
                                        .get(stringElement))
                                .createQuery());
                    }
                }
                else
                {
                    for(String stringElement : sf.getCountForm().keySet())
                    {
                        junctionElements.should(qb.keyword()
                                .onField("co.element")
                                .ignoreFieldBridge()
                                .matching(stringElement+"="+sf.getCountForm()
                                        .get(stringElement))
                                .createQuery());
                    }
                }
                
                junction.should(junctionElements.createQuery());
            }
            else
            {
                throw new IllegalArgumentException("condi");
            }
        }
        
        if(Boolean.valueOf(properties.get(FormulaService.USE_BRANCH)))
        {
            // we obtain user input from form which might be variable A
            // case A = exact length
            // case -A = [currentLength-A;currentLength]
            // cae +A = [currentLength;currentLength+A]
            // case +-A = [currentLength-A;currentLength+A]
            // case -+A = same as above
            //TODO
            
            //int branchLength = Integer.parseInt(sf.getLongestBranch());
        }
        
        Query query = junction.createQuery();
        logger.info(query);
        
        ftq = fullTextEntityManager.createFullTextQuery(query, Formula.class);
        
        
        
        FormulaSearchResponse fsr = new FormulaSearchResponse();
        fsr.setTotalResultSize(ftq.getResultSize());
        List<Formula> resultList = ftq.setFirstResult(pagination.getPageSize() * (pagination.getPageNumber() - 1))
                .setMaxResults(pagination.getPageSize())
                .getResultList();
        
        // we would like to write results immediately
        if (directWrite)
        {   // override old results ?
            if (override)
            {
                formula.setSimilarFormulas(new ArrayList<>(resultList));
            }
            else
            {   // check if null and append to earlier
                List<Formula> similars = new ArrayList<>();
                if (formula.getSimilarFormulas() != null)
                {
                    similars.addAll(formula.getSimilarFormulas());
                    similars.addAll(resultList);

                    formula.setSimilarFormulas(similars);
                }
                else
                {
                    similars.addAll(resultList);

                    formula.setSimilarFormulas(similars);
                }
            }
            // update
            updateFormula(formula);
        }
        
        ftq.setFirstResult(pagination.getPageSize() * (pagination.getPageNumber() - 1));
        ftq.setMaxResults(pagination.getPageSize());
        fsr.setFormulas(resultList);
        
        return fsr;
    }

    @Override
    public void findSimilarMass(Map<String, String> properties)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FormulaSearchResponse findFormulas(FormulaSearchRequest formulaSearchRequest, Pagination pagination)
    {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        org.hibernate.search.jpa.FullTextQuery ftq = null;  // actual query hitting database
        boolean isEmpty = true;

        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Formula.class)
                .overridesForField("co.annotation", "standardAnalyzer")
                .overridesForField("co.element", "keywordAnalyzer")
                .get();

        //logger.info("$"+qb.keyword().onField("annotation").ignoreFieldBridge().matching(formulaSearchRequest.getAnnotationContent()).createQuery().toString());
        BooleanJunction<BooleanJunction> junction = qb.bool();

        if (formulaSearchRequest.getSourceDocument() != null && formulaSearchRequest.getSourceDocument().getId() != null)
        {
            junction.must(qb.keyword()
                    .onField("sourceDocument.id")
                    .matching(formulaSearchRequest.getSourceDocument().getId())
                    .createQuery()
            );
            
            isEmpty = false;
        }

        if (formulaSearchRequest.getProgram() != null && formulaSearchRequest.getProgram().getId() != null)
        {
            junction.must(qb.keyword()
                    .onField("program.id")
                    .matching(formulaSearchRequest.getProgram().getId())
                    .createQuery()
            );
            
            isEmpty = false;
        }

        if (formulaSearchRequest.getElements() != null && !formulaSearchRequest.getElements().isEmpty())
        {
            BooleanJunction<BooleanJunction> junctionElements = qb.bool();
            for(Element e : formulaSearchRequest.getElements().keySet())
            {
                junctionElements.must(qb.keyword()
                        .onField("co.element")
                        .ignoreFieldBridge()
                        .matching(e.getElementName()+"="+formulaSearchRequest.getElements().get(e))
                        .createQuery()
                );        
                
                isEmpty = false;
            }
            
            junction.must(junctionElements.createQuery());
        }

        if (formulaSearchRequest.getAnnotationContent() != null && !StringUtils.isEmpty(formulaSearchRequest.getAnnotationContent()))
        {
            junction.must(qb.keyword()
                    .onField("co.annotation")
                    .ignoreFieldBridge()
                    .matching(formulaSearchRequest.getAnnotationContent())
                    .createQuery()
            );
            
            isEmpty = false;
        }

        if (formulaSearchRequest.getFormulaContent() != null && !StringUtils.isEmpty(formulaSearchRequest.getFormulaContent()))
        {
            junction.must(qb.keyword()
                    .wildcard()
                    .onField("co.distanceForm")
                    .ignoreFieldBridge()
                    .matching(formulaSearchRequest.getFormulaContent()+"*")
                    .createQuery()
            );
            
            isEmpty = false;
        }
        
        if(formulaSearchRequest.getCoRuns() != null)
        {
            junction.must(qb.keyword()
                    .onField("coRuns")
                    .ignoreFieldBridge()
                    .matching(formulaSearchRequest.getCoRuns())
                    .createQuery()
            );
            
            isEmpty = false;
        }
        
        FormulaSearchResponse fsr = new FormulaSearchResponse();
        
        if(!isEmpty)
        {
            Query query = junction.createQuery();
            logger.info(query);

            ftq = fullTextEntityManager.createFullTextQuery(query, Formula.class);
            fsr.setTotalResultSize(ftq.getResultSize());
            
            ftq.setFirstResult(pagination.getPageSize() * (pagination.getPageNumber() - 1));
            ftq.setMaxResults(pagination.getPageSize());
            fsr.setFormulas(ftq.getResultList());
        }
        else
        {
            fsr.setTotalResultSize(getNumberOfRecords());
            fsr.setFormulas(getAllFormulas(pagination));
        }
        
        return fsr;        
    }

    @Override
    public Formula getFormulaByCanonicOutput(CanonicOutput canonicOutput)
    {
        Formula f = null;
        try
        {
            f = entityManager.createQuery("SELECT f FROM formula f WHERE :co MEMBER OF f.outputs", Formula.class)
                    .setParameter("co", canonicOutput).getSingleResult();
        }
        catch(NoResultException nre)
        {
            logger.error(nre);
        }
        
        return f;
    }

    @Override
    public void index(Formula f)
    {
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        fullTextEntityManager.index(f);
    }
}
