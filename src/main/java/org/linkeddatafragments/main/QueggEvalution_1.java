package org.linkeddatafragments.main;

import org.linkeddatafragments.util.io.JsonAccess;
import org.linkeddatafragments.util.io.FilterRows;
import org.linkeddatafragments.util.io.FileUtils;
import org.linkeddatafragments.util.io.LinkedData;
import org.linkeddatafragments.util.io.Summary;
import org.linkeddatafragments.util.io.InputCofiguration;
import org.linkeddatafragments.util.io.CsvFile;
import org.linkeddatafragments.util.io.Calculation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvException;
import org.linkeddatafragments.evaluation.EvaluateAgainstQALD;
import org.linkeddatafragments.evaluation.OfflineQueGGAnswers;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.io.*;
import static java.lang.System.exit;
import org.linkeddatafragments.evaluation.FindAnswer;

@NoArgsConstructor
public class QueggEvalution_1 implements Constants {

    private static final Logger LOG = LogManager.getLogger(QueggEvalution_1.class);
    private static LinkedHashSet<String> menu = new LinkedHashSet<String>();
    private static Boolean online = false;
    private static List<String[]> results = new ArrayList<String[]>();
    private static Map<String, List<File>> patternFiles = new TreeMap<String, List<File>>();
    //private static Map<String, List<File>> lexParamters = new TreeMap<String, List<File>>();
    private static String[] header=new String[]{"lexiconName","configuration","Tp","Fp","Fn","global-precision","global-recall","global-f-measure","average-precision","average-recall","average-f-measure"};

