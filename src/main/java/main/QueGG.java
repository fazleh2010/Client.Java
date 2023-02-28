package main;

import util.io.Calculation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvException;
import evaluation.EvaluateAgainstQALD;
import evaluation.QueGGAnswers;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.io.*;
import static java.lang.System.exit;
import util.io.*;

@NoArgsConstructor
public class QueGG implements Constants {

    private static final Logger LOG = LogManager.getLogger(QueGG.class);
    private static LinkedHashSet<String> menu = new LinkedHashSet<String>();
    private static Boolean online = false;
    private static List<String[]> results = new ArrayList<String[]>();
    private static  Map<String, List<String>> ruleFiles=new TreeMap<String, List<String>>();

    public static void main(String[] args) throws Exception {

        QueGG queGG = new QueGG();
        // FIND_SIMILARITY has to run alone. for unknown reasons all menu does not work
        //menu.add(MAKE_PROPERTY_FILE);
        //menu.add(FIND_QALD_ANSWER);
        //menu.add(FIND_SIMILARITY);
        //menu.add(FIND_QALD_QUEGG_ANSWER);
        menu.add(EVALUTE_QALD_QUEGG);
        String questionDir = "/media/elahi/Elements/A-project/resources/en/questions/";
        String parameterFileName = "/media/elahi/Elements/A-project/resources/ldk/lexicon/parameter.txt";

        ruleFiles = findParamerFiles(questionDir, parameterFileName, 0);
        System.out.println(ruleFiles.keySet());
        
        Map<String, List<String>> ruleSingleFile = new TreeMap<String, List<String>>();
        for (String rulePattern : ruleFiles.keySet()) {
            System.out.println(rulePattern);
            List<String> files = new ArrayList<String>();
            String dir = "/media/elahi/Elements/A-project/resources/en/questions/";
            String outputFile = "/media/elahi/Elements/A-project/Client.Java/output/en/questions//lexicalEntry/" + rulePattern + ".csv";
            CsvFile CsvFile = new CsvFile(new File(outputFile));
            Boolean flag=CsvFile.makeMultipleFilesToSingle(dir, rulePattern);
            if(flag){
            files.add(new File(outputFile).getName());
            ruleSingleFile.put(rulePattern, files);
            }
        }
        
        //exit(1);



        try {
            InputCofiguration inputCofiguration = FileUtils.getInputConfig(new File(configFile));
            online = inputCofiguration.getOnline();
            inputCofiguration.setLinkedData(dataSetConfFile);
            Boolean online = inputCofiguration.getOnline();

            if (inputCofiguration.isNumberOfQuestion()) {
                FileUtils.seperateLexialEntry(inputCofiguration, "numberOfQuestions.csv");
            }
            if (inputCofiguration.isCalculation()) {
                Calculation.numberQuestions(inputCofiguration);
            }
            if (inputCofiguration.isEvalution()) {
                String type = inputCofiguration.getFileType();

                if (type.contains("test")) {
                    queGG.evalutionTest(inputCofiguration);
                } else if (type.contains("train")) {
                    for(String parameter:ruleSingleFile.keySet()){
                        List<String> files=ruleSingleFile.get(parameter);
                        queGG.evalutionTrain(inputCofiguration,files);   
                    }
                }
            }

        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace();
            System.err.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
        }
        //this exit is necessary.
        exit(1);

    }

    private static Map<String, List<String>> findParamerFiles(String questionDir, String parameterFileName,Integer parameterIndex) {

        Set<String> parameters = FileUtils.FileToSet(parameterFileName);

        String[] files = new File(questionDir).list();
        Map<String, List<String>> ruleFiles = new TreeMap<String, List<String>>();
        for (String paramter : parameters) {
            List<String> parameterFiles = new ArrayList<String>();
            for (String fileName : files) {
                String[] info = fileName.split("~");
                String fileParameter = info[parameterIndex];
                //System.out.println("fileParameter::"+fileParameter);

                if (fileParameter.contains(paramter)) {
                    parameterFiles.add(fileName);
                }
            }
            ruleFiles.put(paramter, parameterFiles);

        }
        return ruleFiles;
    }

