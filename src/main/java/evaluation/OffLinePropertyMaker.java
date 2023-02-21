/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import util.io.FileUtils;
import static util.io.FileUtils.stringToFile;

/**
 *
 * @author elahi
 */
public class OffLinePropertyMaker {

    private String menu = null;
    private String endpoint = null;

    public OffLinePropertyMaker(String menu, String endpoint) {
        this.menu = menu;
        this.endpoint = endpoint;
    }

    public void make(QALD qaldFile, String languageCode, Boolean flag, Set<Integer> ids) throws Exception {
        Map<String, List<String>> propertyTripes = new TreeMap<String, List<String>>();
        Integer index = 0;
        Integer total = qaldFile.questions.size();
        for (QALD.QALDQuestions qaldQuestions : qaldFile.questions) {
            List<String> qualResults = new ArrayList<String>();
            String qaldQuestion = QALDImporter.getQaldQuestionString(qaldQuestions, languageCode);
            //System.out.println(qaldQuestion);

            String qaldSparqlQuery = QALDImporter.getQaldSparqlQuery(qaldQuestions);
            index = index + 1;
            String id = qaldQuestions.id;
            EntryComparison entryComparison = new EntryComparison();

            if (flag) {
                qualResults = SparqlExecution.getSparqlQuery(menu, endpoint, id, qaldQuestion, qaldSparqlQuery, true);
                System.out.println(id + " " + total);
                qaldSparqlQuery = qaldSparqlQuery.replace("\n", "");
                String where = this.filterString(StringUtils.substringBetween(qaldSparqlQuery, "{", "}"));
                String[] triple = where.split(" ");
                if (triple.length >= 3) {
                    String subj = this.addBracket(this.modifyEntityToUri(filterString(triple[0])));
                    String property = this.modifyProperty(filterString(triple[1]));
                    String obj = this.addBracket(this.modifyEntityToUri(filterString(triple[2])));
                    String tripleStr = null;
                    List<String> triples = new ArrayList<String>();

                    if (this.answerType(subj)) {
                        for (String answer : qualResults) {
                            answer=filterString(StringUtils.substringBetween(answer, "(", ")"));
                            tripleStr = answer + "=" + answer + "=" + obj + "=" + obj;
                            triples.add(tripleStr);
                        }
                    } else {
                        for (String answer : qualResults) {
                            answer=filterString(StringUtils.substringBetween(answer, "(", ")"));
                            tripleStr = subj + "=" + subj + "=" + answer + "=" + answer;
                            triples.add(tripleStr);
                        }
                    }
                    
                    List<String> oldTriples = new ArrayList<String>();
                    if (propertyTripes.containsKey(property)) {
                        oldTriples = propertyTripes.get(property);
                        oldTriples.addAll(triples);
                        propertyTripes.put(property, triples);
                    } else {
                        oldTriples.addAll(triples);
                        propertyTripes.put(property, triples);
                    }
                   

                }

            }
            break;

        }
        
       this.mapToFile(propertyTripes, "../resources/en/property/");
    }
    
    private void mapToFile(Map<String, List<String>> propertyTripes, String dir) throws IOException {
        String str = "";
        for (String property : propertyTripes.keySet()) {
            System.out.println(property);
            String fileName = dir + modifyEntityToSlash(property) + ".txt";
            List<String> list = propertyTripes.get(property);
            for (String triple : list) {
                str+=triple+"\n";
            }
            stringToFile(str, fileName);
        }

    }


    private String modifyEntityToUri(String reference) {
        reference = reference.replace("dbo:", "http://dbpedia.org/ontology/");
        reference = reference.replace("dbp:", "http://dbpedia.org/property/");
        reference = reference.replace("res:", "http://dbpedia.org/resource/");
        reference = reference.replace("dbr:", "http://dbpedia.org/resource/");
        reference = reference.replace("xsd:", "http://www.w3.org/2001/XMLSchema#");
        
        return reference;
    }

    private String modifyEntityToShort(String reference) {
        reference = reference.replace("http://dbpedia.org/ontology/", "dbo:");
        reference = reference.replace("http://dbpedia.org/property/", "dbp:");
        reference = reference.replace("http://dbpedia.org/resource/", "res:");
        reference = reference.replace("http://dbpedia.org/resource/", "dbr:");
        reference = reference.replace("http://www.w3.org/2001/XMLSchema#", "xsd:");
        return reference;
    }
     private String modifyEntityToSlash(String reference) {
        reference = reference.replace(":", "_");
        return reference;
    }

    private String addBracket(String entity) {
        return "<" + entity + ">";
    }

    private String modifyProperty(String string) {
        if (string.contains(":")) {
            return string;
        } else {
            return modifyEntityToShort(string);
        }

    }

    private boolean answerType(String subj) {
        if (subj.contains("?")) {
            return true;
        }
        return false;
    }
    private String filterString(String subj) {
         return subj.strip().trim().stripLeading().stripTrailing();
    }
   

}
