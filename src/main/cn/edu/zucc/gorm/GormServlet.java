package cn.edu.zucc.gorm;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

@SuppressWarnings("serial")
public class GormServlet extends HttpServlet {
	@Override
	public void init(ServletConfig config) throws ServletException {
		GormInit.init();
	}

	@Override
	public void destroy() {
	}
}