    public void evalutionTrain(InputCofiguration inputCofiguration,List<String>files) throws IOException, Exception {
        String queGGJson = null, queGGJsonCombined = null, qaldFile = null, qaldModifiedFile = null;
        ObjectMapper objectMapper = new ObjectMapper();
        String qaldDir = inputCofiguration.getQaldDir();
        String outputDir = inputCofiguration.getOutputDir();
        LinkedData linkedData = inputCofiguration.getLinkedData();
        Double similarity = inputCofiguration.getSimilarityThresold();
        String endpoint = linkedData.getEndpoint();
        Double similarityMeasure = inputCofiguration.getSimilarityThresold();
        String languageCode = inputCofiguration.getLanguageCode();
        String dataset = inputCofiguration.getDataset();
        String fileType = inputCofiguration.getFileType();

        String FIND_SIMILARITY_OUTPUT = outputDir + File.separator + dataset + "-QueGG-Match_" + languageCode + "_" + fileType + ".csv";
        String comparisonFile = outputDir + File.separator + dataset + "-QueGG-Comparison_" + languageCode + "_" + fileType + ".csv";
        String qaldAnswerFile = outputDir + File.separator + dataset + "-answer_" + languageCode + "_" + fileType + ".csv";
        String qaldQueGGAnswerFile = outputDir + File.separator + dataset + "-QueGG-answer_" + languageCode + "_" + fileType + ".csv";
        String qaldRaw = outputDir + File.separator + dataset + "_" + fileType + "-dataset-raw.csv";
        String qaldQueGGAnswerJsonFile = outputDir + File.separator + "QueGG-Answer" + ".json";
        String summaryFile = outputDir + File.separator + dataset + "_" + "summary" + ".csv";
        
       
        //EvaluateAgainstQALD evaluateAgainstQALD = new EvaluateAgainstQALD(languageCode, endpoint,menu,resultMatchFile);
        for (String fileName : new File(qaldDir).list()) {
            if (fileName.contains(inputCofiguration.getFileType())) {
                if (fileName.contains("modified")) {
                    qaldModifiedFile = qaldDir + File.separator + fileName;
                } else {
                    qaldFile = qaldDir + File.separator + fileName;
                }
            } else if (fileName.contains("lcquad")) {
                qaldFile = qaldModifiedFile = qaldDir + File.separator + fileName;
            }
        }
        
        

        //temporary code for qald entity creation...
        //FileUtils.stringToFile(string, entityLabelDir+File.separator+"qaldEntities.txt");
        //english 1 to 43
        //test code temporarily block.....................
        //String questionDir = inputCofiguration.getOutputDir() + "/questions/" + "lexicalEntry/";
        
        Integer endRange = 102;
        Integer index = 0;
        EvaluateAgainstQALD.getAnswers();
        QueGGAnswers queGGAnswers = JsonAccess.readObjectJson(qaldQueGGAnswerJsonFile);
        EvaluateAgainstQALD.setOfflineAnswerList(queGGAnswers.getAnswers());
        List<String[]> results = new ArrayList<String[]>();
        results.add(Summary.setStart());

        Integer startRange = 0;
        String lexialEntry = null;
        String dir = inputCofiguration.getOutputDir() + "/questions/" + "lexicalEntry/";
        //String dir = "/media/elahi/Elements/A-project/resources/en/questions/";
        
        Map<String, String[]> queGGQuestions = new TreeMap<String, String[]>();

        for (String fileName : files) {
            System.out.println(fileName);
            startRange = startRange + 1;
            FilterRows filterRows = new FilterRows(dir, fileName);
            queGGQuestions.putAll(filterRows.getQueGGQuestions());
            System.out.println("filterRows.getQueGGQuestions()::"+filterRows.getQueGGQuestions());
            lexialEntry = "_" + fileName.replace(".csv", "").replace("questions_", "") + "_" + startRange.toString();
            if (endRange == -1)
                 ; else if (startRange > endRange) {
                break;
            }

            if (queGGQuestions.isEmpty()) {
                //throw new Exception("no question found in QueGG!!!");
                continue;
            } else {
                EvaluateAgainstQALD evaluateAgainstQALD = new EvaluateAgainstQALD(languageCode, endpoint, menu, FIND_SIMILARITY_OUTPUT, comparisonFile, qaldAnswerFile, qaldQueGGAnswerFile, online, lexialEntry);
                evaluateAgainstQALD.evaluateAndOutput(queGGQuestions, qaldFile, qaldModifiedFile, qaldRaw, languageCode, similarityMeasure, lexialEntry);
                results.addAll(evaluateAgainstQALD.getResult());
            }
        }

        QueGGAnswers newQueGGAnswers = new QueGGAnswers(EvaluateAgainstQALD.getAnswers());
        JsonAccess.writeObjectJson(qaldQueGGAnswerJsonFile.replace(".csv", ".json"), newQueGGAnswers);

        CsvFile CsvFile = new CsvFile(new File(summaryFile));

        for (String[] result : results) {
            String str = "";
            for (String lenString : result) {
                str += lenString;
            }
        }
        CsvFile.writeToCSV(results);
    }
    
   
         
    

