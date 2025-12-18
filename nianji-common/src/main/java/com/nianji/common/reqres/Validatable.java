package com.nianji.common.reqres;

/**
 * 可校验接口
 * 用于标记需要自定义校验的请求数据
 */
public interface Validatable {
    
    /**
     * 执行参数校验
     * 校验失败时抛出 ValidationException
     */
    void validate();
}
