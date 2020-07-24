package com.lazy.user.controller;
import java.util.List;

import com.lazy.util.PhoneFormatCheckUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.lazy.pojo.TbUser;
import com.lazy.user.service.UserService;

import com.lazy.entity.PageResult;
import com.lazy.entity.Result;
/**
 * usercontroller
 * @author lazy
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;

	/**
	 * 增加
	 * @param user
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbUser user,String code){
		boolean checkSmsCode=userService.checkSmsCode(user.getPhone(), code);
		if(!checkSmsCode){
			return new Result(false, "验证码输入错误！");
		}
		try{
			userService.add(user);
			return new Result(true,"增加成功");
		}catch(Exception e){
			e.printStackTrace();
			return new Result(false,"增加失败");
		}
	}
	/**
	 * 发送短信
	 * @param phone
	 * @return
	 */
	@RequestMapping("/sendMsg")
	public Result sendCode(String phone){
		//判断手机号格式
		System.out.println(phone);
		if(!PhoneFormatCheckUtils.isPhoneLegal(phone)){
			return new Result(false,"手机号格式不正确");
		}
		try{
			userService.sendMsg(phone);//生成验证码
			return new Result(true,"验证码发送成功");
		}catch(Exception e){
			e.printStackTrace();
			return new Result(true,"验证码发送失败");
		}
	}
}
