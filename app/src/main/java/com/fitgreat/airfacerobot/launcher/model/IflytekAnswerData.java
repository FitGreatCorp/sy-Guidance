package com.fitgreat.airfacerobot.launcher.model;

/**
 * 讯飞提供结果数据<p>
 *
 * @author zixuefei
 * @since 2020/4/10 0010 18:31
 */
public class IflytekAnswerData {

    /**
     * answerType : iFlytekKnowledgeMap
     * emotion : default
     * question : {"question":"李白是谁","question_ws":"李白/NPP//  是/V_SHI//  谁/NP//"}
     * text : 李白（701年－762年） ，字太白，号青莲居士，又号“谪仙人”，是唐代伟大的浪漫主义诗人，被后人誉为“诗仙”。
     * topicID : NULL
     * type : T
     */

    private String answerType;
    private String emotion;
    private QuestionBean question;
    private String text;
    private String topicID;
    private String type;

    public String getAnswerType() {
        return answerType;
    }

    public void setAnswerType(String answerType) {
        this.answerType = answerType;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public QuestionBean getQuestion() {
        return question;
    }

    public void setQuestion(QuestionBean question) {
        this.question = question;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTopicID() {
        return topicID;
    }

    public void setTopicID(String topicID) {
        this.topicID = topicID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static class QuestionBean {
        /**
         * question : 李白是谁
         * question_ws : 李白/NPP//  是/V_SHI//  谁/NP//
         */

        private String question;
        private String question_ws;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getQuestion_ws() {
            return question_ws;
        }

        public void setQuestion_ws(String question_ws) {
            this.question_ws = question_ws;
        }
    }
}
