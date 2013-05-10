package cn.edu.zucc.gweb

import groovy.json.JsonBuilder;
import groovy.lang.GroovyShell;
import groovy.lang.MetaClass;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.codehaus.groovy.runtime.InvokerHelper;

class ElFunctions {
	public static Object g(String t) {
		GroovyShell shell = new GroovyShell();
		return shell.evaluate(t);
	}
	
	public static String date(Date date, String format) {
		return date?.format(format)
	}
	
	public static String format(String format, Object value) {
		return String.format(format, value)
	}
	
	public static String json(Object o) {
		return new JsonBuilder(o).toString()
	}

	public static Map<String, Object> meta(final Object o) {
		final MetaClass m = o != null ? InvokerHelper.getMetaClass(o) : null;

		return new AbstractMap<String, Object>() {
			@Override
			public Object get(Object key) {
				return m?.getProperty(o, (String) key);
			}

			@Override
			public Set<Entry<String, Object>> entrySet() {
				return null;
			}
		};
	}
}
