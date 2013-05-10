package cn.edu.zucc.gorm;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class SessionFilter implements Filter {
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		try {
			GDao.startOpeningSession();
			chain.doFilter(request, response);
		} finally {
			GDao.endOpeningSession();
			SessionContext.close();
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}
