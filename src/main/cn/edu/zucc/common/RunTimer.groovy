package cn.edu.zucc.common

class RunTimer {
	long start = System.currentTimeMillis()
	long end   = 0
	
	long stop() {
		end = System.currentTimeMillis()
		return end - start
	}
}
