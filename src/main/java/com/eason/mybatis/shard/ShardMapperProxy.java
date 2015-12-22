package com.eason.mybatis.shard;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;
import org.springframework.util.Assert;

import com.eason.mybatis.shard.policy.ShardPolicy;

/**
 * @author longyaokun
 *
 * @param <T>
 */
public class ShardMapperProxy<T> implements InvocationHandler {

	private Class<T> mapperInterface;

	private String[] shardableMethods;

	private SqlSession defaultSqlSession;

	private Map<String, SqlSession> shardSqlSessions = new HashMap<String, SqlSession>();

	private ShardPolicy shardPolicy;

	public ShardMapperProxy() {
	}

	public ShardMapperProxy(Class<T> mapperInterface, String[] methods, SqlSession defaultSqlSession,
	        Map<String, SqlSession> shardSqlSessions, ShardPolicy shardPolicy) {
		super();
		this.mapperInterface = mapperInterface;
		this.shardableMethods = methods;
		this.defaultSqlSession = defaultSqlSession;
		this.shardSqlSessions = shardSqlSessions;
		this.shardPolicy = shardPolicy;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (Object.class.equals(method.getDeclaringClass())) {
			try {
				return method.invoke(this, args);
			} catch (Throwable t) {
				throw ExceptionUtil.unwrapThrowable(t);
			}
		}

		String methodName = method.getName();
		SqlSession sqlSession = null;
		if (this.isMethodShardable(methodName)) {
			Assert.notNull(args, "Please provide the shard id");
			sqlSession = this.shardSqlSessions.get(this.shardPolicy.getDatabaseId(args));
		} else {
			sqlSession = this.defaultSqlSession;
		}
		T mapper = sqlSession.getMapper(this.mapperInterface);
		Object result = MethodUtils.invokeMethod(mapper, methodName, args, method.getParameterTypes());
		return result;
	}

	protected boolean isMethodShardable(String methodName) {
		for (String pattern : this.shardableMethods) {
			if (methodName.equals(pattern)) {
				return true;
			}
		}
		return false;
	}

	public Object invokeMethod(Object methodObject, String methodName, Object[] args) throws Exception {
		Class<?> ownerClass = methodObject.getClass();
		Class<?>[] argsClass = new Class<?>[args.length];
		for (int i = 0, j = args.length; i < j; i++) {
			argsClass[i] = args[i].getClass();
		}
		Method method = ownerClass.getMethod(methodName, argsClass);
		return method.invoke(methodObject, args);
	}

}
