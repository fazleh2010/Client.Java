/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.linkeddatafragments.client;

import com.google.common.base.Stopwatch;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import static org.fest.assertions.Assertions.assertThat;
import org.linkeddatafragments.model.LinkedDataFragmentGraph;

/**
 *
 * @author elahi
 */
public class LinkedDataFragmentSpql {

    private static LinkedDataFragmentGraph ldfg = new LinkedDataFragmentGraph("http://data.linkeddatafragments.org/dbpedia2014");
    protected static Model model = ModelFactory.createModelForGraph(ldfg);

    public static void main(String[] args) {
        String queryString = "Select ?s ?o { ?s <http://dbpedia.org/ontology/birthPlace> ?o }";

        LinkedDataFragmentSpql LinkedDataFragmentSpql = new LinkedDataFragmentSpql(queryString);
    }

    public LinkedDataFragmentSpql(String queryString) {
        List<String> results = this.sparqlObjectAsVariable(queryString);
        List<String> parsedResults = this.parseResultList(results);
        System.out.println("results::" + results);
        System.out.println("parsedResults::" + parsedResults);
       

    }

    public LinkedDataFragmentSpql(Model modelT, String endpoint, String queryString) {
        model = modelT;
        this.sparqlObjectAsVariable(queryString);
    }

    public List<String> sparqlObjectAsVariable(String queryString) {
        List<String> results = new ArrayList<String>();
         System.out.println("queryString::" + queryString);
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();
        while (rs.hasNext()) {
            String result = rs.nextSolution().toString();
            results.add(result);
        }
        return results;
    }

    public List<String> parseResultList(List<String> results) {

        List<String> parsedResult = new ArrayList<String>();
        for (String result : results) {
            if (result.contains(",")) {
                String[] info = result.split(",\\s*"); // split on commas
                for (String value : info) {
                    if (value.contains("=")) {
                        String[] info1 = value.split("=");
                        value = info1[1];
                         if(value.contains("<")){
                          value = value.replace("<", "");
                          value = value.replace(">", "");
                        }
                        value = "("+value.strip().trim();
                        parsedResult.add(value);
                    }
                }
            }
            else{
                 String  value = result;
                 if (value.contains("=")) {
                        String[] info1 = value.split("=");
                        value = info1[1];
                        if(value.contains("<")){
                          value = value.replace("<", "");
                          value = value.replace(">", "");
                        }
                         value =  "("+value.strip().trim();
                          parsedResult.add(value);
                       
                    }
                
            }
        }
        
      

        return parsedResult;
    }

}