    public void evalutionTest(InputCofiguration inputCofiguration) throws IOException, Exception {
        String queGGJson = null, queGGJsonCombined = null, qaldFile = null, qaldModifiedFile = null;
        //initialization......
        ObjectMapper objectMapper = new ObjectMapper();
        String qaldDir = inputCofiguration.getQaldDir();
        String outputDir = inputCofiguration.getOutputDir();
        LinkedData linkedData = inputCofiguration.getLinkedData();
        Double similarity = inputCofiguration.getSimilarityThresold();
        String endpoint = linkedData.getEndpoint();
        Double similarityMeasure = inputCofiguration.getSimilarityThresold();
        String languageCode = inputCofiguration.getLanguageCode();
        String dataset = inputCofiguration.getDataset();
        String fileType = inputCofiguration.getFileType();

        String FIND_SIMILARITY_OUTPUT = outputDir + File.separator + dataset + "-QueGG-Match_" + languageCode + "_" + fileType + ".csv";
        String comparisonFile = outputDir + File.separator + dataset + "-QueGG-Comparison_" + languageCode + "_" + fileType + ".csv";
        String qaldAnswerFile = outputDir + File.separator + dataset + "-answer_" + languageCode + "_" + fileType + ".csv";
        String qaldQueGGAnswerFile = outputDir + File.separator + dataset + "-QueGG-answer_" + languageCode + "_" + fileType + ".csv";
        String qaldRaw = outputDir + File.separator + dataset + "_" + fileType + "-dataset-raw.csv";
        String qaldQueGGAnswerJsonFile = outputDir + File.separator + "QueGG-Answer" + ".json";
        String summaryFile = outputDir + File.separator + dataset + "_" + "summary" + ".csv";

        // get the file name
        for (String fileName : new File(qaldDir).list()) {
            if (fileName.contains(inputCofiguration.getFileType())) {
                if (fileName.contains("modified")) {
                    qaldModifiedFile = qaldDir + File.separator + fileName;
                } else {
                    qaldFile = qaldDir + File.separator + fileName;
                }
            } else if (fileName.contains("lcquad")) {
                qaldFile = qaldModifiedFile = qaldDir + File.separator + fileName;
            }
        }

        //temporary code for qald entity creation...
        //FileUtils.stringToFile(string, entityLabelDir+File.separator+"qaldEntities.txt");
        //english 1 to 43
        //italian 11 to 17
        String[] files = new File(outputDir).list();
        Integer endRange = 35, index = 0;
        String filterFileName = "filter";
        //EvaluateAgainstQALD.getAnswers();
        QueGGAnswers queGGAnswers = JsonAccess.readObjectJson(qaldQueGGAnswerJsonFile);
        EvaluateAgainstQALD.setOfflineAnswerList(queGGAnswers.getAnswers());
        List<String[]> results = new ArrayList<String[]>();
        results.add(Summary.setStart());

        for (Integer startRange = 1; startRange < endRange; startRange++) {
            if (startRange < 12) {
                continue;
            }

            FilterRows filterRows = new FilterRows(outputDir, files, filterFileName, startRange);
            Map<String, String[]> queGGQuestions = filterRows.getQueGGQuestions();
            String lexialEntry = filterRows.getLexialEntry().get(filterRows.getLexialEntry().size() - 1);

            if (queGGQuestions.isEmpty()) {
                throw new Exception("no question found in QueGG!!!");
            } else {
                EvaluateAgainstQALD evaluateAgainstQALD = new EvaluateAgainstQALD(languageCode, endpoint, menu, FIND_SIMILARITY_OUTPUT, comparisonFile, qaldAnswerFile, qaldQueGGAnswerFile, online, startRange.toString());
                evaluateAgainstQALD.evaluateAndOutput(queGGQuestions, qaldFile, qaldModifiedFile, qaldRaw, languageCode, similarityMeasure, lexialEntry);
                results.addAll(evaluateAgainstQALD.getResult());
            }
        }

        QueGGAnswers newQueGGAnswers = new QueGGAnswers(EvaluateAgainstQALD.getAnswers());
        JsonAccess.writeObjectJson(qaldQueGGAnswerJsonFile.replace(".csv", ".json"), newQueGGAnswers);

        CsvFile CsvFile = new CsvFile(new File(summaryFile));

        for (String[] result : results) {
            String str = "";
            for (String lenString : result) {
                str += lenString;
            }
        }
        CsvFile.writeToCSV(results);
    }

