package com.eason.dao.mapper;

import org.apache.ibatis.annotations.Param;

import com.eason.dao.entity.User;

public interface UserMapper {

	public User getUser(@Param("uid") String uid);

	public void update(User user);
	
	public void add(User user);
}
