package com.rucart.app.model;

import java.io.Serializable;

public class DeliveryIssueReportRequest implements Serializable {
    private String issueType;
    private String details;

    public DeliveryIssueReportRequest() {
    }

    public DeliveryIssueReportRequest(String issueType, String details) {
        this.issueType = issueType;
        this.details = details;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
