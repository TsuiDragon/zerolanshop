package cn.zerolan.zerolanshop.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，统一接口错误响应，避免控制器重复编写 try-catch。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务校验异常目前由 Service 层通过 RuntimeException 抛出。
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException exception) {
        return Result.error(exception.getMessage());
    }

    /**
     * 未预期异常统一返回通用错误，避免把堆栈信息暴露给前端。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        return Result.error("服务器内部错误");
    }
}
