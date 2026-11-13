package com.codeguru.review;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.codeguru.review.mapper")
public class CodeReviewGuruApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeReviewGuruApplication.class, args);
    }
}
