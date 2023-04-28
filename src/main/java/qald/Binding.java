/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qald;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author elahi
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Binding {
    
    @JsonProperty("uri")
    private Map<String,UriElement> uri = new HashMap<String,UriElement>();
    
    public Binding(){
        
    }

    public Map<String, UriElement> getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "Binding{" + "uri=" + uri + '}';
    }

   
}
