package com.codeguru.review.mapper;

import com.codeguru.review.domain.ReviewFinding;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 评审发现的数据访问。 */
public interface ReviewFindingMapper {

    @Insert("INSERT INTO review_finding(request_id, severity, category, line, title, detail, "
            + "suggestion, created_at) "
            + "VALUES(#{requestId}, #{severity}, #{category}, #{line}, #{title}, #{detail}, "
            + "#{suggestion}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReviewFinding finding);

    /** 按请求聚合：先按严重度（ERROR>WARNING>INFO），再按行号升序。 */
    @Select("<script>"
            + "SELECT * FROM review_finding WHERE request_id = #{requestId} "
            + "ORDER BY (CASE severity WHEN 'ERROR' THEN 0 WHEN 'WARNING' THEN 1 ELSE 2 END), "
            + "COALESCE(line, 2147483647)"
            + "</script>")
    List<ReviewFinding> listByRequestId(@Param("requestId") long requestId);
}
