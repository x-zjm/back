## 设计原则

### 异常和响应结果设计原则

* Controller层：只返回BizResult，不处理异常
* Service层：混合使用
    - 业务逻辑错误：返回 BizResult
    - 基础设施错误：抛出异常
    - 系统级错误：抛出异常
* Infrastructure层：主要使用异常
* 工具类/工具方法：使用异常

### 断言

* ParamAssert：参数校验，HTTP 400错误
* BusinessAssert：业务逻辑校验，HTTP 422错误