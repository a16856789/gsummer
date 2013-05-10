package cn.edu.zucc.common

class LangUtils {
	static def tryCloseable(Closeable closeable, Closure closure) {
		try {
			closure(closeable)
		} finally {
			closeable?.close()
		}
	}

	static def fillProperties(def obj, Map map) {
		map.keySet().intersect(obj.metaClass.properties*.name).each {
			obj."$it" = map[it]
		}
	}

	static Map formatMapBean(Map bean) {
		def out = [:]
		bean.each {
			def m = out, pro = it.key.split(/\./)
			if (pro.size() > 1) {
				for (name in pro[0..-2]) {
					m = m[name] ?: (m[name] = [:])
				}
			}
			m[pro[-1]] = it.value
		}
		return out
	}
	
	static boolean getBoolArg(Map map, String name, boolean defaultValue) {
		if (map == null || map[name] == null) return defaultValue;
		def res = map[name];
		map.remove(name);
		return res;
	}
	
	static void checkExtraArgs(Map map) {
		assert map == null || map.isEmpty();
	}
}
