package com.nianji.common.checker;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Properties;

// @Slf4j
// @Component
// @Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class SqlInterceptor implements Interceptor {
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        // BoundSql boundSql = statementHandler.getBoundSql();
        //
        // String sql = boundSql.getSql();
        // Object parameterObject = boundSql.getParameterObject();
        //
        // log.info("=== SQL 拦截器捕获 ===");
        // log.info("原始SQL: {}", sql);
        // log.info("参数: {}", parameterObject);
        // log.info("参数映射: {}", boundSql.getParameterMappings());
        //
        // // 检查是否是分页查询
        // if (statementHandler instanceof RoutingStatementHandler) {
        //     MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        //     Object delegate = metaObject.getValue("delegate");
        //     if (delegate instanceof PreparedStatementHandler) {
        //         MetaObject delegateMeta = SystemMetaObject.forObject(delegate);
        //         Object mappedStatement = delegateMeta.getValue("mappedStatement");
        //         log.info("MappedStatement: {}", mappedStatement);
        //     }
        // }
        //
        // log.info("=== SQL 拦截器结束 ===");
        //
        // return invocation.proceed();

        return null;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}