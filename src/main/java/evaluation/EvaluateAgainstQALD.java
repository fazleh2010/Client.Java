package evaluation;

import java.io.File;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.lang.SPARQLParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.isNull;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.linkeddatafragments.client.LinkedDataFragmentSpql;
import org.linkeddatafragments.model.LinkedDataFragmentGraph;
import util.io.CsvFile;

public class EvaluateAgainstQALD {

    private static final Logger LOG = LogManager.getLogger(EvaluateAgainstQALD.class);
    private final String language;
    private final String ORIGINAL = "ORIGINAL";
    private final String BOG = "BOG";
    public static final String PROTOTYPE_QUESTION = "PROTOTYPE_QUESTION";
    public static final String REAL_QUESTION = "REAL_QUESTION";
    private Set<String> qaldQuestions = new TreeSet<String>();
    private String endpoint = null;
    private static LinkedDataFragmentGraph ldfg = new LinkedDataFragmentGraph("http://data.linkeddatafragments.org/dbpedia2014");
    protected static Model model = ModelFactory.createModelForGraph(ldfg);    
    private Set<String> menu= new HashSet<String>();
    private String FIND_SIMILARITY_RESULT = null;
    private String evalutionFile = null;
    private String QALD_QUEGG_ANSWER_FILE = null;
    private String QALD_ANSWER_FILE = null;
    private static String FIND_QALD_ANSWER = "FIND_QALD_ANSWER";
    private static String FIND_SIMILARITY = "FIND_SIMILARITY";
    private static String FIND_QALD_QUEGG_ANSWER = "FIND_QALD_QeeGG_ANSWER";
    private static String EVALUTE_QALD_QUEGG = "EVALUTE_QALD_QUEGG";

    public EvaluateAgainstQALD(String language, String endpoint, Set<String> menu, String FIND_SIMILARITY_RESULT, String resultComparisonFile, String qaldAnswerFile, String qaldQueGGAnswerFile) {
        this.language = language;
        this.endpoint = endpoint;
        this.menu = menu;
        this.FIND_SIMILARITY_RESULT = FIND_SIMILARITY_RESULT;
        this.evalutionFile = resultComparisonFile;
        this.QALD_QUEGG_ANSWER_FILE = qaldQueGGAnswerFile;
        this.QALD_ANSWER_FILE = qaldAnswerFile;
        //if (this.endpoint.contains("http://data.linkeddatafragments.org")) {
        //   this.model = ModelFactory.createModelForGraph(new LinkedDataFragmentGraph(this.endpoint));   
        //}
    }

    public void evaluateAndOutput(Map<String, String[]> questions, String qaldOriginalFile, String qaldModifiedFile, String qaldRaw, String languageCode, String questionType, Double similarityMeasure) throws IOException, Exception {
        QALDImporter qaldImporter = new QALDImporter();
        EvaluationResult result = null;
        List<EntryComparison> entryComparisons = new ArrayList<EntryComparison>();
        qaldImporter.qaldToCSV(qaldOriginalFile, qaldRaw, languageCode);
        QALD qaldModified = qaldImporter.readQald(qaldModifiedFile);
        QALD qaldOriginal = qaldImporter.readQald(qaldOriginalFile);
        
        if (menu.contains(FIND_SIMILARITY)) {
            entryComparisons = getAllSentenceMatchesCsv(qaldModified, questions, languageCode, BOG, similarityMeasure);
            result = doEvaluation(qaldModified, entryComparisons, languageCode, questionType, false);
            Writer.writeResult(qaldImporter, qaldOriginal, result, this.FIND_SIMILARITY_RESULT, languageCode,FIND_SIMILARITY);
            System.out.println("FIND_SIMILARITY completed!!");
        }
        if (menu.contains(FIND_QALD_ANSWER)) {
            entryComparisons = getQaldAnswer(qaldModified,languageCode);
            result = doEvaluation(qaldModified, entryComparisons, languageCode, questionType, false);
            Writer.writeResult(qaldImporter, qaldOriginal, result, this.QALD_ANSWER_FILE, languageCode,FIND_QALD_ANSWER);
            System.out.println("FIND_QALD_ANSWER completed!!");
        }
        if (menu.contains(FIND_QALD_QUEGG_ANSWER)) {
            entryComparisons = getMatchedSentences(this.FIND_SIMILARITY_RESULT, this.QALD_ANSWER_FILE, this.QALD_QUEGG_ANSWER_FILE);
            System.out.println("FIND_QALD_QUEGG_ANSWER completed!!");

        }
        if (menu.contains(EVALUTE_QALD_QUEGG)) {
            entryComparisons = getMakeComaprisions(this.QALD_QUEGG_ANSWER_FILE);
            result = doEvaluation(qaldModified, entryComparisons, languageCode, questionType, false);
            Writer.writeResult(qaldImporter, qaldOriginal, result, this.evalutionFile, languageCode,EVALUTE_QALD_QUEGG);
        }

    }

