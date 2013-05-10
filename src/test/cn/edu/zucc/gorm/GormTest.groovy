package cn.edu.zucc.gorm
//
//import jodd.bean.BeanUtil;
//
//import org.hibernate.ScrollMode;
//import org.hibernate.Session;
//
//import cn.edu.zucc.woldas.weibo.Status;
//import cn.edu.zucc.woldas.weibo.User;
//
//GormInit.init("/hibernate.mem.xml");
//// GormInit.init();
//
//new User(id: 1L, name: 'summer').save()
//new User(id: 2L, name: 'summer').save()
//
//// test lazy
//new Status(id: 1L, user: new User(id: 1L)).save()
//println BeanUtil.getDeclaredProperty(Status.get(1L).user, "id")
//println Status.get(1L).user.@id
//
//println GDao.findAll(query: 'FROM User WHERE id = ?', values: [1L])
//println GDao.findAll('FROM User WHERE id = ?', 1L)
//println User.findAll('FROM User WHERE id = ?', 1L, max : 1)
//
//println User.where([query: 'id = ?', values: [1L]])
//println User.where('id = ?', 1L)
//println User.where('id = ?', 1L, max : 1)
//
//GDao.withTransaction {
//	def list = User.where('id = ?', 1L, max : 1);
//	println list[0].name
//}
//
//long s, e;
//
//s = System.currentTimeMillis();
//int cnt = GDao.find('SELECT COUNT(*) FROM User');
//e = System.currentTimeMillis();
//println "${e - s} ms"
//
//println cnt;
//
//println User.find('name = ?', 'summer')
//println User.find('FROM User WHERE name = ?', 'summer')
//println User.find('')
//println User.where('')
//println User.class.getSimpleName()
//
//return
//
//User.findAll('FROM User', max: 10, scroll: true) {
//	println it.id
//}
//println 'ttt'
//User.findAll('FROM User', offset: 10, max: 10, scroll: true) {
//	println it.id
//}