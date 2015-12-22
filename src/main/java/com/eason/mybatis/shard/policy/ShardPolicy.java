package com.eason.mybatis.shard.policy;

/**
 * @author longyaokun
 *
 */
public interface ShardPolicy {

	public String getDatabaseId(Object[] args);
	
}
