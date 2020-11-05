package com.fitgreat.airfacerobot.launcher.model;


public class MyException extends RuntimeException{
    private String exceptionType;
    private String exceptionContent;

    public MyException(String exceptionType, String exceptionContent) {
        this.exceptionType = exceptionType;
        this.exceptionContent = exceptionContent;
    }

    public MyException(String message, String exceptionType, String exceptionContent) {
        super(message);
        this.exceptionType = exceptionType;
        this.exceptionContent = exceptionContent;
    }

    public MyException(String message, Throwable cause, String exceptionType, String exceptionContent) {
        super(message, cause);
        this.exceptionType = exceptionType;
        this.exceptionContent = exceptionContent;
    }

    public MyException(Throwable cause, String exceptionType, String exceptionContent) {
        super(cause);
        this.exceptionType = exceptionType;
        this.exceptionContent = exceptionContent;
    }
}
