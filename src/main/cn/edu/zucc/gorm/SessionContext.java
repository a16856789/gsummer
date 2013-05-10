package cn.edu.zucc.gorm;

import org.hibernate.HibernateException;
import org.hibernate.classic.Session;
import org.hibernate.context.CurrentSessionContext;
import org.hibernate.engine.SessionFactoryImplementor;

@SuppressWarnings("serial")
public class SessionContext implements CurrentSessionContext {
	
	public static ThreadLocal<Session> sessionLocal = new ThreadLocal<>();
	
	@Override
	public Session currentSession() throws HibernateException {
		Session session = sessionLocal.get();
		if (session == null || !session.isOpen()) {
			session = GDao.getSessionFactory().openSession();
			sessionLocal.set(session);
		}
		return session;
	}
	
	public SessionContext(SessionFactoryImplementor factory) {
	}
	
	public static void close() {
		Session session = sessionLocal.get();
		if (session != null && session.isOpen()) {
			session.close();
		}
		sessionLocal.remove();
	}
}
