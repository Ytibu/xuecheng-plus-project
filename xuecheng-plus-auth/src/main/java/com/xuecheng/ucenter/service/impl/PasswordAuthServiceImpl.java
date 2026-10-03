package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    private XcUserMapper xcUserMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto)
    {
        // 确认账号存在
        String userName = authParamsDto.getUsername();

        // TODO @xuecheng-plus-checkcode 临时取消图形验证码校验，便于不启动 checkcode 服务时进行日常测试，恢复时启用以下代码
//        String checkCode = authParamsDto.getCheckcode();
//        String checkCodeKey = authParamsDto.getCheckcodekey();
//        if(StringUtils.isEmpty(checkCode) || StringUtils.isEmpty(checkCodeKey)){
//            throw new RuntimeException("请输入验证码");
//        }
//        Boolean verify = checkCodeClient.verify(checkCodeKey, checkCode);
//        if (verify == null || !verify){
//            throw new RuntimeException("验证码输入错误");
//        }

        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, userName));
        if(user == null){
            throw new RuntimeException("账号不存在");
        }

        // 密码校验
        String passwordDb = user.getPassword();
        String passwordForm = authParamsDto.getPassword();
        boolean matches = passwordEncoder.matches(passwordForm, passwordDb);
        if(!matches){
            throw new RuntimeException("账号或密码错误");
        }

        // 返回封装的用户信息
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        return xcUserExt;
    }
}
