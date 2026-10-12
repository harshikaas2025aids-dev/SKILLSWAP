package com.skillswap.dto;

public class StudentRequest {
    private String studentName;
    private String email;
    private String contactInfo;

    public StudentRequest() {
    }

    public StudentRequest(String studentName, String email, String contactInfo) {
        this.studentName = studentName;
        this.email = email;
        this.contactInfo = contactInfo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
}
