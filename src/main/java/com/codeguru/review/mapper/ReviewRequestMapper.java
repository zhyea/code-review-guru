package com.codeguru.review.mapper;

import com.codeguru.review.domain.ReviewRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 评审请求的数据访问。 */
public interface ReviewRequestMapper {

    @Insert("INSERT INTO review_request(title, language, source_code, source_size, reviewer_type, "
            + "model, summary, score, status, created_at) "
            + "VALUES(#{title}, #{language}, #{sourceCode}, #{sourceSize}, #{reviewerType}, "
            + "#{model}, #{summary}, #{score}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReviewRequest request);

    @Select("SELECT * FROM review_request WHERE id = #{id}")
    ReviewRequest findById(@Param("id") long id);

    /** 历史列表：按 id 倒序（最新在前）。 */
    @Select("SELECT * FROM review_request ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}")
    List<ReviewRequest> listRecent(@Param("limit") int limit, @Param("offset") long offset);

    @Select("SELECT COUNT(*) FROM review_request")
    long count();
}
