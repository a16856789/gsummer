package cn.edu.zucc.gorm;

import groovy.lang.Closure;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import jodd.bean.BeanUtil;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.Type;

import static cn.edu.zucc.common.LangUtils.*;

public class GDao {
	/** SessionFactory */
	private static SessionFactory sessionFactory;

	/** ��ǰ�̵߳��������� */
	private static ThreadLocal<Integer> transactionLocal = new ThreadLocal<>();

	/** ��ǰ�Ƿ���Work */
	private static ThreadLocal<Boolean> isInWorkLocal = new ThreadLocal<>();

	/** ��ʼ�� */
	protected static void init() {
		sessionFactory = new Configuration().configure().buildSessionFactory();
	}

	/** ��ʼ�� */
	protected static void init(String resource) {
		sessionFactory = new Configuration().configure(resource)
				.buildSessionFactory();
	}

	/** ��ʼ�� */
	protected static void init(String resource, List<Class<?>> classes) {
		Configuration conf = new Configuration();
		if (resource != null) {
			conf.configure(resource);
		} else {
			conf.configure();
		}
		if (classes != null) {
			for (Class<?> c : classes) {
				conf.addAnnotatedClass(c);
			}
		}

		conf.buildMappings();
		Iterator<PersistentClass> it = conf.getClassMappings();
		while (it.hasNext()) {
			PersistentClass c = it.next();
			for (Field f : c.getMappedClass().getDeclaredFields()) {
				if (f.getAnnotation(Id.class) != null) {
					c.getProperty(f.getName()).setPropertyAccessorName(
							"property");
				}
			}
		}

		sessionFactory = conf.buildSessionFactory();
	}

	/** ��ȡһ������ */
	protected static Transaction getTransaction(Session session) {
		Transaction t = session.beginTransaction();

		Integer ref = transactionLocal.get();
		if (ref == null) {
			ref = 1;
		} else {
			ref = ref + 1;
		}
		transactionLocal.set(ref);

		return t;
	}

	/** �ύһ������ */
	protected static void commitTransaction() {
		Integer ref = transactionLocal.get() - 1;
		if (ref == 0) {
			Session session = sessionFactory.getCurrentSession();
			Transaction transaction = session.getTransaction();
			transaction.commit();
			transactionLocal.remove();
			if (isInWorkLocal.get() == null) {
				session.close();
			}
		} else {
			transactionLocal.set(ref);
		}
	}

	/** �ع�һ������ */
	protected static void rollbackTransaction() {
		Integer ref = transactionLocal.get() - 1;
		if (ref == 0) {
			Session session = sessionFactory.getCurrentSession();
			Transaction transaction = session.getTransaction();
			transaction.rollback();
			transactionLocal.remove();
			if (isInWorkLocal.get() == null) {
				session.close();
			}
		} else {
			transactionLocal.set(ref);
		}
	}

	/** getSessionFactory */
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/** getCurrentSession */
	public static Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	/** {@link Session#get(Object)} */
	@SuppressWarnings("unchecked")
	public static <T> T get(Class<T> clazz, Serializable id) {
		Session session = sessionFactory.getCurrentSession();
		getTransaction(session);

		T entity = (T) session.get(clazz, id);

		commitTransaction();
		return entity;
	}

	/** {@link Session#load(Object)} */
	@SuppressWarnings("unchecked")
	public static <T> T load(Class<T> clazz, Serializable id) {
		Session session = sessionFactory.getCurrentSession();
		getTransaction(session);

		T entity = (T) session.load(clazz, id);

		commitTransaction();
		return entity;
	}

	/** {@link Session#saveOrUpdate(Object)} */
	public static Object save(Object entity) {
		Session session = sessionFactory.getCurrentSession();
		getTransaction(session);

		session.saveOrUpdate(entity);

		commitTransaction();
		return entity;
	}

	/** {@link Session#save(Object)} */
	public static Object saveOnly(Object entity) {
		Session session = sessionFactory.getCurrentSession();
		getTransaction(session);

		session.save(entity);

		commitTransaction();
		return entity;
	}

	/** {@link Session#update(Object)} */
	public static Object update(Object entity) {
		Session session = sessionFactory.getCurrentSession();
		getTransaction(session);

		session.update(entity);

		commitTransaction();
		return entity;
	}

	/** {@link Session#merge(Object)} */
	public static Object merge(Object entity) {
		return merge(entity, null);
	}