    private EvaluationResult doEvaluation(QALD qaldFile, List<EntryComparison> entryComparisons, String languageCode, String questionType, Boolean flag) {
        EvaluationResult evaluationResult = new EvaluationResult();
        Integer index = 0;
        for (EntryComparison entryComparison : entryComparisons) {
             System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!Start!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
             System.out.println("qald id: "+entryComparison.getQaldEntry().getId());
             System.out.println((index)+"  QaldEntry::::"+entryComparison.getQaldEntry().getQuestions()+" "+"QaldEntry::::"+entryComparison.getQaldEntry().getSparql());
             System.out.println((index)+"   QueGGEntry::::"+entryComparison.getQueGGEntry().getQuestions()+" "+entryComparison.getQueGGEntry().getSparql());
             System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!End!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            if (questionType.contains(REAL_QUESTION)) {
                realQuestionComparision(entryComparison, flag);
            }
            evaluationResult.getEntryComparisons().add(entryComparison);
            LOG.info("tp: {}, fp: {}, fn: {}", entryComparison.getTp(), entryComparison.getFp(), entryComparison.getFn());
            evaluationResult.setTp_global(evaluationResult.getTp_global() + entryComparison.getTp());
            evaluationResult.setFp_global(evaluationResult.getFp_global() + entryComparison.getFp());
            evaluationResult.setFn_global(evaluationResult.getFn_global() + entryComparison.getFn());
        }

        evaluationResult.setPrecision_global(calculateMeasure(
                evaluationResult.getTp_global(),
                evaluationResult.getTp_global(),
                evaluationResult
                        .getFp_global()
        ));
        evaluationResult.setRecall_global(calculateMeasure(
                evaluationResult.getTp_global(),
                evaluationResult.getTp_global(),
                evaluationResult
                        .getFn_global()
        ));
        evaluationResult.setF_measure_global(
                (2
                * (calculateMeasure(
                        evaluationResult.getPrecision_global() * evaluationResult.getRecall_global(),
                        evaluationResult.getPrecision_global(),
                        evaluationResult.getRecall_global()
                )))
        );

        LOG.info("-".repeat(50));
        LOG.info(
                "TP_GLOBAL: {}, FP_GLOBAL: {}, FN_GLOBAL: {}",
                evaluationResult.getTp_global(),
                evaluationResult.getFp_global(),
                evaluationResult.getFn_global()
        );
        LOG.info(
                "PRECISION_GLOBAL: {}, RECALL_GLOBAL: {}, F_MEASURE_GLOBAL: {}",
                evaluationResult.getPrecision_global(),
                evaluationResult.getRecall_global(),
                evaluationResult.getF_measure_global()
        );
        /*System.out.println("getTp_global::" + evaluationResult.getTp_global());
        System.out.println("evaluationResult::" + evaluationResult.getFp_global());
        System.out.println("evaluationResult::" + evaluationResult.getFn_global());
        System.out.println("getPrecision_global()::" + evaluationResult.getPrecision_global());
        System.out.println("getRecall_global()()::" + evaluationResult.getRecall_global());
        System.out.println("getRecall_global()()::" + evaluationResult.getF_measure_global());*/
        return evaluationResult;
    }
    
    
    private EvaluationResult doEvaluation( List<EntryComparison> entryComparisons) {
        EvaluationResult evaluationResult = new EvaluationResult();
        Integer index = 0;
        for (EntryComparison entryComparison : entryComparisons) {
             System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!Start!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
             System.out.println("qald id: "+entryComparison.getQaldEntry().getId());
             System.out.println((index)+"  QaldEntry::::"+entryComparison.getQaldEntry().getQuestions()+" "+"QaldEntry::::"+entryComparison.getQaldEntry().getSparql());
             System.out.println((index)+"   QueGGEntry::::"+entryComparison.getQueGGEntry().getQuestions()+" "+entryComparison.getQueGGEntry().getSparql());
             System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!End!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

      
            evaluationResult.getEntryComparisons().add(entryComparison);
            evaluationResult.setTp_global(evaluationResult.getTp_global() + entryComparison.getTp());
            evaluationResult.setFp_global(evaluationResult.getFp_global() + entryComparison.getFp());
            evaluationResult.setFn_global(evaluationResult.getFn_global() + entryComparison.getFn());
        }

        evaluationResult.setPrecision_global(calculateMeasure(
                evaluationResult.getTp_global(),
                evaluationResult.getTp_global(),
                evaluationResult
                        .getFp_global()
        ));
        evaluationResult.setRecall_global(calculateMeasure(
                evaluationResult.getTp_global(),
                evaluationResult.getTp_global(),
                evaluationResult
                        .getFn_global()
        ));
        evaluationResult.setF_measure_global(
                (2
                * (calculateMeasure(
                        evaluationResult.getPrecision_global() * evaluationResult.getRecall_global(),
                        evaluationResult.getPrecision_global(),
                        evaluationResult.getRecall_global()
                )))
        );

        LOG.info("-".repeat(50));
        LOG.info(
                "TP_GLOBAL: {}, FP_GLOBAL: {}, FN_GLOBAL: {}",
                evaluationResult.getTp_global(),
                evaluationResult.getFp_global(),
                evaluationResult.getFn_global()
        );
        LOG.info(
                "PRECISION_GLOBAL: {}, RECALL_GLOBAL: {}, F_MEASURE_GLOBAL: {}",
                evaluationResult.getPrecision_global(),
                evaluationResult.getRecall_global(),
                evaluationResult.getF_measure_global()
        );
        /*System.out.println("getTp_global::" + evaluationResult.getTp_global());
        System.out.println("evaluationResult::" + evaluationResult.getFp_global());
        System.out.println("evaluationResult::" + evaluationResult.getFn_global());
        System.out.println("getPrecision_global()::" + evaluationResult.getPrecision_global());
        System.out.println("getRecall_global()()::" + evaluationResult.getRecall_global());
        System.out.println("getRecall_global()()::" + evaluationResult.getF_measure_global());*/
        return evaluationResult;
    }


