package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

/**
 * 微信认证接口
 */
public interface WxAuthService {

    /**
     * 微信扫码认证
     * @param code 授权码
     * @return 用户信息
     */
    XcUser wxAuth(String code);

}