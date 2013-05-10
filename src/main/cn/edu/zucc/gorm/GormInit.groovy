package cn.edu.zucc.gorm

import javax.persistence.Entity;

import cn.edu.zucc.common.PackageScanner;
import cn.edu.zucc.gsummer.GSummerConfig;

class GormInit {
	static boolean inited = false
	
	static void init(String resource = null) {
		if (inited) return 
		else inited = true
		
		def list = []
		PackageScanner.scan(GSummerConfig.package) { Class clazz ->
			if (clazz.getAnnotation(Entity) != null) {
				initEntityClass(clazz)
				list.add clazz
			}
		}
		
		GDao.init(resource, list)
	}

	static void initEntityClass(Class clazz) {
		clazz.metaClass.static.get = {GDao.get(clazz, it)}
		clazz.metaClass.static.load = {GDao.load(clazz, it)}
		clazz.metaClass.save = {GDao.save(delegate)}
		clazz.metaClass.saveOnly = {GDao.saveOnly(delegate)}
		clazz.metaClass.update = {GDao.update(delegate)}
		clazz.metaClass.merge = {GDao.merge(delegate, it)}
		clazz.metaClass.delete = {GDao.delete(delegate)}
		
		clazz.metaClass.static.executeUpdate = {String hql, Object...values -> GDao.executeUpdate(clazz, hql, values)}
		
		clazz.metaClass.static.find = {String hql, Object...values -> GDao.find(clazz, hql, values)}
		clazz.metaClass.static.findAll = GDao.&findAll
		clazz.metaClass.static.where = {Object...args->GDao.where(clazz, args)}
		clazz.metaClass.static.loadAll = {GDao.loadAll(clazz)}
		
		clazz.metaClass.static.withTransaction = {GDao.withTransaction(it)}
	}
}
