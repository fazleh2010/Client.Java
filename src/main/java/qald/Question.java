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
import java.util.List;
import java.util.Map;

/**
 *
 * @author elahi
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Question {

    @JsonProperty("language")
    private String language = null;
    @JsonProperty("string")
    private String string = null;
    @JsonIgnore
    private String keywords = null;
    
    public Question(){
        
    }

    public String getLanguage() {
        return language;
    }

    public String getString() {
        return string;
    }

    public String getKeywords() {
        return keywords;
    }

    @Override
    public String toString() {
        return "Question{" + "language=" + language + ", string=" + string + ", keywords=" + keywords + '}';
    }

}
