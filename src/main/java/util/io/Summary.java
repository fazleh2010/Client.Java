/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.io;

import evaluation.EvaluationResult;
import java.io.File;

/**
 *
 * @author elahi
 */
public class Summary {

    public static String[] setStart() {
        String[] row = new String[10];
        row[0] = "fileName";
        row[1] = "global Tp";
        row[2] = "global Fp";
        row[3] = "global Fn";
        row[4] = "global precision";
        row[5] = "global recall";
        row[6] = "global f-measure";
        row[7] = "average precision";
        row[8] = "average recall";
        row[9] = "average f-measure";
        return row;
    }

    public static String[] setKomparsionResult(String fileName, EvaluationResult evaluationResult) {
        String[] row = new String[10];
        row[0] = new File(fileName).getName();
        row[1] = Float.toString(evaluationResult.getTp_global());
        row[2] = Float.toString(evaluationResult.getFp_global());
        row[3] = Float.toString(evaluationResult.getFn_global());
        row[4] = Float.toString(evaluationResult.getPrecision_global());
        row[5] = Float.toString(evaluationResult.getRecall_global());
        row[6] = Float.toString(evaluationResult.getF_measure_global());
        row[7] = Float.toString(evaluationResult.getPrecision_average());
        row[8] = Float.toString(evaluationResult.getRecall_average());
        row[9] = Float.toString(evaluationResult.getF_measure_average());
        return row;
    }

}
