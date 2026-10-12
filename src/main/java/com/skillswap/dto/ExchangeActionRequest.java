package com.skillswap.dto;

public class ExchangeActionRequest {
    private int studentId;

    public ExchangeActionRequest() {
    }

    public ExchangeActionRequest(int studentId) {
        this.studentId = studentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
}
