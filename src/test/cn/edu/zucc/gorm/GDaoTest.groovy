package cn.edu.zucc.gorm
//
//import groovy.json.JsonBuilder;
//
//import org.junit.Test;
//
//import cn.edu.zucc.woldas.test.TestEnvirnment;
//import cn.edu.zucc.woldas.weibo.Status;
//import cn.edu.zucc.woldas.weibo.User;
//
//class GDaoTest {
//	@Test
//	void test() {
//		TestEnvirnment.init();
//		
//		def user = new User(id: 1L, name: 'summer');
//		user.save();
//		GDao.withTransaction {
//			def u = GDao.currentSession.merge(user);
//			println u.is(user);
//		}
//		GDao.withTransaction {
//			user.save();
//			def u = GDao.currentSession.merge(new User(id: 1L, name: 'summer1'));
//			println u.is(user);
//		}
//	}
//	
//	@Test
//	void testMerge() {
//		TestEnvirnment.init();
//		
//		def user = new User(id: 1L, name: 'summer');
//		def status = new Status(id: 1L, text: 'summer', user: user);
//		
//		try {
//			status.merge();
//			assert false
//		} catch(Exception e) {
//		}
//		
//		GDao.withTransaction {
//			def s1 = GDao.merge(status, [forceCascade: true]);
//			status.text = 'summer2';
//			def s2 = GDao.merge(status, [forceCascade: true]);
//			assert s1.is(s2);
//			assert !s1.user.is(user);
//			assert s1.text == 'summer2';
//			
//			status.text = 'summer3';
//			def s3 = status.merge(forceCascade: true, updateWhenExist: false);
//			assert s3.text == 'summer2';
//			
//			println new JsonBuilder(s1).toPrettyString();
//		}
//	}
//}
