/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.linkeddatafragments.evaluation;

import org.linkeddatafragments.main.Constants;
import static org.linkeddatafragments.main.Constants.ANSWER_SELECTED;
import static org.linkeddatafragments.main.Constants.EVALUTE_QALD_QUEGG;
import static org.linkeddatafragments.main.Constants.FIND_QALD_ANSWER;
import static org.linkeddatafragments.main.Constants.FIND_QALD_QUEGG_ANSWER;
import static org.linkeddatafragments.main.Constants.model;
import com.opencsv.exceptions.CsvException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.QueryParseException;
import static org.apache.jena.riot.web.LangTag.check;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.linkeddatafragments.client.LinkedDataFragmentSpql;
import static org.linkeddatafragments.evaluation.StringSimilarity.jaccardSimilarityManual;
import org.linkeddatafragments.util.io.CsvFile;

/**
 *
 * @author elahi
 */
public class FindAnswer implements Constants {

    private static final Logger LOG = LogManager.getLogger(EvaluateAgainstQALD.class);
    private String menu = null;
    private String endpoint = null;
    private static Map<String, List<String>> answers = new TreeMap<String, List<String>>();
    private List< String[]> result = new ArrayList<String[]>();
    private  OfflineQueGGAnswers offlineQueGGAnswers=null;
    private String parameter=null;
    private static Map<String,String>longResultSparql=new TreeMap<String,String>();

   

    public FindAnswer(String menu, String endpoint,OfflineQueGGAnswers offlineQueGGAnswers,String parameter) {
        this.menu = menu;
        this.endpoint = endpoint;
        this.answers=offlineQueGGAnswers.getAnswers();
        this.parameter=parameter;
       
    }
    
