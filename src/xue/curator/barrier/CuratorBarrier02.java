package xue.curator.barrier;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * DistributedDoubleBarrier 与    DistributedBarrier 区别
 * 		DistributedDoubleBarrier 有几个客户端设置几个barrier
 * 		DistributedBarrier 有N客户端设置N+1个barrier 程序执行的时刻 是barrier.removeBarrier()之后
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
						System.out.println(Thread.currentThread().getName() + "设置barrier!");
						barrier.setBarrier();	//设置
						barrier.waitOnBarrier();	//等待
						System.out.println("---------开始执行程序----------");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			},"t" + i).start();
		}

		Thread.sleep(5000);
		barrier.removeBarrier();	//释放
	}
}
