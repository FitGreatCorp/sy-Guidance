package com.fitgreat.airfacerobot.model;

import java.io.Serializable;

/**
 * 常见问题对象
 */
public class CommonProblemEntity implements Serializable {
    private String F_QId;
    private String F_Question;
    private String F_Answer;
    //英文题目题干
    private String F_EQuestion;
    //英文题目答案
    private String F_EAnswer;

    public CommonProblemEntity() {
    }

    public CommonProblemEntity(String f_QId, String f_Question, String f_Answer, String f_EQuestion, String f_EAnswer) {
        F_QId = f_QId;
        F_Question = f_Question;
        F_Answer = f_Answer;
        F_EQuestion = f_EQuestion;
        F_EAnswer = f_EAnswer;
    }


    public String getF_QId() {
        return F_QId;
    }

    public void setF_QId(String f_QId) {
        F_QId = f_QId;
    }

    public String getF_Question() {
        return F_Question;
    }

    public void setF_Question(String f_Question) {
        F_Question = f_Question;
    }

    public String getF_Answer() {
        return F_Answer;
    }

    public void setF_Answer(String f_Answer) {
        F_Answer = f_Answer;
    }


    public String getF_EAnswer() {
        return F_EAnswer;
    }

    public void setF_EAnswer(String f_EAnswer) {
        F_EAnswer = f_EAnswer;
    }

    public String getF_EQuestion() {
        return F_EQuestion;
    }

    public void setF_EQuestion(String f_EQuestion) {
        F_EQuestion = f_EQuestion;
    }

    @Override
    public String toString() {
        return "CommonProblemEntity{" +
                "F_QId='" + F_QId + '\'' +
                ", F_Question='" + F_Question + '\'' +
                ", F_Answer='" + F_Answer + '\'' +
                ", F_EQuestion='" + F_EQuestion + '\'' +
                ", F_EAnswer='" + F_EAnswer + '\'' +
                '}';
    }
}