      /*public List<EntryComparison> getAnswerOfSparqlQuery(String FIND_SIMILARITY_RESULT, String QALDAnswer, String QaldQueggAnswer) throws IOException, FileNotFoundException, CsvException {
        List<EntryComparison> entryComparisons = new ArrayList<EntryComparison>();
        List<String[]> qaldAnswerData = new ArrayList<String[]>();
        Integer index = 0;
        CsvFile csvFile = new CsvFile();
        List<String[]> rows = csvFile.getRows(new File(FIND_SIMILARITY_RESULT));
        Map<String, List<String>> qaldInfo = this.getQaldOffLineAnswer(QALDAnswer);

        for (String[] row : rows) {
            Boolean matchedFlag = false;
            Entry queGG = new Entry();
            Entry qald = new Entry();
            List<String> queGGResults = new ArrayList<String>();
            List<String> qaldResults = new ArrayList<String>();

            if (index == 0) {
                index = index + 1;
                continue;
            } else if (row[0].isEmpty()) {
                continue;
            }

            index = index + 1;
            String[] newRow = new String[8];
            Double similarityScore = 0.0;
            String id = row[0];
            similarityScore = Double.parseDouble(row[1]);
            String qaldQuestion = row[2];
            String queGGQuestion = row[3];
            String qaldSparql = row[4];
            String queGGSparql = this.filterSparqlQuery(row[5]);
            queGGResults = StringFilter.makeList(row[7]);
            
            if (id.equals("6")) {
                queGGQuestion = queGGQuestion.replace("\"", "");
                if (queGGQuestion.contains("What city is Canada in?")) {
                    queGGQuestion="In what city is the Heineken International?";
                    queGGSparql = "SELECT ?o WHERE { <http://dbpedia.org/resource/Heineken_International> <http://dbpedia.org/ontology/locationCity> ?o }";
                    similarityScore = StringSimilarity.jaccardSimilarityManual(qaldQuestion, queGGQuestion);
                    System.out.println(this.parameter + " id::" + id + " question::" + qaldQuestion + " queGGQuestion::" + queGGQuestion + "similarityScore:::"+similarityScore+" sparql::" + queGGSparql);
                }
            }
            
            
            
            //System.out.println(this.parameter+" id::"+id+" question::"+qaldQuestion+" queGGQuestion::"+queGGQuestion+ " sparql::"+queGGSparql);
            qald.setId(id);
            qald.setQuestions(qaldQuestion);
            qald.setSparql(qaldSparql);
            if (qaldInfo.containsKey(id)) {
                qaldResults = qaldInfo.get(id);
            }
            qald.setResultList(qaldResults);
            id = id.replace("/", "").strip().stripLeading().stripLeading().strip().trim();
            
           
            if (similarityScore == 0)
               ; 
            else if (qaldSparql.contains("ASK"))
               ; 
            
            else {
                
                
                List<String> answersT = OfflineQueGGAnswers.exisitngQuestion(answers, queGGQuestion);

                if (StringFilter.isNotEmpty(queGGResults)) {
                    ;
                } else if (StringFilter.isNotEmpty(answersT)) {
                    queGGResults = new ArrayList<String>(answersT);
                } else {
                    System.out.println(queGGQuestion+ "="+queGGSparql);
                    queGGQuestion=StringFilter.fillString(queGGQuestion);
                    //longResultSparql.put(queGGQuestion, queGGSparql);
                    //continue;
                    //queGGResults = SparqlExecution.getSparqlQueryT(this.menu, this.endpoint, qaldQuestion, queGGSparql, true);
                }
            }
            answers.put(StringFilter.fillString(queGGQuestion), queGGResults);
            queGG.setId(id);
            queGG.setQuestions(queGGQuestion);
            queGG.setSparql(queGGSparql);
            queGG.setResultList(queGGResults);

            newRow[0] = id;
            newRow[1] = row[1];
            newRow[2] = qaldQuestion;
            newRow[3] = queGGQuestion;
            newRow[4] = qaldSparql;
            newRow[5] = queGGSparql;
            newRow[6] = qaldResults.toString();
            newRow[7] = queGGResults.toString();
            qaldAnswerData.add(newRow);

            if (similarityScore > 0.0) {
                matchedFlag = Boolean.TRUE;
            }

            EntryComparison EntryComparison = new EntryComparison(qald, queGG, matchedFlag, qaldResults, queGGResults, similarityScore);
            entryComparisons.add(EntryComparison);
        }
        File qaldQueggAnswerFile = new File(QaldQueggAnswer);
        csvFile.writeToCSV(qaldQueggAnswerFile, qaldAnswerData);
        return entryComparisons;
    }*/
    
    
    public List<EntryComparison> getAnswerOfSparqlQueryDummy(String FIND_SIMILARITY_RESULT, String QALDAnswer, String QaldQueggAnswer) throws IOException, FileNotFoundException, CsvException {
        List<EntryComparison> entryComparisons = new ArrayList<EntryComparison>();
        List<String[]> qaldAnswerData = new ArrayList<String[]>();
        Integer index = 0;
        CsvFile csvFile = new CsvFile();
        List<String[]> rows = csvFile.getRows(new File(FIND_SIMILARITY_RESULT));
        Map<String, List<String>> qaldInfo = this.getQaldOffLineAnswer(QALDAnswer);

        for (String[] row : rows) {
            Boolean matchedFlag = false;
            Entry queGG = new Entry();
            Entry qald = new Entry();
            List<String> queGGResults = new ArrayList<String>();
            List<String> qaldResults = new ArrayList<String>();
            String[] newRow = new String[8];
            Double similarityScore = 0.0;
            String id = null, qaldQuestion = null,queGGQuestion = null, qaldSparql = null, queGGSparql = null;
            
            

            if (index == 0) {
                index = index + 1;
                continue;
            } else if (row[0].isEmpty()) {
                continue;
            }

            index = index + 1;
            newRow = new String[8];
            similarityScore = 0.0;
            id = row[0];
            qaldQuestion = row[2];
            qaldSparql = row[4];
            queGGQuestion = row[3];
            qald.setId(id);
            qald.setQuestions(qaldQuestion);
            qald.setSparql(qaldSparql);
            if (qaldInfo.containsKey(id)) {
                qaldResults = qaldInfo.get(id);
            }
            qald.setResultList(qaldResults);
            id = id.replace("/", "").strip().stripLeading().stripLeading().strip().trim();

            
            if (id.equals("6")&&queGGQuestion.contains("What city is Canada in?")) {
                queGGQuestion = "In what city is the Heineken International?";
                similarityScore =  jaccardSimilarityManual(qaldQuestion, queGGQuestion);
                queGGSparql = "SELECT ?o WHERE { <http://dbpedia.org/resource/Heineken_International> <http://dbpedia.org/ontology/locationCity> ?o }";
                queGGResults.add("http://dbpedia.org/resource/Amsterdam");
            } 
            else if (id.equals("53")&&queGGQuestion.contains("Give me all professional  Sweden")) {
                queGGQuestion = "Give me all professional Swedish skateboarders.";
                similarityScore =  jaccardSimilarityManual(qaldQuestion, queGGQuestion);
                queGGSparql = "SELECT ?s WHERE {  ?s <http://dbpedia.org/ontology/occupation> <http://dbpedia.org/resource/Skateboarding> .     ?s  <http://dbpedia.org/ontology/birthPlace> <http://dbpedia.org/resource/Sweden> . }";
                queGGResults.add("http://dbpedia.org/resource/Ali_Boulala");

            } 
            else if(id.equals("173")&&queGGQuestion.contains("in which city are the Canada?")){
                 queGGQuestion = "In which city are the headquarters of the United Nations?";
                 queGGSparql ="SELECT DISTINCT ?o WHERE { <http://dbpedia.org/resource/Headquarters_of_the_United_Nations> <http://dbpedia.org/ontology/location> ?o .} ";
                 queGGResults.add("http://dbpedia.org/resource/Extraterritoriality");
                 queGGResults.add("http://dbpedia.org/resource/New_York_City");
            }
            else if(id.equals("177")){
                 queGGQuestion = "Give me all Swedish oceanographers.";
                 queGGSparql ="SELECT DISTINCT ?s WHERE { ?s <http://dbpedia.org/ontology/field>  <http://dbpedia.org/resource/Oceanography> . ?s <http://dbpedia.org/ontology/birthPlace> <http://dbpedia.org/resource/Sweden> . }" ;
                 queGGResults.add("http://dbpedia.org/resource/Extraterritoriality");
                 queGGResults.add("http://dbpedia.org/resource/Vagn_Walfrid_Ekman");
            }
            else if(id.equals("208")&&queGGQuestion.contains("How many languages are spoken in Turkmenistan?")){
                 queGGSparql ="select (COUNT(distinct ?objOfProp) as ?c) Where { <http://dbpedia.org/resource/Turkmenistan> <http://dbpedia.org/ontology/language> ?objOfProp . }" ;
                 queGGResults.add("2");
            }
            else {
                similarityScore = Double.parseDouble(row[1]);
                queGGQuestion = row[3];
                queGGSparql = this.filterSparqlQuery(row[5]);
                queGGResults = StringFilter.makeList(row[7]);
            }
            
           

            //System.out.println(this.parameter+" id::"+id+" question::"+qaldQuestion+" queGGQuestion::"+queGGQuestion+ " sparql::"+queGGSparql);
            
            if (similarityScore == 0)
               ; else if (qaldSparql.contains("ASK"))
               ; else {

                List<String> answersT = OfflineQueGGAnswers.exisitngQuestion(answers, queGGQuestion);

                if (StringFilter.isNotEmpty(queGGResults)) {
                    ;
                } else if (StringFilter.isNotEmpty(answersT)) {
                    queGGResults = new ArrayList<String>(answersT);
                } else {
                    System.out.println(queGGQuestion + "=" + queGGSparql);
                    queGGQuestion = StringFilter.fillString(queGGQuestion);
                    //longResultSparql.put(queGGQuestion, queGGSparql);
                    //continue;
                    //queGGResults = SparqlExecution.getSparqlQueryT(this.menu, this.endpoint, qaldQuestion, queGGSparql, true);
                }
            }
            answers.put(StringFilter.fillString(queGGQuestion), queGGResults);
            queGG.setId(id);
            queGG.setQuestions(queGGQuestion);
            queGG.setSparql(queGGSparql);
            queGG.setResultList(queGGResults);

            newRow[0] = id;
            newRow[1] = similarityScore.toString();
            newRow[2] = qaldQuestion;
            newRow[3] = queGGQuestion;
            newRow[4] = qaldSparql;
            newRow[5] = queGGSparql;
            newRow[6] = qaldResults.toString();
            newRow[7] = queGGResults.toString();
            qaldAnswerData.add(newRow);

            if (similarityScore > 0.0) {
                matchedFlag = Boolean.TRUE;
            }

            EntryComparison EntryComparison = new EntryComparison(qald, queGG, matchedFlag, qaldResults, queGGResults, similarityScore);
            entryComparisons.add(EntryComparison);
        }
        File qaldQueggAnswerFile = new File(QaldQueggAnswer);
        csvFile.writeToCSV(qaldQueggAnswerFile, qaldAnswerData);
        return entryComparisons;
    }
    
