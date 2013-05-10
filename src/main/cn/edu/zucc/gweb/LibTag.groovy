package cn.edu.zucc.gweb

import static cn.edu.zucc.common.LangUtils.*;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

class LibTag extends TagSupport {

	String libs

	@Override
	public int doStartTag() throws JspException {
		initLibMap()

		def out = pageContext.out
		for (name in libs.split(' ')) {
			if (name.endsWith(".js")) {
				out.println """<script src="${name}"></script>"""
				continue
			}
			if(name.endsWith(".css")) {
				out.println """<link href="${name}" rel="stylesheet" />"""
				continue
			}
			name = name.toUpperCase().replaceAll(/[-_]/, "")
			for (String file in libMap[name]) {
				if (file.endsWith(".js")) {
					out.println """<script src="..${file}"></script>"""
				} else if (file.endsWith(".css")) {
					out.println """<link href="..${file}" rel="stylesheet" />"""
				}
			}
		}

		return SKIP_BODY
	}

	static Map libMap = null

	public void initLibMap() {
		if (libMap != null) {
			// return
		}

		libMap = [:]
		tryCloseable(pageContext.servletContext.getResourceAsStream("/WEB-INF/webLibConifg.groovy")) {
			def t = new GroovyShell().evaluate(it)
			t.each {k, v -> libMap.put(k.toUpperCase().replaceAll(/[-_]/, ""), v)}
		}
	}
}
