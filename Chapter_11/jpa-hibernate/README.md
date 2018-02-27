基于JPA的应用程序需要使用EntiryManagerFacory的实现类来获取Entirymanager实例。
- 应用程序管理类型(Application-managed):当应用程序向实体管理工厂直接请求实体管理时，工厂会创建一个实例管理器。需要负责打开、关闭管理器，并在事务中控制。
- 容器管理类型(Container-manager):实体管理器由JavaaEE创建和管理，应用程序不于管理器打交道。实体管理器由注入或JDNI来获取。容器负责配置实体管理器。


```java
public interface EntityManagerFactory {

    public EntityManager createEntityManager();

    public EntityManager createEntityManager(Map map);

    public CriteriaBuilder getCriteriaBuilder();

    public Metamodel getMetamodel();

    public boolean isOpen();

    public void close();

    public Map<String, Object> getProperties();

    public Cache getCache();

    public PersistenceUnitUtil getPersistenceUnitUtil();
}

```
以上两种管理器实现了同一个EntiryManager接口。不管使用哪种，Spirng都会负责管理理论Entirymanager

应用程序管理类型
```java
public class LocalEntityManagerFactoryBean extends AbstractEntityManagerFactoryBean {

	/**
	 * Initialize the EntityManagerFactory for the given configuration.
	 * @throws javax.persistence.PersistenceException in case of JPA initialization errors
	 */
	@Override
	protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
		if (logger.isInfoEnabled()) {
			logger.info("Building JPA EntityManagerFactory for persistence unit '" + getPersistenceUnitName() + "'");
		}
		PersistenceProvider provider = getPersistenceProvider();
		if (provider != null) {
			// Create EntityManagerFactory directly through PersistenceProvider.
			EntityManagerFactory emf = provider.createEntityManagerFactory(getPersistenceUnitName(), getJpaPropertyMap());
			if (emf == null) {
				throw new IllegalStateException(
						"PersistenceProvider [" + provider + "] did not return an EntityManagerFactory for name '" +
						getPersistenceUnitName() + "'");
			}
			return emf;
		}
		else {
			// Let JPA perform its standard PersistenceProvider autodetection.
			return Persistence.createEntityManagerFactory(getPersistenceUnitName(), getJpaPropertyMap());
		}
	}

}
```
容器管理类型：

 ```java
 public class LocalContainerEntityManagerFactoryBean extends AbstractEntityManagerFactoryBean
 		implements ResourceLoaderAware, LoadTimeWeaverAware {

@Override
protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
    PersistenceUnitManager managerToUse = this.persistenceUnitManager;
    if (this.persistenceUnitManager == null) {
        this.internalPersistenceUnitManager.afterPropertiesSet();
        managerToUse = this.internalPersistenceUnitManager;
    }

    this.persistenceUnitInfo = determinePersistenceUnitInfo(managerToUse);
    JpaVendorAdapter jpaVendorAdapter = getJpaVendorAdapter();
    if (jpaVendorAdapter != null && this.persistenceUnitInfo instanceof SmartPersistenceUnitInfo) {
        ((SmartPersistenceUnitInfo) this.persistenceUnitInfo).setPersistenceProviderPackageName(
                jpaVendorAdapter.getPersistenceProviderRootPackage());
    }

    PersistenceProvider provider = getPersistenceProvider();
    if (provider == null) {
        String providerClassName = this.persistenceUnitInfo.getPersistenceProviderClassName();
        if (providerClassName == null) {
            throw new IllegalArgumentException(
                    "No PersistenceProvider specified in EntityManagerFactory configuration, " +
                    "and chosen PersistenceUnitInfo does not specify a provider class name either");
        }
        Class<?> providerClass = ClassUtils.resolveClassName(providerClassName, getBeanClassLoader());
        provider = (PersistenceProvider) BeanUtils.instantiateClass(providerClass);
    }
    if (provider == null) {
        throw new IllegalStateException("Unable to determine persistence provider. " +
                "Please check configuration of " + getClass().getName() + "; " +
                "ideally specify the appropriate JpaVendorAdapter class for this provider.");
    }

    if (logger.isInfoEnabled()) {
        logger.info("Building JPA container EntityManagerFactory for persistence unit '" +
                this.persistenceUnitInfo.getPersistenceUnitName() + "'");
    }
    this.nativeEntityManagerFactory =
            provider.createContainerEntityManagerFactory(this.persistenceUnitInfo, getJpaPropertyMap());
    postProcessEntityManagerFactory(this.nativeEntityManagerFactory, this.persistenceUnitInfo);

    return this.nativeEntityManagerFactory;
}
}
```


