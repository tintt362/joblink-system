package com.trongtin.notification_service.model;


import lombok.Data;
import java.io.Serializable;

@Data
public class ApplicationSubmittedEvent implements Serializable {
    private String traceId; // Để phục vụ Distributed Tracing
    private String candidateName;
    private String recruiterEmail; // Địa chỉ email nhận thông báo
    private String jobTitle;
    private Long applicationId;
}