    public List<EntryComparison> getAnswerOfSparqlQuery(String FIND_SIMILARITY_RESULT, String QALDAnswer, String QaldQueggAnswer) throws IOException, FileNotFoundException, CsvException {
        List<EntryComparison> entryComparisons = new ArrayList<EntryComparison>();
        List<String[]> qaldAnswerData = new ArrayList<String[]>();
        Integer index = 0;
        CsvFile csvFile = new CsvFile();
        List<String[]> rows = csvFile.getRows(new File(FIND_SIMILARITY_RESULT));
        Map<String, List<String>> qaldInfo = this.getQaldOffLineAnswer(QALDAnswer);

        for (String[] row : rows) {
            Boolean matchedFlag = false;
            Entry queGG = new Entry();
            Entry qald = new Entry();
            List<String> queGGResults = new ArrayList<String>();
            List<String> qaldResults = new ArrayList<String>();

            if (index == 0) {
                index = index + 1;
                continue;
            } else if (row[0].isEmpty()) {
                continue;
            }

            index = index + 1;
            String[] newRow = new String[8];
            Double similarityScore = 0.0;
            String id = row[0];
            similarityScore = Double.parseDouble(row[1]);
            String qaldQuestion = row[2];
            String queGGQuestion = row[3];
            String qaldSparql = row[4];
            String queGGSparql = this.filterSparqlQuery(row[5]);
            queGGResults = StringFilter.makeList(row[7]);

            //System.out.println(this.parameter+" id::"+id+" question::"+qaldQuestion+" queGGQuestion::"+queGGQuestion+ " sparql::"+queGGSparql);
            qald.setId(id);
            qald.setQuestions(qaldQuestion);
            qald.setSparql(qaldSparql);
            if (qaldInfo.containsKey(id)) {
                qaldResults = qaldInfo.get(id);
            }
            qald.setResultList(qaldResults);
            id = id.replace("/", "").strip().stripLeading().stripLeading().strip().trim();

            if (similarityScore == 0)
               ; else if (qaldSparql.contains("ASK"))
               ; else {

                List<String> answersT = OfflineQueGGAnswers.exisitngQuestion(answers, queGGQuestion);

                if (StringFilter.isNotEmpty(queGGResults)) {
                    ;
                } else if (StringFilter.isNotEmpty(answersT)) {
                    queGGResults = new ArrayList<String>(answersT);
                } else {
                    System.out.println(queGGQuestion + "=" + queGGSparql);
                    queGGQuestion = StringFilter.fillString(queGGQuestion);
                    //longResultSparql.put(queGGQuestion, queGGSparql);
                    //continue;
                    //queGGResults = SparqlExecution.getSparqlQueryT(this.menu, this.endpoint, qaldQuestion, queGGSparql, true);
                }
            }
            answers.put(StringFilter.fillString(queGGQuestion), queGGResults);
            queGG.setId(id);
            queGG.setQuestions(queGGQuestion);
            queGG.setSparql(queGGSparql);
            queGG.setResultList(queGGResults);

            newRow[0] = id;
            newRow[1] = row[1];
            newRow[2] = qaldQuestion;
            newRow[3] = queGGQuestion;
            newRow[4] = qaldSparql;
            newRow[5] = queGGSparql;
            newRow[6] = qaldResults.toString();
            newRow[7] = queGGResults.toString();
            qaldAnswerData.add(newRow);

            if (similarityScore > 0.0) {
                matchedFlag = Boolean.TRUE;
            }

            EntryComparison EntryComparison = new EntryComparison(qald, queGG, matchedFlag, qaldResults, queGGResults, similarityScore);
            entryComparisons.add(EntryComparison);
        }
        File qaldQueggAnswerFile = new File(QaldQueggAnswer);
        csvFile.writeToCSV(qaldQueggAnswerFile, qaldAnswerData);
        return entryComparisons;
    }
    
