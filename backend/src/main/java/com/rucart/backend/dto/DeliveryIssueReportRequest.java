package com.rucart.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class DeliveryIssueReportRequest {

    @NotBlank(message = "Issue type is required")
    private String issueType; // CUSTOMER_UNREACHABLE, INCORRECT_ADDRESS, CUSTOMER_REJECTED, DAMAGED_PACKAGE, OTHER

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