	/** {@link Session#merge(Object)} */
	public static Object merge(Object entity, Map<String, Object> args) {
		// Ϊnull��ֱ������
		if (entity == null) {
			return null;
		}

		// ׼������
		Map<String, Object> functionArgs = args == null ? new HashMap<String, Object>()
				: new HashMap<String, Object>(args);
		boolean updateWhenExist = getBoolArg(args, "updateWhenExist", true);
		boolean forceCascade = getBoolArg(args, "forceCascade", false);
		checkExtraArgs(args);

		// ��ʼ����
		Session session = sessionFactory.getCurrentSession();
		getTransaction(session);
		try {
			// �������Ҫǿ�Ƽ�����ֱ��merge
			if (!forceCascade) {
				Object result = session.merge(entity);
				commitTransaction();
				return result;
			}

			// Ԫ��Ϣ
			ClassMetadata meta = sessionFactory.getClassMetadata(entity
					.getClass());

			// ��ȡԭ�ȵ�
			Serializable id = (Serializable) BeanUtil.getDeclaredProperty(
					entity, meta.getIdentifierPropertyName());
			Object old = session.get(entity.getClass(), id);

			// �������Ҫ���£����ڴ���ʱֱ�ӷ���
			if (!updateWhenExist && old != null) {
				commitTransaction();
				return old;
			}

			// ������
			String[] names = meta.getPropertyNames();
			Type[] types = meta.getPropertyTypes();
			for (int i = 0; i < types.length; i++) {
				if (!(types[i] instanceof ManyToOneType)) {
					continue;
				}
				String name = names[i];

				Object value = BeanUtil.getDeclaredProperty(entity, name);
				merge(value, functionArgs);
			}

			// merge
			Object result = session.merge(entity);
			commitTransaction();
			return result;
		} catch (Exception e) {
			rollbackTransaction();
			throw e;
		}
	}

	/** {@link Session#delete(Object)} */
	public static Object delete(Object entity) {
		Session session = sessionFactory.getCurrentSession();
		getTransaction(session);

		session.delete(entity);

		commitTransaction();
		return entity;
	}

	/** {@link Query#executeUpdate()} */
	public static int executeUpdate(String hql, Object... values) {
		Session session = sessionFactory.getCurrentSession();
		getTransaction(session);

		Query query = session.createQuery(hql);
		int i = 0;
		for (Object v : values) {
			query.setParameter(i++, v);
		}
		int result = query.executeUpdate();

		commitTransaction();
		return result;
	}

	/** {@link Query#executeUpdate()} */
	public static int executeUpdate(Class<?> clazz, String hql,
			Object... values) {
		Session session = sessionFactory.getCurrentSession();
		getTransaction(session);

		if (hql.toUpperCase().startsWith("SET")) {
			hql = "UPDATE " + clazz.getSimpleName() + " " + hql;
		}

		Query query = session.createQuery(hql);
		int i = 0;
		for (Object v : values) {
			query.setParameter(i++, v);
		}
		int result = query.executeUpdate();

		commitTransaction();
		return result;
	}

	/** {@link SQLQuery#executeUpdate()} */
	public static int executeSqlUpdate(String sql, Object... values) {
		Session session = sessionFactory.getCurrentSession();
		getTransaction(session);

		SQLQuery query = session.createSQLQuery(sql);
		int i = 0;
		for (Object v : values) {
			query.setParameter(i++, v);
		}
		int result = query.executeUpdate();

		commitTransaction();
		return result;
	}

	/** {@link Query#executeUpdate()} */
	public static Object find(String hql, Object... values) {
		List<Object> list = new ArrayList<>();
		for (Object v : values) {
			list.add(v);
		}

		Map<String, Object> map = new HashMap<>();
		map.put("query", hql);
		map.put("values", list);
		map.put("max", 1);

		List<?> result = findAll(map);
		return result.size() > 0 ? result.get(0) : null;
	}

	/** {@link Query#executeUpdate()} */
	public static Object find(Class<?> clazz, String hql, Object... values) {
		List<Object> list = new ArrayList<>();
		for (Object v : values) {
			list.add(v);
		}

		String hqlUpper = hql.trim().toUpperCase();
		if (!hqlUpper.startsWith("SELECT") && !hqlUpper.startsWith("FROM")) {
			if (hql != null && !hql.isEmpty()) {
				hql = "FROM " + clazz.getSimpleName() + " WHERE " + hql;
			} else {
				hql = "FROM " + clazz.getSimpleName();
			}
		}

		Map<String, Object> map = new HashMap<>();
		map.put("query", hql);
		map.put("values", list);
		map.put("max", 1);

		List<?> result = findAll(map);
		return result.size() > 0 ? result.get(0) : null;
	}

