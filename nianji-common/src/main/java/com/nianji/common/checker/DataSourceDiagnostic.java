package com.nianji.common.checker;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;


import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Map;

// @Slf4j
// @Component
public class DataSourceDiagnostic implements ApplicationRunner {

    // @Autowired
    // private ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // log.info("=== 数据源诊断开始 ===");
        //
        // // 1. 查找所有的 DataSource Bean
        // String[] dataSourceBeanNames = applicationContext.getBeanNamesForType(DataSource.class);
        // log.info("找到 {} 个 DataSource Bean: {}",
        //         dataSourceBeanNames.length, Arrays.toString(dataSourceBeanNames));
        //
        // for (String beanName : dataSourceBeanNames) {
        //     DataSource dataSource = applicationContext.getBean(beanName, DataSource.class);
        //     log.info("DataSource Bean '{}': {}", beanName, dataSource.getClass().getName());
        //
        //     // 尝试获取连接信息
        //     try (Connection connection = dataSource.getConnection()) {
        //         String url = connection.getMetaData().getURL();
        //         String database = connection.getCatalog();
        //         log.info("  - URL: {}", url);
        //         log.info("  - 数据库: {}", database);
        //         log.info("  - 驱动: {}", connection.getMetaData().getDriverName());
        //     } catch (Exception e) {
        //         log.warn("  无法获取连接信息: {}", e.getMessage());
        //     }
        // }
        //
        // // 2. 查找所有的 SqlSessionFactory Bean
        // String[] sqlSessionFactoryNames = applicationContext.getBeanNamesForType(SqlSessionFactory.class);
        // log.info("找到 {} 个 SqlSessionFactory Bean: {}",
        //         sqlSessionFactoryNames.length, Arrays.toString(sqlSessionFactoryNames));
        //
        // // 3. 检查是否有 @Primary 标注的数据源
        // Map<String, DataSource> dataSources = applicationContext.getBeansOfType(DataSource.class);
        //
        // for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
        //     BeanDefinition beanDefinition = ((ConfigurableApplicationContext) applicationContext).getBeanFactory().getBeanDefinition(entry.getKey());
        //     if (beanDefinition.isPrimary()) {
        //         log.info("发现主数据源: {}", entry.getKey());
        //     }
        // }
        //
        // log.info("=== 数据源诊断结束 ===");
    }
}