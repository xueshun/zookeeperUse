package xue.curator.barrier;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * DistributedDoubleBarrier ��    DistributedBarrier ����
 * 		DistributedDoubleBarrier �м����ͻ������ü���barrier
 * 		DistributedBarrier ��N�ͻ�������N+1��barrier ����ִ�е�ʱ�� ��barrier.removeBarrier()֮��
 * @author Administrator
 *
 */
public class CuratorBarrier02 {

	static final String CONNECT_ADDR = "192.168.1.191:2181,192.168.1.220:2181,192.168.1.221:2181";

	static final int SESSION_OUTTIME = 5000;//ms

	static DistributedBarrier barrier = null;

	public static void main(String[] args) throws Exception {



		for(int i = 0; i < 5; i++){
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
						CuratorFramework cf = CuratorFrameworkFactory.builder()
								.connectString(CONNECT_ADDR)
								.sessionTimeoutMs(SESSION_OUTTIME)
								.retryPolicy(retryPolicy)
								.build();
						cf.start();
						barrier = new DistributedBarrier(cf, "/super");
						System.out.println(Thread.currentThread().getName() + "����barrier!");
						barrier.setBarrier();	//����
						barrier.waitOnBarrier();	//�ȴ�
						System.out.println("---------��ʼִ�г���----------");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			},"t" + i).start();
		}

		Thread.sleep(5000);
		barrier.removeBarrier();	//�ͷ�
	}
}
