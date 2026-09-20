package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 根据用户名查找密码并封装成 UserDetails返回给SpringSecurity框架
     * @param userName 用户名
     * @return 封装的UserDetails
     * @throws UsernameNotFoundException 没有用户异常
     */
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

        AuthParamsDto authParamsDto;
        try {
            authParamsDto = JSON.parseObject(userName, AuthParamsDto.class);
        }catch (Exception e){
            throw new RuntimeException("请求认证参数不符合要求");
        }

        String authType = authParamsDto.getAuthType();
        AuthService authService = applicationContext.getBean(authType + "_authservice", AuthService.class);
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        return getUserPrincipal(xcUserExt);
    }

    /**
     * 查询用户信息
     * @param user  用户id，主键
     * @return UserDetails 用户信息
     */
    public UserDetails getUserPrincipal(XcUserExt user){
        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
        String[] authorities = {"test"};
        String password = user.getPassword();
        //为了安全在令牌中不放密码
        user.setPassword(null);
        //将user对象转json
        String userString = JSON.toJSONString(user);
        //创建UserDetails对象
        return User.withUsername(userString).password(password ).authorities(authorities).build();
    }
}