    private List<EntryComparison> getAllSentenceMatchesCsv(QALD qaldFile, Map<String, String[]> questions, String languageCode, String questionType, double similarityPercentage) throws Exception {
        List<String> qaldSentences
                = qaldFile.questions
                        .stream().parallel()
                        .map(qaldQuestions -> qaldQuestions.question)
                        .flatMap(qaldQuestions1
                                -> qaldQuestions1.stream().parallel()
                                .filter(qaldQuestion -> qaldQuestion.language.equals(languageCode))
                                .map(qaldQuestion -> qaldQuestion.string))
                        .collect(Collectors.toList());
        return this.getMatchRealQuestion(qaldFile, questions, languageCode, similarityPercentage);
    }

    private void realQuestionComparision(EntryComparison entryComparison, Boolean flag) {
        String id = entryComparison.getQaldEntry().getId();
        String qaldQuestion = entryComparison.getQaldEntry().getQuestions();
        String queGGQuestion = entryComparison.getQueGGEntry().getQuestions();
        String cleanQaldQuestion = cleanQALDString(qaldQuestion); //  make lower case
        String qaldSparql = entryComparison.getQaldEntry().getSparql();
        String queGGSparql = !isNull(entryComparison.getQueGGEntry()) ? entryComparison.getQueGGEntry().getSparql() : "";
        Query qaldPARQLQuery = new Query();
        SPARQLParser.createParser(Syntax.syntaxSPARQL_11).parse(qaldPARQLQuery, qaldSparql);
        List<String> uriResultListQueGG = new ArrayList<String>();
        List<String> uriResultListQALD;

        uriResultListQALD = this.getSparqlQuery(qaldSparql, flag, entryComparison.getQaldEntry().getResultList());

        entryComparison.setQaldResults(uriResultListQALD);

        if (queGGSparql != null) {
            queGGSparql = queGGSparql.replace("{", "\n" + "{" + "\n");
            queGGSparql = queGGSparql.replace("}", "\n" + "}");
            queGGSparql = queGGSparql.replace("\"", "");
            uriResultListQueGG = this.getSparqlQuery(queGGSparql, flag, entryComparison.getQueGGEntry().getResultList());
            entryComparison.getQueGGEntry().setSparql(queGGSparql);
            entryComparison.setQueGGResults(uriResultListQueGG);

            /*System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("id::" + id);
            System.out.println("qaldQ::" + qaldQuestion);
            System.out.println("qaldSparql::" + qaldSparql);
            System.out.println("resultQALD::" + uriResultListQALD);
            System.out.println("queGGQu::" + queGGQuestion);
            System.out.println("queGGSparql::" + queGGSparql);
            System.out.println("resultQueGG::" + uriResultListQueGG);*/
        }

        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("id::" + id);
        System.out.println("qaldQ::" + qaldQuestion);
        System.out.println("qaldSparql::" + qaldSparql);
        System.out.println("resultQALD::" + uriResultListQALD);
        System.out.println("queGGQu::" + queGGQuestion);
        System.out.println("queGGSparql::" + queGGSparql);
        System.out.println("resultQueGG::" + uriResultListQueGG);

        LOG.debug(
                "Comparing QueGG results to QALD results: #QueGG: {}, #QALD: {}",
                uriResultListQueGG.size(),
                uriResultListQALD.size()
        );
        LOG.debug("Comparing QueGG results to QALD results: QueGG: {}, QALD: {}", uriResultListQueGG, uriResultListQALD);

        List<String> finalUriResultListQueGG = uriResultListQueGG;

        // Add TP, FP, FN
        if (uriResultListQALD.isEmpty() && uriResultListQueGG.isEmpty()) {
            entryComparison.setTp(0);
            entryComparison.addFp(0);
            entryComparison.setFn(0);
        } else {
            entryComparison.setTp(uriResultListQueGG.stream().filter(uriResultListQALD::contains).count());
            entryComparison.addFp(uriResultListQueGG.stream()
                    .filter(resultQueGG -> !uriResultListQALD.contains(resultQueGG))
                    .count());

            entryComparison.setFn(uriResultListQALD.stream()
                    .filter(resultQald -> !finalUriResultListQueGG.contains(resultQald))
                    .count());
        }

        // Add Precision, Recall, F-measure
        if ((entryComparison.getTp() + entryComparison.getFp()) > 0) {
            entryComparison.setPrecision(calculateMeasure(
                    entryComparison.getTp(),
                    entryComparison.getTp(),
                    entryComparison.getFp()
            ));
        }
        if ((entryComparison.getTp() + entryComparison.getFn()) > 0) {
            entryComparison.setRecall((calculateMeasure(
                    entryComparison.getTp(),
                    entryComparison.getTp(),
                    entryComparison.getFn()
            )));
        }
        if ((entryComparison.getPrecision() + entryComparison.getRecall()) > 0) {
            entryComparison.setF_measure(
                    (2
                    * (calculateMeasure(
                            entryComparison.getPrecision() * entryComparison.getRecall(),
                            entryComparison.getPrecision(),
                            entryComparison.getRecall()
                    )))
            );
        }
        LOG.debug("tp: {}, fp: {}, fn: {}", entryComparison.getTp(), entryComparison.getFp(), entryComparison.getFn());
        LOG.debug(
                "Precision: {}, Recall: {}, F-measure: {}",
                entryComparison.getPrecision(),
                entryComparison.getRecall(),
                entryComparison.getF_measure()
        );

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

    /**
     * Make lower case, add regex capture for $x and (... | ...)
     */
    /*protected String cleanString(String sentence) {
        return String.format("^%s$", sentence
                .replace(DEFAULT_BINDING_VARIABLE, "([\\w\\s\\d-,.']+)")
                .replaceAll("\\((.+)\\s\\|\\s(.+)\\)", "([\\\\w\\\\s\\\\d-,.']+)")
                .replace("?", "\\?")
                .toLowerCase()
                .trim());
    }*/
    private List<EntryComparison> sortMatches(List<EntryComparison> matchingEntries) {
        return matchingEntries.stream().parallel()
                .sorted(Comparator.comparing(
                        entryComparison -> Integer.valueOf(entryComparison.getQaldEntry().getId())
                ))
                .collect(Collectors.toList());
    }

    private List<EntryComparison> getMatchRealQuestion(QALD qaldFile, Map<String, String[]> realQuestions, String languageCode, double similarityPercentage) throws Exception {
        List<EntryComparison> entryComparisons = new ArrayList<EntryComparison>();
        //List<String> list = new ArrayList<String>();
        Integer index = 0;
        for (QALD.QALDQuestions qaldQuestions : qaldFile.questions) {
            String qaldQuestion = QALDImporter.getQaldQuestionString(qaldQuestions, languageCode);
            String qaldSparqlQuery = QALDImporter.getQaldSparqlQuery(qaldQuestions);

            Map<String, QueGGinfomation> grammarEntities = this.matchedRealQuestions(qaldQuestion, qaldSparqlQuery, realQuestions, similarityPercentage, index);
            StringSimilarity stringSimilarity = new StringSimilarity();
            index = index + 1;
            /*if(stringSimilarity.isAskSparqlQuery(qaldSparqlQuery)){
                grammarEntities =new TreeMap<String,QueGGinfomation>();  
            }*/
 /*else if(!stringSimilarity.isMultipleSparqlQuery(qaldSparqlQuery)){
                //System.out.println("qaldSparqlQuery::"+qaldSparqlQuery);
                //exit(1);
                grammarEntities =new TreeMap<String,QueGGinfomation>();  
            }*/

            EntryComparison entryComparison = new EntryComparison();
            String qaldSparql = qaldQuestions.query.sparql;
            Entry qaldEntry = new Entry();
            Entry queGGEntry = new Entry();
            qaldEntry.setActualEntry(qaldQuestions);
            qaldEntry.setId(qaldQuestions.id);
            qaldEntry.setQuestions(qaldQuestion);
            qaldEntry.setSparql(qaldSparql);

            if (!grammarEntities.isEmpty()) {
                // StringSimilarity stringSimilarity=new StringSimilarity();
                QueGGinfomation queGGinfomation = stringSimilarity.getMostSimilarMatch(grammarEntities);
                queGGEntry.setId(queGGinfomation.getId());
                queGGEntry.setQuestions(queGGinfomation.getQuestion());
                List<String> list = new ArrayList<String>();
                list.add(queGGinfomation.getQuestion());
                queGGEntry.setQuestionList(list);
                queGGEntry.setSparql(queGGinfomation.getSparqlQuery());
                entryComparison.setMatchedFlag(Boolean.TRUE);
                entryComparison.setSimilarityCsore(queGGinfomation.getValue());
            } else {
                entryComparison.setMatchedFlag(Boolean.FALSE);
                entryComparison.setSimilarityCsore(0.0);
            }

            entryComparison.setQaldEntry(qaldEntry);
            entryComparison.setQueGGEntry(queGGEntry);
            entryComparisons.add(entryComparison);

            if (entryComparison.getMatchedFlag()) {
                //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                //System.out.println("qald::::" + entryComparison.getQaldEntry().getQuestions());
                //System.out.println("queGG::::" + entryComparison.getQueGGEntry().getQuestions());
                //exit(1);
            }

            //}
        }
        return entryComparisons;
    }
  
    private List<EntryComparison> getQaldAnswer(QALD qaldFile, String languageCode) throws Exception {
        List<EntryComparison> entryComparisons = new ArrayList<EntryComparison>();
        Integer index = 0;
        for (QALD.QALDQuestions qaldQuestions : qaldFile.questions) {
            List<String> answers = new ArrayList<String>();
            String qaldQuestion = QALDImporter.getQaldQuestionString(qaldQuestions, languageCode);
            String qaldSparqlQuery = QALDImporter.getQaldSparqlQuery(qaldQuestions);
            index = index + 1;
            EntryComparison entryComparison = new EntryComparison();
            String qaldSparql = qaldQuestions.query.sparql;
            Entry qaldEntry = new Entry();
            Entry queGGEntry = new Entry();
            qaldEntry.setActualEntry(qaldQuestions);
            qaldEntry.setId(qaldQuestions.id);
            qaldEntry.setQuestions(qaldQuestion);
            qaldEntry.setSparql(qaldSparql);
            qaldEntry.setResultList(answers);
            entryComparison.setQaldEntry(qaldEntry);
            entryComparison.setQueGGEntry(queGGEntry);
            entryComparisons.add(entryComparison);
        }
        return entryComparisons;
    }

    public Map<String, QueGGinfomation> matchedRealQuestions(String qaldsentence, String qaldSparqlQuery, Map<String, String[]> questions, double similarityPercentage, Integer index) {
        Map<String, QueGGinfomation> matchedQuestions = new TreeMap<String, QueGGinfomation>();
        qaldsentence = qaldsentence.toLowerCase().strip().trim();
        HashMap<String, Double> sort = new HashMap<String, Double>();
        Boolean singleFlag = false, multipleFlag = false, askFlag = false;

        StringSimilarity stringSimilarity = new StringSimilarity();

        if (stringSimilarity.isAskSparqlQuery(qaldSparqlQuery)) {
            askFlag = true;

        } else if (!stringSimilarity.isMultipleSparqlQuery(qaldSparqlQuery)) {
            singleFlag = true;

        } else {
            multipleFlag = true;
        }

        for (String queGGquestion : questions.keySet()) {
            String[] row = questions.get(queGGquestion);
            qaldsentence = qaldsentence.replace("\"", "");
            queGGquestion = queGGquestion.replace("\"", "");
            Double value = StringSimilarity.zacrdSimilarity(qaldsentence, queGGquestion);
            QueGGinfomation queGGinfomation = new QueGGinfomation(row, value, qaldSparqlQuery);

            if (value > similarityPercentage) {
                //QueGGinfomation queGGinfomation = new QueGGinfomation(row, value);
                sort.put(queGGinfomation.getQuestion(), value);
                matchedQuestions.put(queGGinfomation.getQuestion(), queGGinfomation);
                System.out.println("MATCHED: " + qaldsentence + ":" + queGGquestion + " cosineSimilarityPercentage::" + value);
                //exit(1);
            }
        }

        return matchedQuestions;
    }

    private List<String> getSparqlQuery(String sparql, Boolean flag, List<String> resultList) {
        LOG.debug("Executing QALD SPARQL Query:\n{}", sparql);
        List<String> uriResultList = new ArrayList<String>();

        System.out.println("sparql::" + sparql);

        if (menu.contains(EVALUTE_QALD_QUEGG)) {
            return resultList;
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

        if (menu.contains(FIND_QALD_QUEGG_ANSWER)) {
            LinkedDataFragmentSpql main = new LinkedDataFragmentSpql(model, endpoint, sparql);
            uriResultList = main.sparqlObjectAsVariable(sparql);
            uriResultList = main.parseResultList(uriResultList);
        }

        return uriResultList;
    }

    private boolean isAskSparqlQuery(String qaldSparqlQuery) {
        if (qaldSparqlQuery.contains("ASK")) {
            return true;
        }
        return false;
    }

    private List<EntryComparison> getMatchedSentences(String resultMatchFile, String qaldAnswerFile, String qaldqueGGAnswerFile) {
        List<EntryComparison> entryComparisons = new ArrayList<EntryComparison>();
        List<String[]> qaldAnswerData = new ArrayList<String[]>();
        Integer index = 0;
        CsvFile csvFile = new CsvFile();
        List<String[]> rows = csvFile.getRows(new File(resultMatchFile));
        List<String[]> qaldAnswerRows = csvFile.getRows(new File(qaldAnswerFile));
        Map<String, String> qaldInfo = this.getQaldOffLineAnswer(qaldAnswerRows);
        

        for (String[] row : rows) {
            Boolean matchedFlag = false;
            Entry queGG = new Entry();
            Entry qald = new Entry();
            List<String> queGGResults = new ArrayList<String>();
            List<String> qaldResults = new ArrayList<String>();

            if (index == 0) {
                index = index + 1;
                continue;
            } else if (index == 214) {
                break;
            }

            index = index + 1;

            String[] newRow = new String[8];

            Double similarityScore = 0.0;
            String id = row[0];
            similarityScore = Double.parseDouble(row[1]);
            String qaldQuestion = row[2];
            String queGGQuestion = row[3];
            String qaldSparql = row[4];
            String queGGSparql = row[5];

            qald.setId(id);
            qald.setQuestions(qaldQuestion);
            qald.setSparql(qaldSparql);
            if (qaldInfo.containsKey(id)) {
                qaldResults.add(qaldInfo.get(id));
            }
            qald.setResultList(qaldResults);

            System.out.println("id::" + id);
            System.out.println("qaldQuestion::" + qaldQuestion);
            System.out.println("queGGQuestion::" + queGGQuestion);
            System.out.println("qaldSparql::" + qaldSparql);
            System.out.println("queGGSparql::" + queGGSparql);

            if (similarityScore == 0)
                ; else if (qaldSparql.contains("ASK"))
                ; else {
                queGGResults = this.getSparqlQuery(queGGSparql, true, queGGResults);
            }
            System.out.println("queGGResults:" + queGGResults);

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
            //System.out.println(EntryComparison);
            //exit(1);
        }
        csvFile.writeToCSV(new File(qaldqueGGAnswerFile), qaldAnswerData);
        return entryComparisons;
    }

    private List<EntryComparison> getMakeComaprisions(String resultMatchFile) {
        List<EntryComparison> entryComparisons = new ArrayList<EntryComparison>();
        CsvFile csvFile = new CsvFile();
        List<String[]> rows = csvFile.getRows(new File(resultMatchFile));

        Integer index = 0;
        List<String[]> qaldAnswerData = new ArrayList<String[]>();

        for (String[] row : rows) {
            Boolean matchedFlag = false;
            Entry queGG = new Entry();
            Entry qald = new Entry();
            List<String> queGGResults = new ArrayList<String>();
            List<String> qaldResults = new ArrayList<String>();

            if (index == 0) {
                index = index + 1;
                continue;
            }
            index = index + 1;

            Double similarityScore = 0.0;
            String id = row[0];
            similarityScore = Double.parseDouble(row[1]);
            String qaldQuestion = row[2];
            String queGGQuestion = row[3];
            String qaldSparql = row[4];
            String queGGSparql = row[5];
            qaldResults = this.makeList(row[6]);
            queGGResults = this.makeList(row[7]);

            qald.setId(id);
            qald.setQuestions(qaldQuestion);
            qald.setSparql(qaldSparql);
            qald.setResultList(qaldResults);

            queGG.setId(id);
            queGG.setQuestions(queGGQuestion);
            queGG.setSparql(queGGSparql);
            queGG.setResultList(queGGResults);

            // System.out.println(id);
            // System.out.println(queGGQuestion);
            //System.out.println(queGGQuestion);
            //System.out.println(qaldSparql);
            //System.out.println(queGGSparql);
            // System.out.println("qaldResults:" + qaldResults);
            //System.out.println("queGGResults:" + queGGResults);
            if (similarityScore > 0.0) {
                matchedFlag = Boolean.TRUE;
            }
            EntryComparison EntryComparison = new EntryComparison(qald, queGG, matchedFlag, qaldResults, queGGResults, similarityScore);
            entryComparisons.add(EntryComparison);
        }

        return entryComparisons;
    }

    private List<String> makeList(String data) {
        List<String> list = new ArrayList<String>();

        data = data.replace("[", "").replace("]", "");

        if (data.contains(",")) {
            List<String> newlist = new ArrayList<String>();
            list = Arrays.asList(data.split(","));

            for (String element : list) {
                element = element.strip().stripLeading().stripTrailing().trim();
                if (element.contains("http")) {
                    element = element.replace(" ", "");
                }
                newlist.add(element);
            }
            return newlist;
        } else {
            data = data.strip().stripLeading().stripTrailing().trim();
            if (data.contains("http")) {
                data = data.replace(" ", "");
            }
            list.add(data);

            return list;
        }

    }

    private Map<String, String> getQaldOffLineAnswer(List<String[]> qaldAnswerRows) {
        Map<String, String> map = new TreeMap<String, String>();

        Integer index = 0;
        for (String[] row : qaldAnswerRows) {
            index = index + 1;
            if (index == 1) {
                continue;
            }

            String id = row[0];
            String answer = row[3];
            map.put(id, answer);
        }
        return map;
    }

}
