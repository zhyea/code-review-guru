package com.codeguru.review;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 端到端集成测试（@SpringBootTest + 内存 H2）。
 * 未配置 AI 密钥 → 走本地规则评审路径，验证「提交 → 落库 → 详情渲染」全链路。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    /** 只覆盖数据源为内存 H2（不用 application.yml 遮蔽主配置，主配置的 spring-ai 占位 key 等照常生效）。 */
    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:h2:mem:reviewtest;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.h2.console.enabled", () -> "false");
    }

    @Test
    void homePageRenders() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("AI 代码评审")));
    }

    @Test
    void submitThenDetailShowsFindings() throws Exception {
        MvcResult submit = mvc.perform(post("/reviews")
                        .param("title", "登录校验")
                        .param("language", "java")
                        .param("code", "String pwd = \"hardcoded123\";\n"
                                + "String sql = \"SELECT * FROM u WHERE id = \" + id;\n"
                                + "int x = 1; // TODO 收尾\n"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/reviews/*"))
                .andReturn();

        String detailUrl = submit.getResponse().getRedirectedUrl();
        mvc.perform(get(detailUrl))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("评审发现")))
                .andExpect(content().string(containsString("查看被评审源码")))
                .andExpect(content().string(containsString("security")))
                .andExpect(content().string(containsString("RULE")));
    }

    @Test
    void emptyCodeRejectedWithError() throws Exception {
        mvc.perform(post("/reviews").param("language", "java").param("code", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("代码不能为空")));
    }

    @Test
    void historyPageRenders() throws Exception {
        mvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("历史评审")));
    }

    @Test
    void unknownIdReturns404() throws Exception {
        mvc.perform(get("/reviews/99999999"))
                .andExpect(status().isNotFound());
    }
}
