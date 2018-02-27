# 重要接口和实现类

Session接口提供了基本的数据访问功能，如保存、更新、删除以及加载对象的功能。
```java
public interface Session extends SharedSessionContract {

	public void flush() throws HibernateException;

	public SessionFactory getSessionFactory();

	public Connection close() throws HibernateException;

	public void cancelQuery() throws HibernateException;

	public boolean isOpen();

	public boolean isConnected();
	public boolean isDirty() throws HibernateException;

	public boolean isDefaultReadOnly();

	public boolean contains(Object object);

	public Object load(Class theClass, Serializable id, LockMode lockMode);

	public Object load(Class theClass, Serializable id, LockOptions lockOptions);

	public Object load(String entityName, Serializable id, LockOptions lockOptions);

	public Object load(Class theClass, Serializable id);

	public void replicate(Object object, ReplicationMode replicationMode);

	public void saveOrUpdate(String entityName, Object object);

	public Object merge(Object object);

	public Object merge(String entityName, Object object);

	public void persist(String entityName, Object object);

	public void delete(String entityName, Object object);

	public void refresh(Object object);

	public void refresh(String entityName, Object object, LockOptions lockOptions);

	public void clear();

	public Object get(String entityName, Serializable id);

	public Object get(String entityName, Serializable id, LockOptions lockOptions);

}
```

SessionFactory主要负责Hibernater Session的打开、关闭以及管理。通过工厂bean，来获取SessionFactory
```java

public interface SessionFactory extends Referenceable, Serializable {

	public interface SessionFactoryOptions {
		Interceptor getInterceptor();
		EntityNotFoundDelegate getEntityNotFoundDelegate();
	}

	public SessionFactoryOptions getSessionFactoryOptions();

	public Session openSession() throws HibernateException;

	public Session getCurrentSession() throws HibernateException;

	public StatelessSession openStatelessSession();

	public StatelessSession openStatelessSession(Connection connection);

	public CollectionMetadata getCollectionMetadata(String roleName);

	public Map<String,ClassMetadata> getAllClassMetadata();

	public Statistics getStatistics();

	public void close() throws HibernateException;

	public boolean isClosed();

	public TypeHelper getTypeHelper();
}

```
```java
public class LocalSessionFactoryBean extends HibernateExceptionTranslator
		implements FactoryBean<SessionFactory>, ResourceLoaderAware, InitializingBean, DisposableBean {
    
		}
		
  @Bean
  public SessionFactory sessionFactoryBean() {
    try {
      LocalSessionFactoryBean lsfb = new LocalSessionFactoryBean();
      lsfb.setDataSource(dataSource());
      lsfb.setPackagesToScan("spittr.domain");
      Properties props = new Properties();
      props.setProperty("dialect", "org.hibernate.dialect.H2Dialect");
      lsfb.setHibernateProperties(props);
      lsfb.afterPropertiesSet();
      SessionFactory object = lsfb.getObject();
      return object;
    } catch (IOException e) {
      return null;
    }
  }
```

HibernateTemplate 能够保证每个事务使用同一个Session。但是这种方式会直接与Spirng耦合。


```java
package spittr.db.hibernate4;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import spittr.db.SpitterRepository;
import spittr.domain.Spitter;

@Repository
public class HibernateSpitterRepository implements SpitterRepository {

	private SessionFactory sessionFactory;        

	@Inject                                          // 注入SessionFactory
	public HibernateSpitterRepository(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;		
	}
	
	private Session currentSession() {
		return sessionFactory.getCurrentSession();  //从SessionFactory中获取Session。
	}
	
	public long count() {
		return findAll().size();
	}

	public Spitter save(Spitter spitter) {
		Serializable id = currentSession().save(spitter);  //使用当前事务的Session
		return new Spitter((Long) id, 
				spitter.getUsername(), 
				spitter.getPassword(), 
				spitter.getFullName(), 
				spitter.getEmail(), 
				spitter.isUpdateByEmail());
	}

	public Spitter findOne(long id) {
		return (Spitter) currentSession().get(Spitter.class, id); 
	}

	public Spitter findByUsername(String username) {		
		return (Spitter) currentSession() 
				.createCriteria(Spitter.class) 
				.add(Restrictions.eq("username", username))
				.list().get(0);
	}

	public List<Spitter> findAll() {
		return (List<Spitter>) currentSession() 
				.createCriteria(Spitter.class).list(); 
	}
	
}

```

```java
@SuppressWarnings("serial")
public class PersistenceExceptionTranslationPostProcessor extends AbstractAdvisingBeanPostProcessor
		implements BeanFactoryAware {

	private Class<? extends Annotation> repositoryAnnotationType = Repository.class;


	/**
	 * Set the 'repository' annotation type.
	 * The default repository annotation type is the {@link Repository} annotation.
	 * <p>This setter property exists so that developers can provide their own
	 * (non-Spring-specific) annotation type to indicate that a class has a
	 * repository role.
	 * @param repositoryAnnotationType the desired annotation type
	 */
	public void setRepositoryAnnotationType(Class<? extends Annotation> repositoryAnnotationType) {
		Assert.notNull(repositoryAnnotationType, "'repositoryAnnotationType' must not be null");
		this.repositoryAnnotationType = repositoryAnnotationType;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		if (!(beanFactory instanceof ListableBeanFactory)) {
			throw new IllegalArgumentException(
					"Cannot use PersistenceExceptionTranslator autodetection without ListableBeanFactory");
		}
		this.advisor = new PersistenceExceptionTranslationAdvisor(
				(ListableBeanFactory) beanFactory, this.repositoryAnnotationType);
	}

}

```
