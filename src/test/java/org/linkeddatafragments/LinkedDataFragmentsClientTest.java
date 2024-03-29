package org.linkeddatafragments;

import com.google.common.base.Stopwatch;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Before;
import org.junit.Test;
import org.linkeddatafragments.model.LinkedDataFragmentGraph;
import org.linkeddatafragments.util.LDFTestUtils;

import java.util.Iterator;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import org.junit.Ignore;

public class LinkedDataFragmentsClientTest {
    protected Model model;

    @Before
    public void setUp() {
        LinkedDataFragmentGraph ldfg = new LinkedDataFragmentGraph("http://data.linkeddatafragments.org/dbpedia2014");
        model = ModelFactory.createModelForGraph(ldfg);
    }

    @Ignore
    public void testSize() {
        assertThat(model.size()).isEqualTo(377367913);
        System.out.println(model.size());
    }

    @Ignore
    public void testBasicSparql() {
        String queryString = "SELECT ?p ?o WHERE { <http://dbpedia.org/resource/Barack_Obama> ?p ?o }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }

        assertThat(rs.getRowNumber()).isGreaterThan(0);
    }

    @Ignore
    public void testTypeSparql() {
        String queryString = "SELECT ?o WHERE { <http://dbpedia.org/resource/Barack_Obama> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }
        System.out.println("rs.getRowNumber() = " + rs.getRowNumber());
        assertThat(rs.getRowNumber()).isGreaterThan(0);
    }

    @Ignore
    public void testLiteralSparql() {

        String queryString = "SELECT (COUNT(?o) AS ?count) WHERE { ?o <http://www.w3.org/2000/01/rdf-schema#label> \"Belgium\"@en }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();
        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }

        assertThat(rs.getRowNumber()).isGreaterThan(0);

    }

    @Ignore
    public void testSingleJoinSparql() {

        String queryString = "SELECT ?o ?n WHERE { <http://dbpedia.org/resource/Barack_Obama> <http://dbpedia.org/ontology/almaMater> ?o " +
                ". ?o <http://dbpedia.org/ontology/state> ?n }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();

        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }

        assertThat(rs.getRowNumber()).isGreaterThan(0);

    }

    @Ignore
    public void testSingleCountPredicateSparql() {

        String queryString = "SELECT (COUNT(DISTINCT ?p) AS ?count) where { ?o ?p <http://dbpedia.org/resource/Barack_Obama> }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();

        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }

        assertThat(rs.getRowNumber()).isGreaterThan(0);

    }

    @Ignore
    public void testSingleCountSubjectSparql() {

        String queryString = "SELECT (COUNT(DISTINCT ?o) AS ?count) where { ?o ?p <http://dbpedia.org/resource/Barack_Obama> }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();

        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }

        assertThat(rs.getRowNumber()).isGreaterThan(0);

    }

    @Ignore
    public void testSingleCountObjectSparql() {

        String queryString = "SELECT (COUNT(DISTINCT ?o) AS ?count) where { <http://dbpedia.org/resource/Barack_Obama> ?p ?o }";
        Query qry = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(qry, model);
        ResultSet rs = qe.execSelect();

        while(rs.hasNext()) {
            System.out.println(rs.nextSolution().toString());
        }

        assertThat(rs.getRowNumber()).isGreaterThan(0);

    }



    @Ignore
    public void testSparqlQueries() {

        List<String> queries = LDFTestUtils.readFiles(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        String prefixes = "" +
                "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> " +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                "PREFIX dbpedia: <http://dbpedia.org/resource/> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
        for(String query : queries) {

            String queryString = prefixes + query;
            System.out.println(queryString);
            Query qry = QueryFactory.create(queryString);
            Stopwatch sw = Stopwatch.createStarted();
            QueryExecution qe = QueryExecutionFactory.create(qry, model);
            if(qe.getQuery().getQueryType() == Query.QueryTypeConstruct) {
                Iterator<Triple> rm = qe.execConstructTriples();

                assertThat(rm.hasNext()).isTrue();

                while(rm.hasNext()) {
                    System.out.println(rm.next().toString());
                }

            } else if(qe.getQuery().getQueryType() == Query.QueryTypeSelect) {
                ResultSet rs = qe.execSelect();

                while (rs.hasNext()) {
                    System.out.println(rs.nextSolution().toString());
                }

                assertThat(rs.getRowNumber()).isGreaterThan(0);
            }
            System.out.println(sw.stop());
        }


    }
}
