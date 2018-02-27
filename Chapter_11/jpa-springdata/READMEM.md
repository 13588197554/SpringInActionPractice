```java
package org.springframework.data.jpa.repository;


@NoRepositoryBean
public interface JpaRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.CrudRepository#findAll()
	 */
	List<T> findAll();

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.PagingAndSortingRepository#findAll(org.springframework.data.domain.Sort)
	 */
	List<T> findAll(Sort sort);

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.CrudRepository#save(java.lang.Iterable)
	 */
	<S extends T> List<S> save(Iterable<S> entities);

	/**
	 * Flushes all pending changes to the database.
	 */
	void flush();

	/**
	 * Saves an entity and flushes changes instantly.
	 * 
	 * @param entity
	 * @return the saved entity
	 */
	T saveAndFlush(T entity);

	/**
	 * Deletes the given entities in a batch which means it will create a single {@link Query}. Assume that we will clear
	 * the {@link javax.persistence.EntityManager} after the call.
	 * 
	 * @param entities
	 */
	void deleteInBatch(Iterable<T> entities);

	/**
	 * Deletes all entites in a batch call.
	 */
	void deleteAllInBatch();
}

```

```xml
  	<jpa:repositories base-package="spittr.db" />
  	
  	<bean id="emf" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean"
  		p:dataSource-ref="dataSource" 
  		p:persistenceUnitName="spitter"
  		p:jpaVendorAdapter-ref="jpaVendorAdapter" />
  
  	<bean id="jpaVendorAdapter" class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter">
  		<property name="database" value="H2" />
  		<property name="showSql" value="false" />
  		<property name="generateDdl" value="false" />
  	</bean>
  
  	<bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager"
  		p:entityManagerFactory-ref="emf" />
  
  	<jdbc:embedded-database id="dataSource" type="H2">
  		<jdbc:script location="spittr/db/jpa/schema.sql" />
  		<jdbc:script location="spittr/db/jpa/test-data.sql" />
  	</jdbc:embedded-database>
```
```java
@Configuration
@EnableJpaRepositories("com.habuma.spitter.db")
public class SpringDataJpaConfig {
  
  @Bean
  public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .addScript("classpath:/com/habuma/spitter/db/jpa/schema.sql")
        .addScript("classpath:/com/habuma/spitter/db/jpa/test-data.sql")
        .build();
  }
  
  @Bean
  public JpaTransactionManager transactionManager() {
    return new JpaTransactionManager(); // does this need an emf???
  }
  
  @Bean
  public HibernateJpaVendorAdapter jpaVendorAdapter() {
    HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
    adapter.setDatabase(Database.H2);
    adapter.setShowSql(false);
    adapter.setGenerateDdl(true);
    return adapter;
  }
  
  @Bean
  public Object emf() {
    LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
    emf.setDataSource(dataSource());
    emf.setPersistenceUnitName("spitter");
    emf.setJpaVendorAdapter(jpaVendorAdapter());
    return emf;
  }
  
}

```
