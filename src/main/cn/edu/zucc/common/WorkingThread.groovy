package cn.edu.zucc.common

class TaskStatus {
	volatile String message      // 当前状态
	volatile String error        // 错误信息
	volatile int    totalSteps   // 总共步数
	volatile int    currentStep  // 当前步骤
	volatile double stepProgress // 当前步骤的完成百分比
	
	void setMessage(String message) {
		this.message = message
		// println message
	}
}

class DelayException extends RuntimeException {
	Long milliseconds = null
	
	DelayException() {
	}
	
	DelayException(long milliseconds) {
		this.milliseconds = milliseconds
	}
}

class WorkingThread extends Thread {
	/** 线程的结束标志 */
	volatile boolean stopFlag = false
	volatile boolean interruptable = false

	/** 发送停止信号，但不保证立即停止 */
	void sendStopSignal() {
		stopFlag = true
		if (interruptable) {
			interrupt()
		}
	}

	/** 延时time毫秒 */
	void delay(long time) {
		if (time == 0) {
			if (stopFlag) throw new InterruptedException()
		} else {
			interruptable = true
			Thread.sleep(time)
			interruptable = false
		}
	}
	
	/** 运行Worker */
	void runWorker() {
	}
	
	/** 出错 */
	void onException(Exception e) {
		throw e
	}
	
	/** 线程主方法 */
	void run() {
		try {
			runWorker()
		} catch(InterruptedException e) {
		} catch(Exception e) {
			onException(e)
		}
	}
}