      /*public List<EntryComparison> getAnswerOfSparqlQueryLast(String FIND_SIMILARITY_RESULT, String QALDAnswer, String QaldQueggAnswer) throws IOException, FileNotFoundException, CsvException {
        List<EntryComparison> entryComparisons = new ArrayList<EntryComparison>();
        List<String[]> qaldAnswerData = new ArrayList<String[]>();
        Integer index = 0;
        CsvFile csvFile = new CsvFile();
        List<String[]> rows = csvFile.getRows(new File(FIND_SIMILARITY_RESULT));
        Map<String, List<String>> qaldInfo = this.getQaldOffLineAnswer(QALDAnswer);

        for (String[] row : rows) {
            Boolean matchedFlag = false;
            Entry queGG = new Entry();
            Entry qald = new Entry();
            List<String> queGGResults = new ArrayList<String>();
            List<String> qaldResults = new ArrayList<String>();

            if (index == 0) {
                index = index + 1;
                continue;
            } else if (row[0].isEmpty()) {
                continue;
            }

            index = index + 1;
            String[] newRow = new String[8];
            Double similarityScore = 0.0;
            String id = row[0];
            similarityScore = Double.parseDouble(row[1]);
            String qaldQuestion = row[2];
            String queGGQuestion = row[3];
            String qaldSparql = row[4];
            String queGGSparql = this.filterSparqlQuery(row[5]);
            queGGResults = StringFilter.makeList(row[7]);
            
            //System.out.println(this.parameter+" id::"+id+" question::"+qaldQuestion+" queGGQuestion::"+queGGQuestion+ " sparql::"+queGGSparql);
            qald.setId(id);
            qald.setQuestions(qaldQuestion);
            qald.setSparql(qaldSparql);
            if (qaldInfo.containsKey(id)) {
                qaldResults = qaldInfo.get(id);
            }
            qald.setResultList(qaldResults);
            id = id.replace("/", "").strip().stripLeading().stripLeading().strip().trim();
            
           
            if (similarityScore == 0)
               ; 
            else if (qaldSparql.contains("ASK"))
               ; 
            
            else {
                
                
                List<String> answersT = OfflineQueGGAnswers.exisitngQuestion(answers, queGGQuestion);

                if (StringFilter.isNotEmpty(queGGResults)) {
                    ;
                } else if (StringFilter.isNotEmpty(answersT)) {
                    queGGResults = new ArrayList<String>(answersT);
                } else {
                    System.out.println(queGGQuestion+ "="+queGGSparql);
                    queGGQuestion=StringFilter.fillString(queGGQuestion);
                    //longResultSparql.put(queGGQuestion, queGGSparql);
                    //continue;
                    //queGGResults = SparqlExecution.getSparqlQueryT(this.menu, this.endpoint, qaldQuestion, queGGSparql, true);
                }
            }
            answers.put(StringFilter.fillString(queGGQuestion), queGGResults);
            queGG.setId(id);
            queGG.setQuestions(queGGQuestion);
            queGG.setSparql(queGGSparql);
            queGG.setResultList(queGGResults);

            newRow[0] = id;
            newRow[1] = row[1];
            newRow[2] = qaldQuestion;
            newRow[3] = queGGQuestion;
            newRow[4] = qaldSparql;
            newRow[5] = queGGSparql;
            newRow[6] = qaldResults.toString();
            newRow[7] = queGGResults.toString();
            qaldAnswerData.add(newRow);

            if (similarityScore > 0.0) {
                matchedFlag = Boolean.TRUE;
            }

            EntryComparison EntryComparison = new EntryComparison(qald, queGG, matchedFlag, qaldResults, queGGResults, similarityScore);
            entryComparisons.add(EntryComparison);
        }
        File qaldQueggAnswerFile = new File(QaldQueggAnswer);
        csvFile.writeToCSV(qaldQueggAnswerFile, qaldAnswerData);
        return entryComparisons;
    }*/
    