    private static void calculateSum() {
        Double english = 189.01;
        Double remove_born_in = english - 1815098;
        Double add_death_date = english + (556491 * 4);
        System.out.println("english::" + english + " remove_born_in::" + remove_born_in + " add_death_date::" + add_death_date);

        Double german = 111.83;
        Double remove_born_in_de = german - (0.62 * 4);
        Double add_death_date_de = german + (0.25 * 4);
        System.out.println("german::" + german + " remove_born_in::" + remove_born_in_de + " add_death_date::" + add_death_date_de);

        Double italian = 7.52;
        Double remove_born_in_it = italian - (0.29 * 3);
        Double add_death_date_it = italian + (0.13 * 4);
        System.out.println("italian::" + italian + " remove_born_in::" + remove_born_in_it + " add_death_date::" + add_death_date_it);

        Double spanish = 44.01;
        Double remove_born_in_es = spanish - (0.27 * 3);
        Double add_death_date_es = spanish + (0.12 * 4);
        System.out.println("spanish::" + spanish + " remove_born_in::" + remove_born_in_es + " add_death_date::" + add_death_date_es);

        System.out.println();
        System.out.println();

        Double englishT = 189.01;
        Double remove_born_inT = englishT - 25.87;
        Double add_death_dateT = englishT + (25.87);
        System.out.println("englishT::" + englishT + " remove_born_inT::" + remove_born_inT + " add_death_dateT::" + add_death_dateT);

        Double germanT = 111.83;
        Double remove_born_in_deT = germanT - 14.86;
        Double add_death_date_deT = germanT + 14.86 * 2;
        System.out.println("germanT::" + germanT + " remove_born_in_deT::" + remove_born_in_deT + " add_death_date_deT::" + add_death_date_deT);

        Double italianT = 7.52;
        Double remove_born_in_itT = italianT - (0.41);
        Double add_death_date_itT = italianT + (0.41 * 2);
        System.out.println("italianT::" + italianT + " remove_born_in_itT::" + remove_born_in_itT + " add_death_date_itT::" + add_death_date_itT);

        Double spanishT = 44.01;
        Double remove_born_in_esT = spanishT - (10.30);
        Double add_death_date_esT = spanishT + (10.30 * 2);
        System.out.println("spanishT::" + spanishT + " remove_born_in_esT::" + remove_born_in_esT + " add_death_date::" + add_death_date_esT);

        System.out.println(189.01 - 187.20);
        System.out.println(189.01 - 163.14);
        System.out.println(191.24 - 189.01);
    }

}
