package com.lazy.seckill.service.impl;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证类：本质认证工作由cas完成<br>
 * 此类主要负责在认证后返回角色列表
 */
class UserDetailServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 构建角色集合
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 往角色中添加用户角色
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new User(username, "", authorities);
    }

}