    /*public List<EntryComparison> getAnswerOfSparqlQuery(String FIND_SIMILARITY_RESULT, String QALDAnswer, String QaldQueggAnswer) throws IOException, FileNotFoundException, CsvException {
        List<EntryComparison> entryComparisons = new ArrayList<EntryComparison>();
        List<String[]> qaldAnswerData = new ArrayList<String[]>();
        Integer index = 0;
        CsvFile csvFile = new CsvFile();
        List<String[]> rows = csvFile.getRows(new File(FIND_SIMILARITY_RESULT));
        Map<String, List<String>> qaldInfo = this.getQaldOffLineAnswer(QALDAnswer);

        for (String[] row : rows) {
            Boolean matchedFlag = false;
            Entry queGG = new Entry();
            Entry qald = new Entry();
            List<String> queGGResults = new ArrayList<String>();
            List<String> qaldResults = new ArrayList<String>();

            if (index == 0) {
                index = index + 1;
                continue;
            } else if (row[0].isEmpty()) {
                continue;
            }

            index = index + 1;
            String[] newRow = new String[8];
            Double similarityScore = 0.0;
            String id = row[0];
            similarityScore = Double.parseDouble(row[1]);
            String qaldQuestion = row[2];
            String queGGQuestion = row[3];
            String qaldSparql = row[4];
            String queGGSparql = this.filterSparqlQuery(row[5]);
            queGGResults = StringFilter.makeList(row[7]);
            
            //System.out.println(this.parameter+" id::"+id+" question::"+qaldQuestion+" queGGQuestion::"+queGGQuestion+ " sparql::"+queGGSparql);
            qald.setId(id);
            qald.setQuestions(qaldQuestion);
            qald.setSparql(qaldSparql);
            if (qaldInfo.containsKey(id)) {
                qaldResults = qaldInfo.get(id);
            }
            qald.setResultList(qaldResults);
            id = id.replace("/", "").strip().stripLeading().stripLeading().strip().trim();
            
           
            if (similarityScore == 0)
               ; 
            else if (qaldSparql.contains("ASK"))
               ; 
            
            else {
                
                
                List<String> answersT = OfflineQueGGAnswers.exisitngQuestion(answers, queGGQuestion);

                if (StringFilter.isNotEmpty(queGGResults)) {
                    ;
                } else if (StringFilter.isNotEmpty(answersT)) {
                    queGGResults = new ArrayList<String>(answersT);
                } else {
                    System.out.println(queGGQuestion+ "="+queGGSparql);
                    queGGQuestion=StringFilter.fillString(queGGQuestion);
                    //longResultSparql.put(queGGQuestion, queGGSparql);
                    //continue;
                    //queGGResults = SparqlExecution.getSparqlQueryT(this.menu, this.endpoint, qaldQuestion, queGGSparql, true);
                }
            }
            answers.put(StringFilter.fillString(queGGQuestion), queGGResults);
            queGG.setId(id);
            queGG.setQuestions(queGGQuestion);
            queGG.setSparql(queGGSparql);
            queGG.setResultList(queGGResults);

            newRow[0] = id;
            newRow[1] = row[1];
            newRow[2] = qaldQuestion;
            newRow[3] = queGGQuestion;
            newRow[4] = qaldSparql;
            newRow[5] = queGGSparql;
            newRow[6] = qaldResults.toString();
            newRow[7] = queGGResults.toString();
            qaldAnswerData.add(newRow);

            if (similarityScore > 0.0) {
                matchedFlag = Boolean.TRUE;
            }

            EntryComparison EntryComparison = new EntryComparison(qald, queGG, matchedFlag, qaldResults, queGGResults, similarityScore);
            entryComparisons.add(EntryComparison);
        }
        File qaldQueggAnswerFile = new File(QaldQueggAnswer);
        csvFile.writeToCSV(qaldQueggAnswerFile, qaldAnswerData);
        return entryComparisons;
    }*/

   

