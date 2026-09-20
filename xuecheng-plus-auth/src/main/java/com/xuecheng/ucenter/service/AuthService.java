package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * 统一的认证接口
 */
public interface AuthService {

    /**
     * 认证方式
     * @param authParamsDto 认证的用户信息
     * @return 用户信息
     */
    XcUserExt execute(AuthParamsDto authParamsDto);
}
