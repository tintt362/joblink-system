package com.trongtin.job_service.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@Document(indexName = "jobs_index")
public class JobDocument {

    @Id
    private UUID id;

    // Tìm kiếm toàn văn (analyzed)
    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    // Lọc chính xác (non-analyzed)
    @Field(type = FieldType.Keyword)
    private String location;

    // Lọc theo mảng kỹ năng
    @Field(type = FieldType.Keyword)
    private List<String> skills;

    // Lọc theo phạm vi (Range Query)
    @Field(type = FieldType.Integer)
    private Integer salaryMin;

    @Field(type = FieldType.Integer)
    private Integer salaryMax;
}