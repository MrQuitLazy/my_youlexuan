package com.lazy.user.service;

import java.util.List;
import com.lazy.pojo.TbUser;

import com.lazy.entity.PageResult;
/**
 * user服务层接口
 * @author lazy
 *
 */
public interface UserService {

	
	/**
	 * 增加
	*/
	public void add(TbUser user);

	/**
	 * 发送短信
	 * @param phone 当前页 码
	 * @return
	 */
    void sendMsg(String phone);

	boolean checkSmsCode(String phone, String code);
}