    public static void main(String[] args) throws Exception {

        QueggEvalution_1 queGG = new QueggEvalution_1();
        // FIND_SIMILARITY has to run alone. for unknown reasons all menu does not work
        //menu.add(MAKE_PROPERTY_FILE);
        //menu.add(FIND_QALD_ANSWER);
        //menu.add(FIND_SIMILARITY);
        menu.add(FIND_QALD_QUEGG_ANSWER);
        //menu.add(EVALUTE_QALD_QUEGG);
        //menu.add(SUMMARY);
        String lexiconsDir = "/media/elahi/Elements/A-project/LDK2023/resources/en/lexicons/";
        String parameterFileName = "/media/elahi/Elements/A-project/LDK2023/resources/en/lexicons/parameter.txt";
        String evalutionType = "inductive";
        InputCofiguration inputCofiguration = FileUtils.getInputConfig(new File(configFile));

        if (evalutionType.contains("inductive")) {
            patternFiles = findSingleParameterFile(lexiconsDir, parameterFileName);
        }
        if (evalutionType.contains("incremental")) {
            patternFiles = findMultipleParameterFile(lexiconsDir, parameterFileName);
        }
        LinkedHashMap<String, String> lexParameters = FileUtils.fileToLinkedHashMap(parameterFileName);
        System.out.println(lexParameters);
        
        //System.out.println(inputCofiguration.getOutputDir()+ File.separator+ "Summary.csv");
        //exit(1);
        /*if(menu.contains(SUMMARY)){
            lexParamters=findlexParameterParameterFile(inputCofiguration.getOutputDir(), parameterFileName);
            for(String parameter:lexParamters.keySet()){
                List<File> files=lexParamters.get(parameter);
                for(File file:files){
                    System.out.println(file.getName());
                }
                
            }
        }*/
        //exit(1);
        //exit(1);
        /*Map<String, CsvFile> ruleSingleFile = new TreeMap<String, CsvFile>();
        for (String parameter : ruleFiles.keySet()) {
            List<File> files = ruleFiles.get(parameter);
             System.out.println(parameter+" "+files.size());
            String outputFile = lexiconsDir + parameter + ".csv";
            CsvFile csvFile = new CsvFile(new File(outputFile));
            Boolean flag = csvFile.makeMultipleFilesToSingle(files);
            if (flag) {
                ruleSingleFile.put(parameter, csvFile);
            }
        }*/
        try {
            //InputCofiguration inputCofiguration = FileUtils.getInputConfig(new File(configFile));
            String[] donefiles = new File(inputCofiguration.getOutputDir()).list();
            List<String> doneFiles = Arrays.asList(donefiles);
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
                    for (String parameter : patternFiles.keySet()) {
                        if (!isDone(doneFiles, parameter)) {
                            List<File> files = patternFiles.get(parameter);
                            if (parameter.contains("lexicon_3")) {
                                //System.out.println(parameter);
                                queGG.evalutionTest(inputCofiguration, parameter, lexParameters);
                            }
                        }
                    }

                } else if (type.contains("train")) {
                    Integer index = 0;
                    for (String parameter : patternFiles.keySet()) {
                        List<File> files = patternFiles.get(parameter);
                        if (menu.contains(FIND_SIMILARITY)) {
                            if (!isDone(doneFiles, parameter)) {
                                if (parameter.contains("lexicon_5")||parameter.contains("lexicon_6")
                                        ||parameter.contains("lexicon_7")||parameter.contains("lexicon_8")||
                                        parameter.contains("lexicon_9")) {
                                    //System.out.println(parameter);
                                    queGG.evalutionTrain(inputCofiguration, files, doneFiles, parameter, lexParameters);
                                }
                            }

                        } else if (menu.contains(FIND_QALD_QUEGG_ANSWER)) {
                             if (parameter.contains("lexicon_5")||parameter.contains("lexicon_6")
                                        ||parameter.contains("lexicon_7")||parameter.contains("lexicon_8")||
                                        parameter.contains("lexicon_9")) {
                              System.out.println(parameter);
                              queGG.evalutionTrain(inputCofiguration, files, doneFiles, parameter, lexParameters);
                             }
                        }//break;
                        else if (menu.contains(EVALUTE_QALD_QUEGG)) {
                            //if (parameter.equals("lexicon_1")) {
                            //System.out.println(parameter);
                            queGG.evalutionTrain(inputCofiguration, files, doneFiles, parameter, lexParameters);
                            index = index + 1;
                            /*if (index >= 5) {
                                break;
                            }*/
                            //}
                        }//break;
                    }
                    //queGG.evalutionTrain(inputCofiguration,new ArrayList<File>()); 
                }
                //FileUtils.ResultToFile(EvaluateAgainstQALD.getOverallResult(), inputCofiguration.getOutputDir()+File.separator+"A-Summary.csv",header);
                //System.out.print(EvaluateAgainstQALD.getOverallResultRanked().keySet());
                FileUtils.ResultToFileRanked(EvaluateAgainstQALD.getOverallResultRanked(), inputCofiguration.getOutputDir()+File.separator+"A-Summary-ranked.csv",header);
            }

        } catch (IllegalArgumentException | IOException e) {
            e.printStackTrace();
            System.err.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
        }
        //this exit is necessary.
        exit(1);

    }

    /*private static Map<String, List<File>> findSingleParameterFile(String lexiconsDir, String parameterFileName) throws IOException, FileNotFoundException, CsvException {

        Set<String> parameters = FileUtils.FileToSet(parameterFileName);
        System.out.print(parameters);

        Map<String, List<File>> ruleFiles = new TreeMap<String, List<File>>();
        for (String paramter : parameters) {
            CsvFile outputCsvFile = new CsvFile();
            String dir = lexiconsDir + paramter ;
            File[] parameterFiles = new File(dir).listFiles();
            List<String[]> csvData = new ArrayList<String[]>();
            for (File fileName : parameterFiles) {
                if (fileName.getName().contains(".csv")) {
                    CsvFile csvFile = new CsvFile();
                    List<String[]> rows = csvFile.getRows(fileName);
                    if (rows.size() > 1) {
                        System.out.println(fileName + "  elemetns size:::" + rows.size());
                        csvData.addAll(rows);
                    }
                }
            }
            File outputFile = new File(lexiconsDir + paramter + ".csv");
            outputCsvFile.writeToCSV(outputFile, csvData);
            List<File> list = new ArrayList<File>();
            list.add(outputFile);
            ruleFiles.put(paramter, list);
        }
        return ruleFiles;
    }*/
    private static boolean isDone(List<String> doneFiles, String parameter) {
        for (String fileString : doneFiles) {
            if (!fileString.contains(".csv")) {
                continue;
            }
            if (fileString.contains("_" + parameter + "_")) {
                return true;
            }
        }
        return false;
    }

    /*private static Map<String, List<File>> findlexParameterParameterFile(String lexiconsDir, String parameterFileName) throws IOException {
        LinkedHashMap<String, String> parameters = FileUtils.FileToLinkedHashMap(parameterFileName);

        Map<String, List<File>> ruleFiles = new TreeMap<String, List<File>>();
        for (String paramter : parameters.keySet()) {
            String value = parameters.get(paramter);
            File[] parameterFiles = new File(lexiconsDir).listFiles();
            for (File fileName : parameterFiles) {
                if (fileName.getName().contains("paramter")&&) {
                    List<File> list = new ArrayList<File>();
                    list.add(fileName);
                    ruleFiles.put(value, list);
                }
            }
        }
        return ruleFiles;
    }*/
    public void evalutionTrain(InputCofiguration inputCofiguration, List<File> files, List<String> doneFiles, String parameter, LinkedHashMap<String, String> lexParameters) throws IOException, Exception {
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

        //String FIND_SIMILARITY_OUTPUT = outputDir + File.separator + dataset + "-QueGG-Match_" + languageCode + "_" + fileType + ".csv";
        String FIND_SIMILARITY_OUTPUT = outputDir + File.separator + dataset + "-QueGG-answer_" + languageCode + "_" + fileType + ".csv";
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
        OfflineQueGGAnswers queGGAnswers = JsonAccess.readObjectJson(qaldQueGGAnswerJsonFile);
        System.out.println(queGGAnswers.getAnswers());
        //EvaluateAgainstQALD.setOfflineAnswerList(queGGAnswers.getAnswers());
        List<String[]> results = new ArrayList<String[]>();
        results.add(Summary.setStart());

        Integer startRange = 0;
        String lexialEntry = null;

        Map<String, String[]> queGGQuestions = new TreeMap<String, String[]>();

        //String dir="/media/elahi/Elements/A-project/LDK2023/Client.Java/output/en/questions/lexicalEntry";
        //File []files=new File(dir).listFiles();
        for (File file : files) {
            startRange = startRange + 1;
            FilterRows filterRows = new FilterRows(file);
            queGGQuestions.putAll(filterRows.getQueGGQuestions());
            lexialEntry = "_" + file.getName().replace(".csv", "").replace("questions_", "") + "_" + startRange.toString();
            if (endRange == -1)
                 ; else if (startRange > endRange) {
                break;
            }

            if (queGGQuestions.isEmpty()) {
                //throw new Exception("no question found in QueGG!!!");
                continue;
            } else {
                EvaluateAgainstQALD evaluateAgainstQALD = new EvaluateAgainstQALD(languageCode, endpoint, menu, FIND_SIMILARITY_OUTPUT, comparisonFile, qaldAnswerFile, qaldQueGGAnswerFile, online, lexialEntry, parameter, queGGAnswers, lexParameters);
                evaluateAgainstQALD.evaluateAndOutput(queGGQuestions, qaldFile, qaldModifiedFile, qaldRaw, languageCode, similarityMeasure, lexialEntry);
                results.addAll(evaluateAgainstQALD.getResult());
            }
        }

        OfflineQueGGAnswers newQueGGAnswers = new OfflineQueGGAnswers(FindAnswer.getAnswers());
        JsonAccess.writeObjectJson(qaldQueGGAnswerJsonFile.replace(".csv", ".json"), newQueGGAnswers);

        CsvFile CsvFile = new CsvFile(new File(summaryFile));

        for (String[] result : results) {
            String str = "";
            for (String lenString : result) {
                str += lenString;
            }
        }
        CsvFile.writeToCSV(results);

        FileUtils.hashMapOrgtoFile(FindAnswer.getLongResultSparql(), "src/main/resources/sparql.txt");

    }

    public void evalutionTest(InputCofiguration inputCofiguration, String parameter, LinkedHashMap<String, String> lexParameters) throws IOException, Exception {
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
        OfflineQueGGAnswers queGGAnswers = JsonAccess.readObjectJson(qaldQueGGAnswerJsonFile);
        //EvaluateAgainstQALD.setOfflineAnswerList(queGGAnswers.getAnswers());
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
                EvaluateAgainstQALD evaluateAgainstQALD = new EvaluateAgainstQALD(languageCode, endpoint, menu, FIND_SIMILARITY_OUTPUT, comparisonFile, qaldAnswerFile, qaldQueGGAnswerFile, online, startRange.toString(), parameter, queGGAnswers, lexParameters);
                evaluateAgainstQALD.evaluateAndOutput(queGGQuestions, qaldFile, qaldModifiedFile, qaldRaw, languageCode, similarityMeasure, lexialEntry);
                results.addAll(evaluateAgainstQALD.getResult());
            }
        }

        OfflineQueGGAnswers newQueGGAnswers = new OfflineQueGGAnswers(EvaluateAgainstQALD.getAnswers());
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

    private static Map<String, List<File>> findSingleParameterFile(String lexiconsDir, String parameterFileName) throws IOException, FileNotFoundException, CsvException {

        Set<String> parameters = FileUtils.FileToSet(parameterFileName);

        Map<String, List<File>> ruleFiles = new TreeMap<String, List<File>>();
        for (String paramter : parameters) {
            String dir = lexiconsDir + paramter;
            File[] parameterFiles = new File(dir).listFiles();
            for (File fileName : parameterFiles) {
                if (fileName.getName().contains(".csv")) {
                    List<File> list = new ArrayList<File>();
                    list.add(fileName);
                    ruleFiles.put(paramter, list);
                }
            }
        }
        return ruleFiles;
    }

    private static void findParameterResults(String evalutionDir, String parameterFileName) throws IOException, FileNotFoundException, CsvException {

        LinkedHashMap<String, String> parameters = FileUtils.fileToLinkedHashMap(parameterFileName);
        File[] allFiles = new File(evalutionDir).listFiles();

        for (String lexT : parameters.keySet()) {
            String parameter = parameters.get(lexT);
            for (File fileName : allFiles) {
                if (fileName.getName().contains("Comparison")) {
                    CsvFile CsvFile = new CsvFile();
                    List<String[]> rows = CsvFile.getRows(fileName);
                    for (String[] row : rows) {
                        String id = row[0];
                        if (id.contains("214")) {

                        }
                    }
                }
            }
        }
    }

    /*private static void findParameterResults(String evalutionDir, String parameterFileName) throws IOException, FileNotFoundException, CsvException {

        LinkedHashMap<String, String> parameters = FileUtils.fileToLinkedHashMap(parameterFileName);
        File[] allFiles = new File(evalutionDir).listFiles();

        for (String lexT : parameters.keySet()) {
            String parameter = parameters.get(lexT);
            for (File fileName : allFiles) {
                if (fileName.getName().contains("Comparison")) {
                    CsvFile CsvFile = new CsvFile();
                    List<String[]> rows = CsvFile.getRows(fileName);
                    for (String[] row : rows) {
                        String id = row[0];
                        if(id.contains("214")){
                            
                        }
                    }
                }
            }
        }
    }*/
    private static Map<String, List<File>> findMultipleParameterFile(String lexiconsDir, String parameterFileName) throws IOException, FileNotFoundException, CsvException {
        Set<String> parameters = FileUtils.FileToSet(parameterFileName);
        Map<String, List<File>> ruleFiles = new TreeMap<String, List<File>>();
        for (String paramter : parameters) {
            String dir = lexiconsDir + paramter + File.separator + "questions/";
            File[] parameterFiles = new File(dir).listFiles();
            List<File> list = new ArrayList<File>();
            for (File fileName : parameterFiles) {
                if (fileName.getName().contains(".csv")) {
                    CsvFile csvFile = new CsvFile();
                    List<String[]> rows = csvFile.getRows(fileName);
                    if (rows.size() > 1) {
                        System.out.println(fileName + "  elemetns size:::" + rows.size());
                        list.add(csvFile.getFilename());
                    }
                }
            }
            ruleFiles.put(paramter, list);
        }
        return ruleFiles;
    }

    /*private static Map<String, List<String>> findParamerFiles(String questionDir, String parameterFileName,Integer parameterIndex) {

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
    }*/
}
