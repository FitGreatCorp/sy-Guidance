package com.fitgreat.airfacerobot.speech.model;


public class ShowContentMain {

    private String content1;
    private String content2 =null;

    public String getContent1() {
        return content1;
    }

    public void setContent1(String content1) {
        this.content1 = content1;
    }

    public String getContent2() {
        return content2;
    }

    public void setContent2(String content2) {
        this.content2 = content2;
    }

    @Override
    public String toString() {
        return "ShowContent{" +
                "content1='" + content1 + '\'' +
                ", content2='" + content2 + '\'' +
                '}';
    }
}
