package com.lazy.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.lazy.mapper.TbUserMapper;
import com.lazy.pojo.TbUser;
import com.lazy.user.service.UserService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * user服务实现层
 * @author lazy
 *
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private TbUserMapper userMapper;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private RedisTemplate redisTemplate;


	@Autowired
	private ActiveMQQueue smsQueue;


	/**
	 * 增加
	 */
	@Override
	public void add(TbUser user) {
		user.setCreated(new Date());//创建日期
		user.setUpdated(new Date());//修改日期
		String password= DigestUtils.md5Hex(user.getPassword());//对密码加密
		user.setPassword(password);
		userMapper.insert(user);		
	}

	@Override
	public void sendMsg(String phone) {
		System.out.println(phone);
		//生成6位随机数
		String code=(long)(Math.random()*1000000)+"";
		redisTemplate.boundHashOps("smscode").put(phone,code);
		//发送到activeMQ
		Map<String,String> map = new HashMap<>();
		map.put("phone",phone);
		map.put("sign_name","优乐选");
		map.put("template_code","SMS_195871257");
		map.put("param","{\"code\":\""+code+"\"}");

		jmsTemplate.convertAndSend(smsQueue,map);
		System.out.println("发送验证码成功："+code);
	}


	@Override
	public boolean checkSmsCode(String phone, String code) {
		//得到缓存中存储的验证码
		String sysCode=(String)redisTemplate.boundHashOps("smscode").get(phone);
		if(sysCode==null){
			return false;
		}
		if(!sysCode.equals(code)){
			return false;
		}
		return true;
	}
}
