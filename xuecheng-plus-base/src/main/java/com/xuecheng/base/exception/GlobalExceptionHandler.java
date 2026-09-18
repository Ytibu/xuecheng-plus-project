package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局异常拦截器 —— 后端“错误返回”的唯一出口
 *
 * 前后端约定：成功时 controller 直接返回裸业务对象，不加响应壳；
 * 失败一律 throw，由本拦截器统一转成 RestErrorResponse{errMessage} + HTTP 500。
 * 状态码必须是非 2xx：前端 axios 只在错误拦截器里读 error.response.data.errMessage 弹窗，
 * 若返回 200，前端会当成功处理，提示丢失且继续执行后续刷新逻辑。
 * 提示文案在抛出异常的位置设计(如 XueChengPlusException.cast("自定义提示"))。
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException e)
    {
        log.error("系统已知异常：{}", e.getErrMessage(), e);

        return new RestErrorResponse(e.getErrMessage());
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse methodArgumentNotValidException(MethodArgumentNotValidException e) {

        // 遍历后，将所有错误提示存储
        List<String> errorMsg = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errorMsg.add(fieldError.getDefaultMessage());
        });

        String errMessage = StringUtils.join(errorMsg, ",");
        log.error("系统异常：{}and {}", e.getMessage(), errMessage);

        return new RestErrorResponse(errMessage);
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e)
    {
        log.error("系统未知异常：{}", e.getMessage(), e);

        return new RestErrorResponse(CommonError.UNKNOWN_ERROR.getErrMessage());
    }
}
