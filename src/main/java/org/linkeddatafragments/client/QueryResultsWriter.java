package org.linkeddatafragments.client;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import org.linkeddatafragments.model.LinkedDataFragmentGraph;
import org.linkeddatafragments.utils.Config;

/**
 * A QueryResultsWriter executes a query on a graph and writes its results to a stream.
 * @author Laurens De Vocht
 * @author Ruben Verborgh
 */
public class QueryResultsWriter {
    private final Model model;
    private final Query query;

    /**
     * Creates a new QueryResultsWriter.
     * @param model The model to query.
     * @param query The query to execute.
     */
    public QueryResultsWriter(final Model model, final Query query) {
        this.model = model;
        this.query = query;
    }

    /**
     * Write the results of the query to the stream.
     * @param outputStream The output stream.
     */
    public void writeResults(final PrintStream outputStream) {
        final QueryExecution executor = QueryExecutionFactory.create(query, model);
        switch (query.getQueryType()) {
        case Query.QueryTypeSelect:
            final ResultSet rs = executor.execSelect();
            while (rs.hasNext())
                outputStream.println(rs.next());
            break;
        case Query.QueryTypeAsk:
            outputStream.println(executor.execAsk());
            break;
        case Query.QueryTypeConstruct:
        case Query.QueryTypeDescribe:
            final Iterator<Triple> triples = executor.execConstructTriples();
            while (triples.hasNext())
                outputStream.println(triples.next());
            break;
        default:
            throw new Error("Unsupported query type");
        }
    }

    /**
     * Starts a standalone version of the results writer.
     * @param args The command-line arguments.
     */
    public static void main(final String[] args) {
       
        // Verify arguments
        if (args.length < 1 || args.length > 2 || args[0].matches("/^--?h(elp)?$/"))
            error("usage: java -jar ldf-client.jar [config.json] query");
        final boolean hasConfig = args.length >= 2;

        // Read the configuration
         File configFile = hasConfig ? new File(args[0]) : new File("./config-default.json");
        
        configFile =new File("src/main/conf/config-default.json");
        JsonReader configReader;
        try {
            configReader = new JsonReader(new FileReader(configFile));
        } catch (FileNotFoundException e) {
            error("Config file could not be found.");
            return;
        }
        final Config config = new Gson().fromJson(configReader, Config.class);

        // Read the query
        //final String queryFilePath = hasConfig ? args[1] : args[0];
        String queryText;
        //try {
            //queryText = new String(Files.readAllBytes(Paths.get(queryFilePath)), StandardCharsets.UTF_8);
            queryText="select  ?o    {    <http://dbpedia.org/resource/Australia> <http://dbpedia.org/ontology/capital>  ?o .   ?o <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://dbpedia.org/ontology/City> .    }";
        //} catch (IOException e) {
          //  error("Query file could not be read.");
          //  return;
        //}

        // Query prefixes must be applied before the query is parsed.
        final Query query = QueryFactory.create();
        PrefixMapping pm = PrefixMapping.Factory.create();
        pm.setNsPrefixes(config.prefixes);
        query.setPrefixMapping(pm);
        QueryFactory.parse(query, queryText, null, Syntax.syntaxSPARQL);

        // Execute the query over the graph and write its results
        final LinkedDataFragmentGraph graph = new LinkedDataFragmentGraph(config.datasource);
        new QueryResultsWriter(ModelFactory.createModelForGraph(graph), query).writeResults(System.out);
        System.exit(0);
    }

    /**
     * Writes the error message and exits the application.
     * @param message The error message.
     */
    private static void error(String message) {
        System.err.println(message);
        System.exit(1);
    }
}
