/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.linkeddatafragments.evaluation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.File;
import java.io.IOException;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.fest.util.Arrays;
import static org.linkeddatafragments.main.Constants.FIND_QALD_QUEGG_ANSWER;
import org.linkeddatafragments.util.io.FileUtils;
import org.linkeddatafragments.util.io.JsonAccess;

/**
 *
 * @author elahi
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfflineQueGGAnswers {

    @JsonProperty("answers")
    private Map<String, List<String>> answers = new TreeMap<String, List<String>>();

    public OfflineQueGGAnswers() {

    }

    public OfflineQueGGAnswers(Map<String, List<String>> answersT) {
       this.answers=answersT;
    }

    public Map<String, List<String>> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, List<String>> answers) {
        this.answers = answers;
    }
    
    public static List<String> exisitngQuestion(Map<String, List<String>> answers, String givenQueGGQuestion) {
        /*if (givenQueGGQuestion.contains("Sweden")) {
            System.out.println(givenQueGGQuestion);
        }*/
        givenQueGGQuestion=StringFilter.fillString(givenQueGGQuestion);
        //System.out.println(answers.keySet());
        if (answers.containsKey(givenQueGGQuestion)) {
            return filterAnswer(answers.get(givenQueGGQuestion));
        }

        return new ArrayList<String>();

    }
    
    private static List<String> filterAnswer(List<String> answers) {
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


    @Override
    public String toString() {
        return "QueGGAnswers{" + "answers=" + answers + '}';
    }
    
    
    
     public static void main(String []args) throws IOException{
        System.out.println("Hello World");
        String inputFile="src/main/resources/?s-dbo:birthPlace-res:Sweden.txt";
        List<String> list=FileUtils.FileToSetOrginal(inputFile);
        String outputFile="/media/elahi/Elements/A-project/LDK2023/Client.Java/output/en/QueGG-Answer.json";
        Map<String, List<String>> answers = new TreeMap<String, List<String>>();
        String givenQueGGQuestion="Give me all professional  Sweden";
        givenQueGGQuestion=StringFilter.fillString(givenQueGGQuestion);
        answers.put(givenQueGGQuestion, list);
        
    
        inputFile="src/main/resources/canada.txt";
        list=FileUtils.FileToList(inputFile);
        System.out.println(list.size()+" "+list);
        answers.put("inwhichcityarethecanada?", list);
        answers.put("whatcityiscanadain?", list);
        answers.put("whatisthesiteofcanada", list);
        String sparql="select  ?s    {   ?s <http://dbpedia.org/ontology/location>  <http://dbpedia.org/resource/Canada> .   ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://dbpedia.org/ontology/Country> .    }";
        sparql=sparql.replace(" ", "");
        answers.put(sparql, list);
        list=new ArrayList<String>();
        list.add("http://dbpedia.org/resource/Pokhara");
        list.add("http://dbpedia.org/resource/Siddharthanagar");
        list.add("http://dbpedia.org/resource/Biratnagar");
        answers.put("inwhichairportsdoesyetiairlinesfound?", list);
        answers.put("whichairportsdoesyetiairlinesserve?", list);
        answers.put("whichairportsheadquarteredyetiairlines?",list);
        
        list=new ArrayList<String>();
        list.add("http://dbpedia.org/resource/Seoul_National_Cemetery");
        answers.put("wherewassyngmanrheeburied?",list);
        
        list=new ArrayList<String>();
        list.add("http://dbpedia.org/resource/Belgrade");
        answers.put("inwhichcitydidpresidentofmontenegroborn?",list);
        
        list=new ArrayList<String>();
        list.add("http://dbpedia.org/resource/Canada");
        
         answers.put("inwhichcountryisthelimericklake?",list);
         
         list=new ArrayList<String>();
        list.add("http://dbpedia.org/resource/Saudi_Arabia");
        
         answers.put("inwhichcountryisthemecca?",list);
         answers.put("whatcountryismeccain?",list);
         
          list=new ArrayList<String>();
        list.add("http://dbpedia.org/resource/DBpedia");
           answers.put("whowasuniversityofmannheim'sunitedstates?",list);
           
           answers.put("whoisthememberofnetherlands",new ArrayList<String>());
           
            list=new ArrayList<String>();
            list.add("http://dbpedia.org/resource/Palace_of_Versailles");
           answers.put("whereisfranceproduced?",new ArrayList<String>());
           
            list=new ArrayList<String>();
            String queGGQuestion=StringFilter.fillString("In what city is Heineken International located?");
            list.add("http://dbpedia.org/resource/Amsterdam");
            answers.put(queGGQuestion,list);
           
        
        
        /*String q2="When did Diana die?"; 
        list=new ArrayList<String>();
        list.add("1997-08-31");
        answers.put(q2, list);*/
              
        OfflineQueGGAnswers offlineQueGGAnswers=new OfflineQueGGAnswers(answers);
        JsonAccess.writeObjectJson(outputFile, offlineQueGGAnswers);
        System.out.println(answers);
       
        
        
    }

}
