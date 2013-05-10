package cn.edu.zucc.common

class TaskStatus {
	volatile String message      // ��ǰ״̬
	volatile String error        // ������Ϣ
	volatile int    totalSteps   // �ܹ�����
	volatile int    currentStep  // ��ǰ����
	volatile double stepProgress // ��ǰ�������ɰٷֱ�
	
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
	/** �̵߳Ľ�����־ */
	volatile boolean stopFlag = false
	volatile boolean interruptable = false

	/** ����ֹͣ�źţ�������֤����ֹͣ */
	void sendStopSignal() {
		stopFlag = true
		if (interruptable) {
			interrupt()
		}
	}

	/** ��ʱtime���� */
	void delay(long time) {
		if (time == 0) {
			if (stopFlag) throw new InterruptedException()
		} else {
			interruptable = true
			Thread.sleep(time)
			interruptable = false
		}
	}
	
	/** ����Worker */
	void runWorker() {
	}
	
	/** ���� */
	void onException(Exception e) {
		throw e
	}
	
	/** �߳������� */
	void run() {
		try {
			runWorker()
		} catch(InterruptedException e) {
		} catch(Exception e) {
			onException(e)
		}
	}
}
