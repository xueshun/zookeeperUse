package xue.curator.lock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

/**
 * synchronized 与 ReentantLock() 重入锁的区别
 * @author Administrator
 *
 */
public class Lock01 {
	
	static ReentrantLock reentrantLock = new ReentrantLock();
	static int count = 10;
	public static void genarNo(){
		try {
			reentrantLock.lock();
			count --;
		} finally {
			reentrantLock.unlock();
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		final CountDownLatch countdown = new CountDownLatch(1);
		
		for (int i = 0; i < 10; i++) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						countdown.await();
						genarNo();
						SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss|SSS");
						System.out.println(sdf.format(new Date()));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
			},"t"+i).start();;
		}
		Thread.sleep(1000);
		countdown.countDown();
	}
}