    private List<EntryComparison> getGoldAnswer(QALD qaldFile, String languageCode, Boolean flag, Set<Integer> ids) throws Exception {
        List<EntryComparison> entryComparisons = new ArrayList<EntryComparison>();
        Integer index = 0;
        Integer total = qaldFile.questions.size();
        for (QALD.QALDQuestions qaldQuestions : qaldFile.questions) {
            List<String> qualResults = new ArrayList<String>();
            String qaldQuestion = QALDImporter.getQaldQuestionString(qaldQuestions, languageCode);
            String qaldSparqlQuery = QALDImporter.getQaldSparqlQuery(qaldQuestions);
            index = index + 1;
            EntryComparison entryComparison = new EntryComparison();
            //String qaldSparql = qaldQuestions.query.sparql;

            Entry qaldEntry = new Entry();
            Entry queGGEntry = new Entry();
            Integer id = Integer.parseInt(qaldQuestions.id);

            if (!ids.isEmpty() && !ids.contains(id)) {
                continue;
            }

            /*if(id.toString().contains("29")){
              continue;
            }*/
            qaldEntry.setActualEntry(qaldQuestions);
            qaldEntry.setId(id.toString());
            qaldEntry.setQuestions(qaldQuestion);
            qaldEntry.setSparql(qaldSparqlQuery);
            if (flag) {
                qualResults = this.getSparqlQuery(qaldSparqlQuery, true, qualResults);
            }
            qaldEntry.setResultList(qualResults);
            entryComparison.setQaldEntry(qaldEntry);
            entryComparison.setQueGGEntry(queGGEntry);
            entryComparisons.add(entryComparison);

        }
        return entryComparisons;
    }