```java
@Configuration
@ComponentScan
public class JpaConfig {

  @Bean
  public DataSource dataSource() {
    EmbeddedDatabaseBuilder edb = new EmbeddedDatabaseBuilder();
    edb.setType(EmbeddedDatabaseType.H2);
    edb.addScript("spittr/db/jpa/schema.sql");
    edb.addScript("spittr/db/jpa/test-data.sql");
    EmbeddedDatabase embeddedDatabase = edb.build();
    return embeddedDatabase;
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean emf(DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {
    LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
    emf.setDataSource(dataSource);
    emf.setPersistenceUnitName("spittr");
    emf.setJpaVendorAdapter(jpaVendorAdapter);
    emf.setPackagesToScan("spittr.domain");
    return emf;
  }
  
  @Bean
  public JpaVendorAdapter jpaVendorAdapter() {
    HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
    adapter.setDatabase(Database.H2);
    adapter.setShowSql(true);
    adapter.setGenerateDdl(false);
    adapter.setDatabasePlatform("org.hibernate.dialect.H2Dialect");
    return adapter;
  }
  

  @Configuration
  @EnableTransactionManagement
  public static class TransactionConfig implements TransactionManagementConfigurer {
    @Inject
    private EntityManagerFactory emf;

    public PlatformTransactionManager annotationDrivenTransactionManager() {
      JpaTransactionManager transactionManager = new JpaTransactionManager();
      transactionManager.setEntityManagerFactory(emf);
      return transactionManager;
    }    
  }
}
```

不使用Spring模板的纯代码
```java
 @Repository
 public class JpaSpitterRepository implements SpitterRepository {
 
 	@PersistenceContext                //重点。不会在担心线程安全的问题。
 	private EntityManager entityManager;
 
 	public long count() {
 		return findAll().size();
 	}
 
 	public Spitter save(Spitter spitter) {
 		entityManager.persist(spitter);
 		return spitter;
 	}
 
 	public Spitter findOne(long id) {
 		return entityManager.find(Spitter.class, id);
 	}
 
 	public Spitter findByUsername(String username) {		
 		return (Spitter) entityManager.createQuery("select s from Spitter s where s.username=?").setParameter(1, username).getSingleResult();
 	}
 
 	public List<Spitter> findAll() {
 		return (List<Spitter>) entityManager.createQuery("select s from Spitter s").getResultList();
 	}
 	
 }
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:jdbc="http://www.springframework.org/schema/jdbc"
	xmlns:c="http://www.springframework.org/schema/c" xmlns:context="http://www.springframework.org/schema/context"
	xmlns:p="http://www.springframework.org/schema/p"
	xsi:schemaLocation="http://www.springframework.org/schema/jdbc http://www.springframework.org/schema/jdbc/spring-jdbc-3.1.xsd
		http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.1.xsd">


	<context:component-scan base-package="spittr.db.jpa" />

	<bean id="emf" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean"
		p:dataSource-ref="dataSource" 
    p:persistenceUnitName="spittr"
		p:jpaVendorAdapter-ref="jpaVendorAdapter"
    p:packagesToScan="spittr.domain" />

	<bean id="jpaVendorAdapter" class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter">
		<property name="database" value="H2" />
		<property name="showSql" value="true" />
		<property name="generateDdl" value="false" />
		<property name="databasePlatform" value="org.hibernate.dialect.H2Dialect" />
	</bean>

 	<bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager"
		p:entityManagerFactory-ref="emf" />
		
	<jdbc:embedded-database id="dataSource" type="H2">
		<jdbc:script location="spittr/db/jpa/schema.sql" />
		<jdbc:script location="spittr/db/jpa/test-data.sql" />
	</jdbc:embedded-database>

</beans>
```
