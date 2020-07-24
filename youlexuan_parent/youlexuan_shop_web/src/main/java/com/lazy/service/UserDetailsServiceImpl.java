package com.lazy.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lazy.pojo.TbSeller;
import com.lazy.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证类
 * @author Administrator
 *
 */

public class UserDetailsServiceImpl implements UserDetailsService {
    @Reference
    private SellerService sellerService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        // grantedAuths：该用户拥有的角色List集合

// 得到商家对象
        TbSeller seller = sellerService.findOne(username);

        if (seller != null) {
            // 审核通过
            if (seller.getStatus().equals("1")) {
                return new User(username, seller.getPassword(), grantedAuths);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


}
