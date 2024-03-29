/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.linkeddatafragments.util.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.linkeddatafragments.evaluation.OfflineQueGGAnswers;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author elahi
 */
public class JsonAccess {

    public static void writeObjectJson( String fileName,  OfflineQueGGAnswers resources) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writeValue(new File(fileName), resources);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static OfflineQueGGAnswers readObjectJson(String fileName) {
        OfflineQueGGAnswers resources = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            resources = mapper.readValue(new File(fileName), OfflineQueGGAnswers.class);
        } catch (IOException ex) {
            Logger.getLogger(JsonAccess.class.getName()).log(Level.SEVERE, null, ex);
            return new OfflineQueGGAnswers();
        }
        return resources;
    }

}
