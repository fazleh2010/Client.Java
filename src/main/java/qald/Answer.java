/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qald;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author elahi
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Answer {

    @JsonProperty("head")
    private Map<String, List<String>> head = new HashMap<String, List<String>>();
    @JsonProperty("results")
    private Results results= new Results();

    public Answer() {

    }

    public Map<String, List<String>> getHead() {
        return head;
    }

    public Results getResults() {
        return results;
    }

    @Override
    public String toString() {
        return "Answer{" + "head=" + head + ", results=" + results + '}';
    }

   
   

}
