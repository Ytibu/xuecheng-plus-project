package com.xuecheng.base.model;

import lombok.Data;

/**
 * 前后端统一响应模型（全量壳，规范化约定）
 * 所有接口(读/写)统一返回 Result，code: 1=成功 2=失败；msg: 提示信息；data: 业务数据
 * 业务代码只 return 成功(Result.success()/success(data))；
 * 失败一律 throw，由全局异常拦截器(GlobalExceptionHandler)统一生成 Result.error(msg)。
 */
@Data
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(){
        Result<T> result = new Result<>();
        result.code = 1;
        result.msg = "success";
        return result;
    }

    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.data = data;
        result.code = 1;
        result.msg = "success";
        return result;
    }

    public static <T> Result<T> error(){
        Result<T> result = new Result<>();
        result.code = 2;
        result.msg = "error";
        return result;
    }

    public static <T> Result<T> error(String msg){
        Result<T> result = new Result<>();
        result.code = 2;
        result.msg = msg;
        return result;
    }
}
