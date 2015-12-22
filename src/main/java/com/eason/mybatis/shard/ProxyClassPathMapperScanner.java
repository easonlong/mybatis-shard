package com.eason.mybatis.shard;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;

import com.eason.mybatis.shard.policy.ShardPolicy;

/**
 * @author longyaokun
 *
 */
public class ProxyClassPathMapperScanner extends ClassPathBeanDefinitionScanner {

	private String[] shardableMethods;
	
	private ShardPolicy shardPolicy;

	private SqlSessionFactory defaultSqlSessionFactory;
	
	private Map<String, SqlSessionFactory> shardSqlSessionFactorys;

	private String defaultSqlSessionFactoryBeanName;
	
	private Map<String, String> shardSqlSessionFactoryBeanNames;

	public ProxyClassPathMapperScanner(BeanDefinitionRegistry registry) {
		super(registry, false);
	}

	public void registerFilters() {
		boolean acceptAllInterfaces = true;

		if (acceptAllInterfaces) {
			// default include filter that accepts all classes
			addIncludeFilter(new TypeFilter() {
				public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
				        throws IOException {
					return true;
				}
			});
		}

		// exclude package-info.java
		addExcludeFilter(new TypeFilter() {
			public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
			        throws IOException {
				String className = metadataReader.getClassMetadata().getClassName();
				return className.endsWith("package-info");
			}
		});
	}

	@Override
	public Set<BeanDefinitionHolder> doScan(String... basePackages) {
		Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

		if (beanDefinitions.isEmpty()) {
			logger.warn("No MyBatis mapper was found in '" + Arrays.toString(basePackages)
			        + "' package. Please check your configuration.");
		} else {
			for (BeanDefinitionHolder holder : beanDefinitions) {
				GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();

				if (logger.isDebugEnabled()) {
					logger.debug("Creating WriteReadMapperProxyFactoryBean with name '" + holder.getBeanName()
					        + "' and '" + definition.getBeanClassName() + "' mapperInterface");
				}

				definition.getPropertyValues().add("mapperInterface", definition.getBeanClassName());
				
				definition.setBeanClass(ShardMapperProxyFactoryBean.class);

				definition.getPropertyValues().add("shardableMethods", this.shardableMethods);

				definition.getPropertyValues().add("shardPolicy", this.shardPolicy);
				
				if (StringUtils.hasText(this.defaultSqlSessionFactoryBeanName)) {
					definition.getPropertyValues().add("defaultSqlSessionFactory",
					        new RuntimeBeanReference(this.defaultSqlSessionFactoryBeanName));
				} else if (this.defaultSqlSessionFactory != null) {
					definition.getPropertyValues().add("defaultSqlSessionFactory", this.defaultSqlSessionFactory);
				}
				if (shardSqlSessionFactoryBeanNames != null) {
					ManagedMap<String,Object> refMap = new ManagedMap<String, Object>();
					for(String key: this.shardSqlSessionFactoryBeanNames.keySet()){
						refMap.put(key, new RuntimeBeanReference(this.shardSqlSessionFactoryBeanNames.get(key)));
					}
					definition.getPropertyValues().add("shardSqlSessionFactorys", refMap);
				} else if (this.shardSqlSessionFactorys != null) {
					definition.getPropertyValues().add("shardSqlSessionFactorys", this.shardSqlSessionFactorys);
				}
			}
		}

		return beanDefinitions;
	}

	/**
	 */
	@Override
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		return (beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
		if (super.checkCandidate(beanName, beanDefinition)) {
			return true;
		} else {
			logger.warn("Skipping ShardMapperProxyFactoryBean with name '" + beanName + "' and '"
			        + beanDefinition.getBeanClassName() + "' mapperInterface"
			        + ". Bean already defined with the same name!");
			return false;
		}
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
	}

	public Map<String, SqlSessionFactory> getShardSqlSessionFactorys() {
		return shardSqlSessionFactorys;
	}

	public void setShardSqlSessionFactorys(Map<String, SqlSessionFactory> shardSqlSessionFactorys) {
		this.shardSqlSessionFactorys = shardSqlSessionFactorys;
	}

	public String getDefaultSqlSessionFactoryBeanName() {
		return defaultSqlSessionFactoryBeanName;
	}

	public void setDefaultSqlSessionFactoryBeanName(String defaultSqlSessionFactoryBeanName) {
		this.defaultSqlSessionFactoryBeanName = defaultSqlSessionFactoryBeanName;
	}

	public Map<String, String> getShardSqlSessionFactoryBeanNames() {
		return shardSqlSessionFactoryBeanNames;
	}

	public void setShardSqlSessionFactoryBeanNames(Map<String, String> shardSqlSessionFactoryBeanNames) {
		this.shardSqlSessionFactoryBeanNames = shardSqlSessionFactoryBeanNames;
	}

	public ShardPolicy getShardPolicy() {
	    return shardPolicy;
    }

	public void setShardPolicy(ShardPolicy shardPolicy) {
	    this.shardPolicy = shardPolicy;
    }



}
