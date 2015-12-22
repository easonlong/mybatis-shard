package com.eason.mybatis.shard;

import static org.springframework.util.Assert.notNull;

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

import com.eason.mybatis.shard.policy.ShardPolicy;

/**
 * @author longyaokun
 *
 */
public class ProxyMapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean,
        ApplicationContextAware, BeanNameAware {

	private String basePackage;

	private ApplicationContext applicationContext;

	private String beanName;

	private BeanNameGenerator nameGenerator;

	private boolean processPropertyPlaceHolders;

	private SqlSessionFactory defaultSqlSessionFactory;

	private String defaultSqlSessionFactoryBeanName;

	private Map<String, SqlSessionFactory> shardSqlSessionFactorys;
	
	private Map<String, String> shardSqlSessionFactoryBeanNames;

	private String shardableMethods;
	
	private ShardPolicy shardPolicy;

	public void setBasePackage(String basePackage) {
		this.basePackage = basePackage;
	}

	/**
  */
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
  */
	public void setBeanName(String name) {
		this.beanName = name;
	}

	/**
	 * Gets beanNameGenerator to be used while running the scanner.
	 *
	 * @return the beanNameGenerator BeanNameGenerator that has been configured
	 * @since 1.2.0
	 */
	public BeanNameGenerator getNameGenerator() {
		return nameGenerator;
	}

	/**
  */
	public void setNameGenerator(BeanNameGenerator nameGenerator) {
		this.nameGenerator = nameGenerator;
	}

	/**
  */
	public void afterPropertiesSet() throws Exception {
		notNull(this.basePackage, "Property 'basePackage' is required");
		notNull(this.shardableMethods, "Property 'shardableMethods' is required");
	}

	/**
  */
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
	}

	/**
  */
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

		if (this.processPropertyPlaceHolders) {
			processPropertyPlaceHolders();
		}

		ProxyClassPathMapperScanner scanner = new ProxyClassPathMapperScanner(registry);
		scanner.setResourceLoader(this.applicationContext);
		scanner.setBeanNameGenerator(this.nameGenerator);
		scanner.setDefaultSqlSessionFactory(defaultSqlSessionFactory);
		scanner.setDefaultSqlSessionFactoryBeanName(defaultSqlSessionFactoryBeanName);
		scanner.setShardableMethods(StringUtils.tokenizeToStringArray(this.shardableMethods,
		        ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
		scanner.setShardPolicy(this.shardPolicy);
		if (this.shardSqlSessionFactorys != null) {
			scanner.setShardSqlSessionFactorys(this.shardSqlSessionFactorys);
		}
		if (this.shardSqlSessionFactoryBeanNames != null && !this.shardSqlSessionFactoryBeanNames.isEmpty()) {
			scanner.setShardSqlSessionFactoryBeanNames(this.shardSqlSessionFactoryBeanNames);
		}
		scanner.registerFilters();
		scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage,
		        ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
	}

	public SqlSessionFactory getDefaultSqlSessionFactory() {
		return defaultSqlSessionFactory;
	}

	public void setDefaultSqlSessionFactory(SqlSessionFactory defaultSqlSessionFactory) {
		this.defaultSqlSessionFactory = defaultSqlSessionFactory;
	}

	public String getDefaultSqlSessionFactoryBeanName() {
		return defaultSqlSessionFactoryBeanName;
	}

	public void setDefaultSqlSessionFactoryBeanName(String defaultSqlSessionFactoryBeanName) {
		this.defaultSqlSessionFactoryBeanName = defaultSqlSessionFactoryBeanName;
	}

	public Map<String, SqlSessionFactory> getShardSqlSessionFactorys() {
		return shardSqlSessionFactorys;
	}

	public void setShardSqlSessionFactorys(Map<String, SqlSessionFactory> shardSqlSessionFactorys) {
		this.shardSqlSessionFactorys = shardSqlSessionFactorys;
	}

	public String getShardableMethods() {
		return shardableMethods;
	}

	public void setShardableMethods(String shardableMethods) {
		this.shardableMethods = shardableMethods;
	}

	private void processPropertyPlaceHolders() {
		Map<String, PropertyResourceConfigurer> prcs = applicationContext
		        .getBeansOfType(PropertyResourceConfigurer.class);

		if (!prcs.isEmpty() && applicationContext instanceof GenericApplicationContext) {
			BeanDefinition mapperScannerBean = ((GenericApplicationContext) applicationContext).getBeanFactory()
			        .getBeanDefinition(beanName);

			DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
			factory.registerBeanDefinition(beanName, mapperScannerBean);

			for (PropertyResourceConfigurer prc : prcs.values()) {
				prc.postProcessBeanFactory(factory);
			}

			PropertyValues values = mapperScannerBean.getPropertyValues();

			this.basePackage = updatePropertyValue("basePackage", values);
		}
	}

	private String updatePropertyValue(String propertyName, PropertyValues values) {
		PropertyValue property = values.getPropertyValue(propertyName);

		if (property == null) {
			return null;
		}

		Object value = property.getValue();

		if (value == null) {
			return null;
		} else if (value instanceof String) {
			return value.toString();
		} else if (value instanceof TypedStringValue) {
			return ((TypedStringValue) value).getValue();
		} else {
			return null;
		}
	}

	public void setProcessPropertyPlaceHolders(boolean processPropertyPlaceHolders) {
		this.processPropertyPlaceHolders = processPropertyPlaceHolders;
	}

	public ShardPolicy getShardPolicy() {
	    return shardPolicy;
    }

	public void setShardPolicy(ShardPolicy shardPolicy) {
	    this.shardPolicy = shardPolicy;
    }

	public Map<String, String> getShardSqlSessionFactoryBeanNames() {
	    return shardSqlSessionFactoryBeanNames;
    }

	public void setShardSqlSessionFactoryBeanNames(Map<String, String> shardSqlSessionFactoryBeanNames) {
	    this.shardSqlSessionFactoryBeanNames = shardSqlSessionFactoryBeanNames;
    }

}
