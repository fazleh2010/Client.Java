package Main;

import com.fasterxml.jackson.databind.ObjectMapper;
import evaluation.EvaluateAgainstQALD;
import static evaluation.EvaluateAgainstQALD.PROTOTYPE_QUESTION;
import static evaluation.EvaluateAgainstQALD.REAL_QUESTION;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import static java.util.Objects.isNull;
import java.io.File;
import java.io.IOException;
import util.io.CsvFile;
import util.io.FileUtils;
import util.io.InputCofiguration;
import util.io.Language;
import util.io.LinkedData;

@NoArgsConstructor
public class QueGG {

    private static final Logger LOG = LogManager.getLogger(QueGG.class);
    private static String questionsFile = "questions";
    private static String summaryFile = "summary";
    private static Boolean online = true;
    private static Integer menu=2;

    public static void main(String[] args) throws Exception {
        QueGG queGG = new QueGG();
        String configFile = null, dataSetConfFile = null;
        configFile="/home/elahi/AHack/general/Client.Java/conf/inputConf.json";
        dataSetConfFile="/home/elahi/AHack/general/Client.Java/conf/dbpedia.json";

        try {
            InputCofiguration inputCofiguration = FileUtils.getInputConfig(new File(configFile));
            inputCofiguration.setLinkedData(dataSetConfFile);
            if (inputCofiguration.isEvalution()) {
                Language language = inputCofiguration.getLanguage();
                String qaldDir = inputCofiguration.getQaldDir();
                String outputDir = inputCofiguration.getOutputDir();
                LinkedData linkedData = inputCofiguration.getLinkedData();
                Double similarity = inputCofiguration.getSimilarityThresold();
                queGG.evalution(qaldDir, outputDir, language, linkedData.getEndpoint(), EvaluateAgainstQALD.REAL_QUESTION, similarity);
            }

        } catch (IllegalArgumentException | IOException e) {
            System.err.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
            System.err.printf("Usage: <%s> <input directory> <output directory>%n", Arrays.toString(Language.values()));
        }

    }

    public void evalution(String qaldDir, String outputDir, Language language, String endpoint, String questionType, Double similarityMeasure) throws IOException, Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String queGGJson = null, queGGJsonCombined = null, qaldFile = null, qaldModifiedFile = null;
        String languageCode = language.name().toLowerCase();
        String resultMatchFile = outputDir + File.separator + "QALD-QueGG-Match_" + languageCode + ".csv";
        String resultComparisonFile = outputDir + File.separator + "QALD-QueGG-Comparison_" + languageCode + ".csv";
        String qaldQueGGAnswerFile= outputDir + File.separator + "QALD-QueGG-answer_" + languageCode + ".csv";
        String qaldRaw = outputDir + File.separator + "QALD-2017-dataset-raw.csv";
        //EvaluateAgainstQALD evaluateAgainstQALD = new EvaluateAgainstQALD(languageCode, endpoint,menu,resultMatchFile);

        for (String fileName : new File(qaldDir).list()) {
            if (fileName.contains("qald")) {
                if (fileName.contains("train-multilingual_modified.json")) {
                    qaldModifiedFile = qaldDir + File.separator + fileName;
                } else if (fileName.contains("train-multilingual.json")) {
                    qaldFile = qaldDir + File.separator + fileName;
                }
            }
        }

        //temporary code for qald entity creation...
        //System.out.println(entityLabelDir+File.separator+"qaldEntities.txt");
        //FileUtils.stringToFile(string, entityLabelDir+File.separator+"qaldEntities.txt");
        if (questionType.contains(REAL_QUESTION)) {

            Map<String, String[]> queGGQuestions = new HashMap<String, String[]>();
            List<String[]> rows = new ArrayList<String[]>();
            String[] files = new File(outputDir).list();
            for (String fileName : files) {
                if (fileName.contains(questionsFile) && fileName.contains(".csv")) {
                    File file = new File(outputDir + File.separator + fileName);
                    CsvFile csvFile = new CsvFile(file);
                    rows = csvFile.getRows(file);
                    for (String[] row : rows) {
                        String question = row[1];
                        String cleanQuestion = question.toLowerCase().trim().strip().stripLeading().stripTrailing();
                        queGGQuestions.put(cleanQuestion, row);
                    }
                }
            }
            EvaluateAgainstQALD evaluateAgainstQALD =new EvaluateAgainstQALD(languageCode,endpoint,menu,resultMatchFile,resultComparisonFile,qaldQueGGAnswerFile);
            evaluateAgainstQALD.evaluateAndOutput(queGGQuestions, qaldFile, qaldModifiedFile,qaldRaw, languageCode, questionType, similarityMeasure);

            /*if(menu==1){
              evaluateAgainstQALD.evaluateAndOutput(queGGQuestions, qaldFile, qaldModifiedFile,qaldRaw, languageCode, questionType, similarityMeasure);
            }
            else{
              evaluateAgainstQALD.evaluateAndOutput(queGGQuestions, qaldFile, qaldModifiedFile, qaldRaw, languageCode, questionType, similarityMeasure);
 
            }*/

        }

    }

    private void questionEvaluation(InputCofiguration inputCofiguration) throws Exception {
        Language language = inputCofiguration.getLanguage();
        String qaldDir = inputCofiguration.getQaldDir();
        String outputDir = inputCofiguration.getOutputDir();
        LinkedData linkedData = inputCofiguration.getLinkedData();
        Double similarityMeasure = inputCofiguration.getSimilarityThresold();
        Boolean combinedFlag = inputCofiguration.getCompositeFlag();
        evalution(qaldDir, outputDir, language, linkedData.getEndpoint(), EvaluateAgainstQALD.REAL_QUESTION, similarityMeasure);

    }

}
