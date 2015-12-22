package com.eason.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eason.dao.entity.User;
import com.eason.dao.mapper.UserMapper;

@Service("userService")
public class UserService {

	@Autowired
	private UserMapper userMapper;
	
	public void update(User user){
		this.userMapper.update(user);
	}
	
	public User getUser(String uid){
		return this.userMapper.getUser(uid);
	}
	
	public void add(User user){
		this.userMapper.add(user);
	}
}
