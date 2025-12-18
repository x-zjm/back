package com.nianji.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MyBatisPlusConfig {

    public MyBatisPlusConfig() {
        log.debug("MyBatisPlusConfig 被初始化了");
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 使用更明确的构造方式
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // paginationInnerInterceptor.setOverflow(false); // 关闭溢出处理，看是否有影响
        // paginationInnerInterceptor.setMaxLimit(500L); // 明确设置最大限制

        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }

}