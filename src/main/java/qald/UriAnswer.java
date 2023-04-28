/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qald;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author elahi
 */
public class UriAnswer {

    @JsonProperty("uri")
    private Map<String, String> uri = new HashMap<String, String>();

    @JsonProperty("c")
    private Map<String, String> c = new HashMap<String, String>();
    
    @JsonProperty("string")
    private Map<String, String> string = new HashMap<String, String>();
    
    @JsonProperty("date")
    private Map<String, String> date = new HashMap<String, String>();

    public UriAnswer() {

    }

    public Map<String, String> getUri() {
        return uri;
    }

    public Map<String, String> getC() {
        return c;
    }

    public Map<String, String> getString() {
        return string;
    }

    public Map<String, String> getDate() {
        return date;
    }
    
    public String getUriValue() {
        return uri.get("value");
    }
   
    public String getDateValue() {
        return date.get("value");
    }
    public String getStringValue() {
        return string.get("value");
    }
    public String getCValue() {
        return c.get("value");
    }

    @Override
    public String toString() {
        return "UriAnswer{" + "uri=" + uri + ", c=" + c + ", string=" + string + ", date=" + date + '}';
    }
    
    
}