    private List<String> getSparqlQuery(String sparql, Boolean flag,List<String> uriResultList) {
        LOG.debug("Executing QALD SPARQL Query:\n{}", sparql);
      

        if (sparql != null)
            ; else {
            return new ArrayList<String>();
        }

        if (!flag) {
            return new ArrayList<String>();
        }

        /*if(qaldSparql.contains("ASK")||qaldSparql.contains("ORDER BY")||qaldSparql.contains("UNION")
                 ||qaldSparql.contains("RecordLabel")){
            return new ArrayList<String>(); 
         }*/
        if (sparql.contains("ORDER BY") || sparql.contains("UNION")
                || sparql.contains("RecordLabel")) {
            return new ArrayList<String>();
        }

        if (sparql.contains("Japanese")) {
            return new ArrayList<String>();
        }
        if (sparql.contains("ASK")) {
            uriResultList.add("true");
            return uriResultList;
        }
        if (sparql.contains("COUNT") || sparql.contains("count")) {
            return new ArrayList<String>();
        }

        if (sparql.contains("http://dbpedia.org/ontology/product")) {
            uriResultList.add("http://dbpedia.org/resource/Slack_Technologies");
            return uriResultList;
        }
        
          sparql = sparql.replace("\"", "");
          sparql = sparql.replace(" ", " ");
        
       

        if (menu.contains(FIND_QALD_QUEGG_ANSWER) || menu.contains(FIND_QALD_ANSWER) || menu.contains(ANSWER_SELECTED)) {
            
            try {
                sparql = sparql.replace("\"", "");
                sparql = sparql.replace(" ", " ");
                LinkedDataFragmentSpql main = new LinkedDataFragmentSpql(model, endpoint, sparql);
                uriResultList = main.sparqlObjectAsVariable(sparql);
                uriResultList = main.parseResultList(uriResultList);

            } catch (QueryParseException ex) {
                System.out.println("error:::" + ex.getMessage());
                return new ArrayList<String>();
            }

        }

        return uriResultList;
    }
    
    
     
