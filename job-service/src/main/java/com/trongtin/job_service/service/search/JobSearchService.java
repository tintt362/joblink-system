package com.trongtin.job_service.service.search;

import co.elastic.clients.json.JsonData;
import com.trongtin.job_service.entity.JobDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public JobSearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }


    /**
     * Tìm kiếm JobDocument theo query text, location và minSalary.
     *
     * @param query     từ khoá full-text (title, description)
     * @param location  vị trí (exact match)
     * @param minSalary giá tối thiểu (lọc range)
     * @param page      chỉ số trang (0-based)
     * @param size      kích thước trang
     * @return Page<JobDocument> kết quả phân trang
     */
    public Page<JobDocument> searchJobs(String query, String location, Integer minSalary, int page, int size) {

        // --- Chuẩn bị list chứa các câu query cho phần should (tăng relevance) và filter (lọc chính xác)
        List<Query> shouldQueries = new ArrayList<>(); // các query ảnh hưởng tới điểm relevance (score)
        List<Query> filterQueries = new ArrayList<>(); // các query kiểu filter (không ảnh hưởng score)

        // --- 1) Full-text search: nếu có query string -> multiMatch
        // Đây là phần không bắt buộc (should). Tăng trọng số cho title bằng cú pháp "title^3".
        if (query != null && !query.isEmpty()) {
            shouldQueries.add(Query.of(q -> q.multiMatch(m -> m
                    .query(query)
                    .fields("title^3", "description"))));
        }

        // --- 2) Exact filter cho location (term query)
        // Term query đặt vào filterQueries để làm bộ lọc (không ảnh hưởng score, chỉ loại/bỏ doc)
        if (location != null && !location.isEmpty()) {
            filterQueries.add(Query.of(q -> q.term(t -> t
                    .field("location")
                    // value() dùng builder để tạo FieldValue; tuỳ version client có thể khác
                    .value(v -> v.stringValue(location))
            )));
        }

        // --- 3) Range filter cho salaryMax (ví dụ: chỉ lấy job có salaryMax >= minSalary)
        // Sử dụng range.untyped(...) để tương thích nhiều kiểu dữ liệu (number, date,...)
        if (minSalary != null && minSalary > 0) {
            filterQueries.add(Query.of(q -> q.range(r -> r
                    .untyped(ur -> ur
                            .field("salaryMax")
                            // .from(...) tương đương >= (inclusive). Tuỳ version có thể dùng gte(...)
                            .from(JsonData.of(minSalary))
                    )
            )));
        }

        // --- 4) Xây dựng native query kết hợp bool { should, filter, minimum_should_match }
        //  - should: danh sách match (tăng relevance)
        //  - filter: lọc chính xác (không ảnh hưởng score)
        //  - minimumShouldMatch("1"): nếu có shouldQueries thì cần ít nhất 1 should match
        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .should(shouldQueries)
                        .filter(filterQueries)
                        .minimumShouldMatch("1") // nếu có shouldQueries, ít nhất 1 phải match
                ))
                // --- 5) Thêm phân trang (PageRequest)
                .withPageable(PageRequest.of(page, size));

        // --- 6) Thực thi truy vấn qua Spring's ElasticsearchOperations
        // elasticsearchOperations.search(...) trả về SearchHits<JobDocument>
        var searchHits = elasticsearchOperations.search(queryBuilder.build(), JobDocument.class);

        // --- 7) Chuyển SearchHits -> List<JobDocument>
        // Mỗi SearchHit chứa metadata + content; ta lấy content làm entity model
        List<JobDocument> jobs = searchHits.getSearchHits()
                .stream()
                .map(hit -> hit.getContent()) // hoặc getContent() tuỳ API
                .toList();

        // --- 8) Gói kết quả vào PageImpl để trả về Page<JobDocument>
        // totalHits = tổng số document thỏa điều kiện (dùng cho tính tổng trang)
        return new PageImpl<>(jobs, PageRequest.of(page, size), searchHits.getTotalHits());
    }

}
