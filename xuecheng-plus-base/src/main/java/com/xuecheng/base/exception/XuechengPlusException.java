package com.xuecheng.base.exception;

import lombok.Getter;

@Getter
public class XuechengPlusException extends RuntimeException {

    private String errMessage;

    public XuechengPlusException() {
        super();
    }

    public XuechengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

   public String getErrMessage(){
        return errMessage;
   }

    public static void cast(String message) {
        throw new XuechengPlusException(message);
    }

    public static void cast(CommonError error) {
        throw new XuechengPlusException(error.getErrMessage());
    }
}
