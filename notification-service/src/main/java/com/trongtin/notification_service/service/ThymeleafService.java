package com.trongtin.notification_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
public class ThymeleafService {

    @Autowired
    private SpringTemplateEngine templateEngine;

    /**
     * Tạo nội dung HTML từ template name và các biến dữ liệu.
     * @param templateName Tên file template Thymeleaf (ví dụ: "application-submitted")
     * @param variables Map chứa các biến (ví dụ: jobTitle, recruiterName)
     * @return Nội dung email dưới dạng chuỗi HTML
     */
    public String buildEmailContent(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        // Thymeleaf sẽ tìm template trong src/main/resources/templates/
        return templateEngine.process(templateName, context);
    }
}