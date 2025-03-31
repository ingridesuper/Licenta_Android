package com.example.licentaagain.models;

public class ProblemSignature {
    private String problemId;
    private String userId;

    private ProblemSignature(){

    }

    private ProblemSignature(String problemId, String userId){
        this.problemId=problemId;
        this.userId=userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProblemId() {
        return problemId;
    }

    public void setProblemId(String problemId) {
        this.problemId = problemId;
    }

    @Override
    public String toString() {
        return "ProblemSignature{" +
                "userId='" + userId + '\'' +
                ", problemId='" + problemId + '\'' +
                '}';
    }
}
