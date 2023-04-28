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
public class QalddEntry {

    @JsonProperty("id")
    private Integer id = 0;

    @JsonProperty("answertype")
    private String answertype = null;

    @JsonProperty("aggregation")
    private Boolean aggregation = false;

    @JsonProperty("onlydbo")
    private Boolean onlydbo = false;

    @JsonProperty("hybrid")
    private Boolean hybrid = false;

    @JsonProperty("question")
    private List<Question> question = new ArrayList<Question>();

    @JsonProperty("query")
    private Map<String, String> query = new HashMap<String, String>();

    @JsonProperty("answers")
    private List<Answer> answers = new ArrayList<Answer>();

    public QalddEntry() {

    }

    public Integer getId() {
        return id;
    }

    public String getAnswertype() {
        return answertype;
    }

    public Boolean getAggregation() {
        return aggregation;
    }

    public Boolean getOnlydbo() {
        return onlydbo;
    }

    public Boolean getHybrid() {
        return hybrid;
    }

    public List<Question> getQuestion() {
        return question;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    public static List<String> getAnswers(QalddEntry qalddEntry, String languageCode) throws Exception {
        List<Answer> answers = qalddEntry.getAnswers();
        List<String> results = new ArrayList<String>();

        for (Answer answer : answers) {
            String uri = null;
            String anwerType = answer.getHead().get("vars").toString();
            if (anwerType.contains("uri")) {
                for (UriAnswer uriAnswer : answer.getResults().getBindings()) {
                    uri = uriAnswer.getUriValue();
                    results.add(uri);
                }
            } else if (anwerType.contains("c")) {
                for (UriAnswer uriAnswer : answer.getResults().getBindings()) {
                     uri =uriAnswer.getCValue();
                    results.add(uri);
                }
            } else if (anwerType.contains("string")) {
                for (UriAnswer uriAnswer : answer.getResults().getBindings()) {
                    uri = uriAnswer.getStringValue();
                    results.add(uri);
                }
            } else if (anwerType.contains("date")) {
                for (UriAnswer uriAnswer : answer.getResults().getBindings()) {
                    uri = uriAnswer.getDateValue();
                    results.add(uri);
                }
            }

        }
        return results;
    }

    public static String getQuestion(QalddEntry qalddEntry, String languageCode) throws Exception {
        for (Question questionE : qalddEntry.getQuestion()) {
            String language=questionE.getLanguage();
            if (language.contains(languageCode)) {
                return questionE.getString();
            } 

        }
        return null;
    }

    @Override
    public String toString() {
        return "QalddEntry{" + "id=" + id + ", answertype=" + answertype + ", aggregation=" + aggregation + ", onlydbo=" + onlydbo + ", hybrid=" + hybrid + ", question=" + question + ", query=" + query + ", answers=" + answers + '}';
    }

}
