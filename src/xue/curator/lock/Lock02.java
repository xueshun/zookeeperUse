package xue.curator.lock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * ������(��������) �� �ֲ�ʽ��������
 * @author Administrator
 *
 */
public class Lock02 {

	/** zookeeper��ַ */
	static final String CONNECT_ADDR = "192.168.1.191:2181,192.168.1.220:2181,192.168.1.221:2181";
	/** session��ʱʱ�� */
	static final int SESSION_OUTTIME = 5000;//ms 
	
	static int count = 10;
	public static void genarNo(){
		try {
			count--;
			System.out.println(count);
		} finally {
		
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		//1 ���Բ��ԣ�����ʱ��Ϊ1s ����10��
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2 ͨ��������������
		CuratorFramework cf = CuratorFrameworkFactory.builder()
					.connectString(CONNECT_ADDR)
					.sessionTimeoutMs(SESSION_OUTTIME)
					.retryPolicy(retryPolicy)
//					.namespace("super")
					.build();
		//3 ��������
		cf.start();
		
		//4 �ֲ�ʽ��
		//final InterProcessMutex lock = new InterProcessMutex(cf, "/super");
		
		//������
		final ReentrantLock reentrantLock = new ReentrantLock();
		final CountDownLatch countdown = new CountDownLatch(1);
		
		for(int i = 0; i < 10; i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						countdown.await();
						//����
						//lock.acquire();
						reentrantLock.lock();
						//-------------ҵ����ʼ
						genarNo();
						SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss|SSS");
						System.out.println(sdf.format(new Date()));
						//System.out.println(System.currentTimeMillis());
						//-------------ҵ�������
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							//�ͷ�
							//lock.release();
							reentrantLock.unlock();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			},"t" + i).start();
		}
		Thread.sleep(100);
		countdown.countDown();		 
	}
}
