package com.fitgreat.airfacerobot.speech.model;


public class CommonProblemEvent {

    private String showQuestion = null;
    private String showAnswer;

    public String getShowQuestion() {
        return showQuestion;
    }

    public void setShowQuestion(String showQuestion) {
        this.showQuestion = showQuestion;
    }

    public String getShowAnswer() {
        return showAnswer;
    }

    public void setShowAnswer(String showAnswer) {
        this.showAnswer = showAnswer;
    }

    @Override
    public String toString() {
        return "CommonProblemEvent{" +
                "showQuestion='" + showQuestion + '\'' +
                ", showAnswer='" + showAnswer + '\'' +
                '}';
    }
}
