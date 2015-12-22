package com.eason.mybatis.shard;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.eason.dao.entity.User;
import com.eason.service.UserService;

public class ShardTest {

	private ApplicationContext ctx=new ClassPathXmlApplicationContext("spring-context.xml");
	
	private UserService userService= (UserService) ctx.getBean("userService");
	
	@Test
	public void testAdd(){
		User user=new User();
		user.setUid("101");
		user.setUsername("eason");
		user.setPassword("123456");
		userService.add(user);
	}
	
	@Test
	public void testUpdate(){
		User user=new User();
		user.setUid("101");
		user.setUsername("eason3");
		user.setPassword("12345678");
		userService.update(user);
	}
	
	@Test
	public void testQuery(){
		User user=userService.getUser("101");
		System.out.println(ToStringBuilder.reflectionToString(user));
	}
}
