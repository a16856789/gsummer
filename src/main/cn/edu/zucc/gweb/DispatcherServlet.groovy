package cn.edu.zucc.gweb

import static cn.edu.zucc.common.LangUtils.*

import java.util.List;

import cn.edu.zucc.common.*;
import cn.edu.zucc.gsummer.GSummerConfig;
import groovy.json.JsonBuilder;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

protected class JsonResult {
	def view
}

protected class RedirectResult {
	String url
}

class DispatcherServlet extends HttpServlet {
	/** Ĭ�ϱ��� */
	final static String DEFAULT_ENCODEING = "UTF-8"
	
	/** ��ʽ����ѯ�ַ����������ı��� */
	def decodeQueryString(Map map, String query, String encodeing) {
		if (!query) return
		query.split('&').each { s ->
			def vals = s.split('=')
			if (vals.length >= 2) {
				def k = vals[0], v = vals[1] ?: ""
				map[k] = URLDecoder.decode(v, encodeing)
			}
		}
		map['queryString'] = query
	}
	
	/** service */
	void service(HttpServletRequest request, HttpServletResponse response) {
		// �����ַ���
		request.setCharacterEncoding(DEFAULT_ENCODEING)
		response.setCharacterEncoding(DEFAULT_ENCODEING)
		
		// ��������URI
		def uri = request.servletPath.split(/[\/\\]/)
		if (uri.size() <= 2) {
			response.sendRedirect(GSummerConfig.homePage)
			return
		}
		def ctrlName = uri[1], ctrlMethod = uri[2].find(/[\w\_]*/)

		// ���������ಢ��ʼ��
		def isAjax = request.getHeader("X-Requested-With") == 'XMLHttpRequest';
		def ctrl = getCtrl(ctrlName + ".ctrl")
		def params = new HashMap(request.parameterMap).each {it.value = it.value[0]}
		decodeQueryString(params, request.queryString, DEFAULT_ENCODEING)
		ctrl.metaClass.json = {view -> new JsonResult(view: view)}
		ctrl.metaClass.redirect = {url -> new RedirectResult(url : url)}
		ctrl.metaClass.params = params
		ctrl.metaClass.request = request
		request.setAttribute("params", params);
		
		// ��У��
		def res = null;
		if (uri[2].endsWith(".validate")) {
			res = invokeValidator(ctrl, ctrlMethod);
		} else {
			res = invokeAction(ctrl, ctrlMethod);
		}

		// ����View
		response.setHeader("Cache-Control", "no-cache")
		if(res instanceof JsonResult) {
			def view = new JsonBuilder(res.view).toPrettyString()
			response.setContentType("application/json")
			tryCloseable(response.writer) {it.append(view)}
		} else if(res instanceof RedirectResult) {
			response.sendRedirect(res.url)
		} else {
			if (res instanceof Map) res.each {k, v -> request.setAttribute(k, v)}
			def view = "/${ctrlName}/${ctrlMethod}.jsp"
			if (res != null && res['view']) {
				view = res['view']
			}
			request.getRequestDispatcher(view).forward(request, response)
		}
	}
	
	/** ִ��Action */
	def invokeAction(def ctrl, def methodName) {
		return ctrl.invokeMethod(methodName, null)
	}
	
	/** ִ��У�� */
	def invokeValidator(def ctrl, def methodName) {
		if (ctrl.metaClass.getMetaMethod(methodName + "Validator", null) == null) {
			return new JsonResult(view: [success: true]);
		}
		
		Map validators = ctrl.invokeMethod(methodName + "Validator", null);
		
		def validations = new Validations();		
		def errors = [];
		def params = ctrl.params;
		def __name = params['__name'];
		validators.each { String name, Map validator ->
			if (__name && __name != name) return;
			String val = params[name]; assert val != null;
			for (en in validator) {
				String type = en.key; def args = en.value;
				if (type.endsWith("Message")) {
					continue;
				}
				def e = validations.invokeMethod(type, [args, name, val]);
				if (e) {
					errors.add([name, e]);
					break;
				}
			}
		}
		
		if (errors) {
			return new JsonResult(view: [success: false, errors: errors]);
		} else {
			return new JsonResult(view: [success: true]);
		}
	}
	
	/** ����У���� */
	class Validations {	
		String required(def args, String name, String val) {
			if (val.isEmpty()) {
				if (args instanceof String) {
					return args;
				} else {
					return "���ֶα���!";
				}
			}
			return null;
		}
		
		String matches(def args, String name, String val) {
			return null;
		}
		
		String validator(def args, String name, String val) {
			return args(val);
		}
	}
	
	/** ������ */
	Map<String, Class> ctrls = [:]
	
	/** �������ַ���һ����������ʵ�� */
	def getCtrl(String name) {
		return ctrls[name].newInstance()
	}

	/** ��ʼ�� */
	void init(ServletConfig config) {	
		PackageScanner.scan(GSummerConfig.package) { Class clazz ->
			if (!clazz.getAnnotation(Controller)) return
			def name = clazz.simpleName[0].toLowerCase() + clazz.simpleName[1..-5] + ".ctrl"
			ctrls[name] = clazz
		}
		super.init(config)
	}
}