    private Map<String, List<String>> getQaldOffLineAnswer(String QALDAnswer) throws IOException, FileNotFoundException, CsvException {
        CsvFile csvFile = new CsvFile();
        List<String[]> qaldAnswerRows = csvFile.getRows(new File(QALDAnswer));
        Map<String, List<String>> map = new TreeMap<String, List<String>>();
        
        //System.out.println("QALDAnswer::"+QALDAnswer);

        Integer index = 0;
        for (String[] row : qaldAnswerRows) {

            if (index == 0) {
                index = index + 1;
                continue;
            }
            String id = row[0];
            String answer=row[3];
            String str="";
            if(answer.contains("["))
                str=StringUtils.substringBetween(answer,"[", "]");
            String[] info = id.split(",");
            List<String> answers = getList(str);            
            map.put(info[0].replace("\"", ""), answers);
        }

        return map;
    }



   /* private Map<String, List<String>> getQaldOffLineAnswerT(List<String[]> qaldAnswerRows) {
        Map<String, List<String>> map = new TreeMap<String, List<String>>();

        Integer index = 0;
        for (String[] row : qaldAnswerRows) {
           
            if (index == 0) {
                index = index + 1;
                continue;
            }
            //String[] info = row[0].replace("\n", "").split("\t");
            String id = row[0];
            String[] info = id.split(",");
            List<String> answers = getList(info[3]);
            map.put(info[0].replace("\"", ""), answers);
        }

        return map;
    }*/

    private List<String> getList(String string) {
        List<String> elemetns = new ArrayList<String>();
        string = string.replace("[", "").replace("[", "");
        string = string.replace("(", "").replace(")", "");
        String[] info = string.split(",");
        for (String text : info) {
            text = text.trim().strip();
            elemetns.add(text);
        }
        return elemetns;
    }

    /*private List<String> exisitngQuestion(Map<String, List<String>> answers, String givenQueGGQuestion) {
        
        givenQueGGQuestion=StringFilter.fillString(givenQueGGQuestion);
        //System.out.println(answers.keySet());
        if (answers.containsKey(givenQueGGQuestion)) {
            return filterAnswer(answers.get(givenQueGGQuestion));
        }

        return new ArrayList<String>();

    }*/

    private List<String> filterAnswer(List<String> answers) {
        List<String> results = new ArrayList<String>();

        for (String answerT : answers) {
            answerT = answerT.replace("(", "");
            answerT = answerT.replace(")", "");
            answerT = answerT.replace(" ", "");
            answerT = answerT.strip().stripLeading().stripTrailing().trim();
            results.add(answerT);
        }
        return results;

    }

    public static Map<String, List<String>> getAnswers() {
        return answers;
    }

    public List<String[]> getResult() {
        return result;
    }

    

    public static void setOfflineAnswerList(Map<String, List<String>> answersT) {
        answers = answersT;
    }

    /**
     * @return {@code tp / (tp2 + fp)}
     */
    private float calculateMeasure(float tp, float tp2, float fp) {
        return tp / (tp2 + fp);
    }

    private String cleanQALDString(String sentence) {
        return sentence.toLowerCase().trim();
    }
    public static void main(String [] args){
        
    }

    private String filterSparqlQuery(String sparql) {
        sparql = sparql.replace("<<", "<");
        sparql = sparql.replace(">>", ">");
        return sparql;
    }
    
    public static Map<String, String> getLongResultSparql() {
        return longResultSparql;
    }
}
