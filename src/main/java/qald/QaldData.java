/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qald;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author elahi
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QaldData {

    @JsonProperty("dataset")
    private Map<String, String> dataset = new HashMap<String, String>();
    @JsonProperty("questions")
    private List<QalddEntry> questions = new ArrayList<QalddEntry>();

    public QaldData() {

    }

    public Map<String, String> getDataset() {
        return dataset;
    }

    public List<QalddEntry> getQuestions() {
        return questions;
    }
    
    @Override
    public String toString() {
        return "QaldData{" + "dataset=" + dataset + ", questions=" + questions + '}';
    }

}
