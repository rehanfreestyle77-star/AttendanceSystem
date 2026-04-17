package com.example.attendancesystem;

public class Student {
    String studentId;
    String rollNo;
    String name;
    String studentClass;
    String phone;  // Parent Notification ke liye
    String status; // View Report ke liye (Present/Absent)

    // Checkbox ke liye (Attendance mark karte waqt)
    public boolean isPresent = false;

    public Student() {
    }

    // Constructor (Add Student ke waqt use hota hai)
    public Student(String studentId, String rollNo, String name, String studentClass, String phone) {
        this.studentId = studentId;
        this.rollNo = rollNo;
        this.name = name;
        this.studentClass = studentClass;
        this.phone = phone;
        this.status = ""; // Default empty
    }

    // Getters
    public String getStudentId() { return studentId; }
    public String getRollNo() { return rollNo; }
    public String getName() { return name; }
    public String getStudentClass() { return studentClass; }
    public String getPhone() { return phone; }

    // Ye method missing tha, isliye error aa raha tha
    public String getStatus() { return status; }

    // Setter for Status
    public void setStatus(String status) {
        this.status = status;
    }
}