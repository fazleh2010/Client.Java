/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qald;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author elahi
 */
public class UriElement {

    @JsonProperty("type")
    private String type = null;
    @JsonProperty("value")
    private String value = null;

    public UriElement() {

    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "UriElement{" + "type=" + type + ", value=" + value + '}';
    }

}
