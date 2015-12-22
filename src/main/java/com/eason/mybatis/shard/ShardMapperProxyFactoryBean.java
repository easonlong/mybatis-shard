package com.eason.mybatis.shard;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.FactoryBean;

import com.eason.mybatis.shard.policy.ShardPolicy;

/**
 * @author longyaokun
 *
 * @param <T>
 */
public class ShardMapperProxyFactoryBean<T> implements FactoryBean<T> {

	private Class<T> mapperInterface;

	private String[] shardableMethods;

	private SqlSessionFactory defaultSqlSessionFactory;

	private Map<String, SqlSessionFactory> shardSqlSessionFactorys;

	private SqlSession defaultSqlSession;

	private Map<String, SqlSession> shardSqlSessions = new HashMap<String, SqlSession>();

	private ShardPolicy shardPolicy;

	@SuppressWarnings("unchecked")
	public T getObject() throws Exception {
		ShardMapperProxy<T> shardMapperProxy = new ShardMapperProxy<T>(mapperInterface, shardableMethods,
		        defaultSqlSession, shardSqlSessions, shardPolicy);
		return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, shardMapperProxy);
	}

	public Class<T> getObjectType() {
		return this.mapperInterface;
	}

	public boolean isSingleton() {
		return true;
	}

	public Class<T> getMapperInterface() {
		return mapperInterface;
	}

	public void setMapperInterface(Class<T> mapperInterface) {
		this.mapperInterface = mapperInterface;
	}

	public String[] getShardableMethods() {
		return shardableMethods;
	}

	public void setShardableMethods(String[] shardableMethods) {
		this.shardableMethods = shardableMethods;
	}

	public SqlSessionFactory getDefaultSqlSessionFactory() {
		return defaultSqlSessionFactory;
	}

	public void setDefaultSqlSessionFactory(SqlSessionFactory defaultSqlSessionFactory) {
		this.defaultSqlSessionFactory = defaultSqlSessionFactory;
		this.defaultSqlSession = new SqlSessionTemplate(defaultSqlSessionFactory);
	}

	public Map<String, SqlSessionFactory> getShardSqlSessionFactorys() {
		return shardSqlSessionFactorys;
	}

	public void setShardSqlSessionFactorys(Map<String, SqlSessionFactory> shardSqlSessionFactorys) {
		this.shardSqlSessionFactorys = shardSqlSessionFactorys;
		for (String key : shardSqlSessionFactorys.keySet()) {
			this.shardSqlSessions.put(key, new SqlSessionTemplate(shardSqlSessionFactorys.get(key)));
		}
	}

	public ShardPolicy getShardPolicy() {
		return shardPolicy;
	}

	public void setShardPolicy(ShardPolicy shardPolicy) {
		this.shardPolicy = shardPolicy;
	}
}