	/** {@link Session#list()} */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<?> findAll(Object... args) {
		Session session = sessionFactory.getCurrentSession();
		getTransaction(session);

		// �������
		Map<String, Object> map = null;
		Closure closure = null;
		int hqlArg = 0, valuesArg = 1, valuesEnd = args.length - 1; // HQL��Values��args�����е��±�
		if (args[0] instanceof Map) {
			map = (Map<String, Object>) args[0];
			hqlArg = 1;
			valuesArg = 2;
		}
		if (args.length > 0 && args[args.length - 1] instanceof Closure) {
			valuesEnd = args.length - 2;
			closure = (Closure) args[args.length - 1];
		}

		// ׼��HQL���
		String hql = null;
		if (map != null && map.containsKey("query")) {
			hql = (String) map.get("query");
		} else if (args.length > hqlArg) {
			hql = (String) args[hqlArg];
		}

		// ������ѯ
		Query query = session.createQuery(hql);
		query.setReadOnly(true);

		// ���ò���
		if (args.length > valuesArg) {
			for (int i = valuesArg; i <= valuesEnd; i++) {
				query.setParameter(i - valuesArg, args[i]);
			}
		} else if (map != null && map.containsKey("values")) {
			List<?> values = (List<?>) map.get("values");
			int i = 0;
			for (Object v : values) {
				query.setParameter(i++, v);
			}
		}

		// ��������
		if (map != null && map.containsKey("max")) {
			query.setMaxResults((Integer) map.get("max"));
		}
		if (map != null && map.containsKey("offset")) {
			query.setFirstResult((Integer) map.get("offset"));
		}

		// ��ѯ������
		List<?> list = null;
		if (map != null && map.containsKey("scroll")
				&& (Boolean) map.get("scroll")) {
			query.setCacheable(false);
			query.setFetchSize(Integer.MIN_VALUE);
			ScrollableResults result = query.scroll(ScrollMode.FORWARD_ONLY);
			while (result.next()) {
				Object[] o = result.get();
				closure.call(o);
			}
			result.close();
		} else {
			list = query.list();
			if (closure != null) {
				for (Object o : list) {
					closure.setDelegate(o);
					closure.call(o);
				}
			}
		}
		commitTransaction();
		return list;
	}

	/** {@link Session#list()} */
	@SuppressWarnings("unchecked")
	public static Object where(Class<?> clazz, Object... args) {
		// �������
		if (args[0] instanceof Object[]) {
			args = (Object[]) args[0];
		}
		Map<String, Object> map = null;
		int hqlArg = 0; // HQL��Values��args�����е��±�
		if (args[0] instanceof Map) {
			map = (Map<String, Object>) args[0];
			hqlArg = 1;
		}

		// ׼��HQL���
		String hql = null;
		if (map != null && map.containsKey("query")) {
			hql = (String) map.get("query");
		} else if (args.length > hqlArg) {
			hql = (String) args[hqlArg];
		}
		if (hql != null && !hql.isEmpty()) {
			hql = "FROM " + clazz.getSimpleName() + " WHERE " + hql;
		} else {
			hql = "FROM " + clazz.getSimpleName();
		}

		// д�����
		if (map != null) {
			map.put("query", hql);
		} else {
			args[0] = hql;
		}

		// ����
		return findAll(args);
	}

	/** {@link Session#list()} */
	public static List<?> loadAll(Class<?> clazz) {
		String hql = "FROM " + clazz.getSimpleName();
		Map<String, Object> map = new HashMap<>();
		map.put("query", hql);
		return findAll(map);
	}

	/** withTransaction */
	public static Object withTransaction(Closure<Object> closure) {
		Session session = sessionFactory.getCurrentSession();
		getTransaction(session);

		try {
			Object result = closure.call();
			commitTransaction();
			return result;
		} catch (Exception e) {
			rollbackTransaction();
			throw e;
		}
	}

	/** ��ʼһ��һֱ�����session������֮�������ʾ�ĵ���endOpeningSession */
	public static void startOpeningSession() {
		isInWorkLocal.set(true);
	}

	/** ����һ��һֱ�����session */
	public static void endOpeningSession() {
		isInWorkLocal.remove();
		sessionFactory.getCurrentSession().close();
	}

	/** ��OpeningSession��Χ�������е���䶼��ͬһ��session�� */
	public static void withOpeningSession(Closure<?> closure) {
		try {
			startOpeningSession();
			closure.call();
		} finally {
			endOpeningSession();
		}
	}
}
