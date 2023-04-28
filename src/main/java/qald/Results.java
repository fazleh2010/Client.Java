/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qald;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author elahi
 */
public class Results {

    @JsonProperty("bindings")
    private List<UriAnswer> bindings = new ArrayList<UriAnswer>();

    public Results() {

    }

    public List<UriAnswer> getBindings() {
        return bindings;
    }

   

    @Override
    public String toString() {
        return "Results{" + "dataset=" + bindings + '}';
    }

}
