package Main;

import com.fasterxml.jackson.databind.ObjectMapper;
import evaluation.EvaluateAgainstQALD;
import static evaluation.EvaluateAgainstQALD.REAL_QUESTION;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.io.*;
import static java.lang.System.exit;
import util.io.*;


@NoArgsConstructor
public class QueGG implements Constants{

    private static final Logger LOG = LogManager.getLogger(QueGG.class);
    private static LinkedHashSet <String> menu = new LinkedHashSet <String>();;

    public static void main(String[] args) throws Exception {
        QueGG queGG = new QueGG();
        // FIND_SIMILARITY has to run alone. for unknown reasons all menu does not work
         //menu.add(FIND_QALD_ANSWER);
         menu.add(FIND_QALD_ANSWER);
         //menu.add(FIND_SIMILARITY);
         //menu.add(FIND_QALD_QUEGG_ANSWER);
         //menu.add(EVALUTE_QALD_QUEGG);

        try {
            InputCofiguration inputCofiguration = FileUtils.getInputConfig(new File(configFile));
            inputCofiguration.setLinkedData(dataSetConfFile);
            if (inputCofiguration.isEvalution()) {
                queGG.evalution(inputCofiguration,EvaluateAgainstQALD.REAL_QUESTION);
            }

        } catch (IllegalArgumentException | IOException e) {
             e.printStackTrace();
            System.err.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
        }
        exit(1);

    }

    public void evalution(InputCofiguration inputCofiguration,String questionType) throws IOException, Exception {
        String queGGJson = null, queGGJsonCombined = null, qaldFile = null, qaldModifiedFile = null;
        ObjectMapper objectMapper = new ObjectMapper();
        String qaldDir = inputCofiguration.getQaldDir();
        String outputDir = inputCofiguration.getOutputDir();
        LinkedData linkedData = inputCofiguration.getLinkedData();
        Double similarity = inputCofiguration.getSimilarityThresold();
        String endpoint=linkedData.getEndpoint();
        Double similarityMeasure=inputCofiguration.getSimilarityThresold();
        String languageCode = inputCofiguration.getLanguageCode();
        String dataset=inputCofiguration.getDataset();
      
        
        String FIND_SIMILARITY_OUTPUT = outputDir + File.separator + dataset+"-QueGG-Match_" + languageCode + ".csv";
        String comparisonFile = outputDir + File.separator + dataset+"-QueGG-Comparison_" + languageCode + ".csv";
        String qaldAnswerFile= outputDir + File.separator + dataset+"-answer_" + languageCode + ".csv";
        String qaldQueGGAnswerFile= outputDir + File.separator + dataset+"-QueGG-answer_" + languageCode + ".csv";
        String qaldRaw = outputDir + File.separator + dataset+"-dataset-raw.csv";
        //EvaluateAgainstQALD evaluateAgainstQALD = new EvaluateAgainstQALD(languageCode, endpoint,menu,resultMatchFile);

        for (String fileName : new File(qaldDir).list()) {
            if (fileName.contains("qald")) {
                if (fileName.contains("train-multilingual_modified.json")) {
                    qaldModifiedFile = qaldDir + File.separator + fileName;
                } else if (fileName.contains("train-multilingual.json")) {
                    qaldFile = qaldDir + File.separator + fileName;
                }
            }
            else if(fileName.contains("lcquad")){
                qaldFile=qaldModifiedFile = qaldDir + File.separator + fileName;
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
            EvaluateAgainstQALD evaluateAgainstQALD =new EvaluateAgainstQALD(languageCode,endpoint,menu,FIND_SIMILARITY_OUTPUT,comparisonFile,qaldAnswerFile,qaldQueGGAnswerFile);
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
        Language language = inputCofiguration.getLanguage(inputCofiguration.getLanguageCode());
        String qaldDir = inputCofiguration.getQaldDir();
        String outputDir = inputCofiguration.getOutputDir();
        LinkedData linkedData = inputCofiguration.getLinkedData();
        Double similarityMeasure = inputCofiguration.getSimilarityThresold();
        Boolean combinedFlag = inputCofiguration.getCompositeFlag();
        evalution(inputCofiguration,EvaluateAgainstQALD.REAL_QUESTION);

    }

}
