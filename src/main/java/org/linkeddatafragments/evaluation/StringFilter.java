/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.linkeddatafragments.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author elahi
 */
public class StringFilter {

    public static String fillString(String string) {
        string = string.replace("\"", "").toLowerCase().trim().strip().stripLeading().stripTrailing().replace(" ", "");
        return string;
    }

    public static List<String> makeList(String data) {
        List<String> list = new ArrayList<String>();

        data = data.replace("[", "").replace("]", "");
        data = data.replace("(", "").replace(")", "");
        data = data.replace(" ", "").strip().stripLeading().stripTrailing().trim().replace("\"", "");

        if (data.contains(",")) {
            List<String> newlist = new ArrayList<String>();
            list = Arrays.asList(data.split(","));

            for (String element : list) {
                 element = element.strip().stripLeading().stripTrailing().trim();
                if (element.contains("http")) {
                    element = element;
                }
                newlist.add(element);
            }
            return newlist;
        } else {
            data = data.strip().stripLeading().stripTrailing().trim();
            if (data.contains("http")) {
                data = data.replace(" ", "");
            }
            list.add(data);

            return list;
        }

    }

    static boolean isNotEmpty(List<String> queGGResults) {
        if (queGGResults.size() == 1) {
            if (queGGResults.toString().contains("[]")) {
                return false;
            }
        }
        return true;
    }

}
