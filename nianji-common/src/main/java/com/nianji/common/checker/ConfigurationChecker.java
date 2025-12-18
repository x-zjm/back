package com.nianji.common.checker;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
// @Component
public class ConfigurationChecker implements ApplicationRunner {
    
    // @Autowired
    // private ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // log.info("=== 开始检查 MyBatis 相关配置 ===");
        //
        // // 检查所有的 SqlSessionFactory
        // String[] sqlSessionFactoryNames = applicationContext.getBeanNamesForType(SqlSessionFactory.class);
        // log.info("找到 {} 个 SqlSessionFactory Bean: {}",
        //         sqlSessionFactoryNames.length, Arrays.toString(sqlSessionFactoryNames));
        //
        // for (String beanName : sqlSessionFactoryNames) {
        //     SqlSessionFactory factory = applicationContext.getBean(beanName, SqlSessionFactory.class);
        //     Configuration configuration = factory.getConfiguration();
        //     log.info("Bean '{}' 的 Configuration 类: {}", beanName, configuration.getClass().getName());
        //
        //     if (configuration.getInterceptors() != null) {
        //         log.info("Bean '{}' 有 {} 个拦截器:", beanName, configuration.getInterceptors().size());
        //         for (Interceptor interceptor : configuration.getInterceptors()) {
        //             log.info("  - {}", interceptor.getClass().getName());
        //             if (interceptor instanceof MybatisPlusInterceptor) {
        //                 MybatisPlusInterceptor mpInterceptor = (MybatisPlusInterceptor) interceptor;
        //                 List<InnerInterceptor> inners = mpInterceptor.getInterceptors();
        //                 log.info("    包含 {} 个内部拦截器:", inners.size());
        //                 for (InnerInterceptor inner : inners) {
        //                     log.info("      * {}", inner.getClass().getSimpleName());
        //                 }
        //             }
        //         }
        //     } else {
        //         log.warn("Bean '{}' 没有拦截器!", beanName);
        //     }
        // }
        //
        // log.info("=== 配置检查完成 ===");
    }
}