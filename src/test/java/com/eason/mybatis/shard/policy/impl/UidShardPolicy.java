package com.eason.mybatis.shard.policy.impl;

import com.eason.dao.entity.User;
import com.eason.mybatis.shard.policy.ShardPolicy;

public class UidShardPolicy implements ShardPolicy {

	private int shards;

	public UidShardPolicy(int shards) {
		super();
		this.shards = shards;
	}

	public String getDatabaseId(Object[] args) {
		String uid = null;
		Object first = args[0];
		if (first instanceof String) {
			uid = (String) first;
		} else if (first instanceof User) {
			uid = User.class.cast(first).getUid();
		}
		return String.valueOf(Math.abs(uid.hashCode()) % this.shards + 1);
	}
}
