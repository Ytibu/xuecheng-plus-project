package com.xuecheng.base.exception;

import com.xuecheng.base.model.Result;
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
 * 前后端约定(全量壳)：
 * 0. 所有接口(读/写)成功统一返回 Result{code:1, msg, data}；
 * 1. 所有失败响应统一使用 Result 模型：{code:2, msg, data:null}
 * 2. 业务已知/参数校验等“预期内失败”返回 HTTP 200 + code:2，
 *    前端在正常响应分支按 code!=1 判定失败并展示 msg；
 * 3. 未预期异常(系统故障)返回 HTTP 500 + code:2，前端在错误分支读取 msg；
 * 4. 提示文案(msg)在抛出异常的位置设计(如 XuechengPlusException.cast("自定义提示"))，
 *    本拦截器只负责把异常携带的信息放入统一模型返回。
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(XuechengPlusException.class)
    public Result<Void> customException(XuechengPlusException e)
    {
        log.error("系统已知异常：{}", e.getErrMessage(), e);

        return Result.error(e.getErrMessage());
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> methodArgumentNotValidException(MethodArgumentNotValidException e) {

        // 遍历后，将所有错误提示存储
        List<String> errorMsg = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errorMsg.add(fieldError.getDefaultMessage());
        });

        String errMessage = StringUtils.join(errorMsg, ",");
        log.error("系统异常：{}and {}", e.getMessage(), errMessage);

        return Result.error(errMessage);
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> exception(Exception e)
    {
        log.error("系统未知异常：{}", e.getMessage(), e);

        return Result.error(CommonError.UNKNOWN_ERROR.getErrMessage());
    }
}
