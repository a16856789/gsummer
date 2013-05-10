package cn.edu.zucc.gsummer

import cn.edu.zucc.common.LangUtils;

class GSummerConfig {
	static final String CONF_FILE = "/app.properties";

	static final Properties conf = new Properties();

	static {
		LangUtils.tryCloseable(GSummerConfig.class.getResourceAsStream(CONF_FILE)) {
			if (it == null) throw new Exception("Can't find config file $CONF_FILE");
			conf.load(it);
		}
	}

	public static String getPackage() {
		return conf.get("app.package");
	}
	
	public static String getHomePage() {
		return conf.get("app.homepage");
	}

	public static void main(String[] args) {
		println this.package
	}
}
