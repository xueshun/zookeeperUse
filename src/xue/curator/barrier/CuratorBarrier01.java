package xue.curator.barrier;

import java.util.Random;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * DistributedDoubleBarrier 
 * 	 添加barrier的作用就是让程序在barrier移除的时刻同时运行
 * @author Administrator
 *
 */
public class CuratorBarrier01 {

	static final String CONNECT_ADDR = "192.168.1.191:2181,192.168.1.220:2181,192.168.1.221:2181";

	static final int SESSION_OUTTIME = 5000;//ms

	public static void main(String[] args) {

		for (int i = 0; i < 5; i++) {

			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
						CuratorFramework cf = CuratorFrameworkFactory.builder()
								.connectString(CONNECT_ADDR)
								.sessionTimeoutMs(SESSION_OUTTIME)
								.retryPolicy(retryPolicy)
								.build();
						cf.start();
							
						/**
						 * 参数一 ： 连接工程
						 * 参数二 ： 连接节点
						 * 参数三 ： 总共几个客户端
						 */
						DistributedDoubleBarrier barrier = new DistributedDoubleBarrier(cf, "/super", 5);
						Thread.sleep(1000 * (new Random()).nextInt(5));
						System.out.println(Thread.currentThread().getName() + "已经准备");
						barrier.enter();
						System.out.println("同时开始运行.....");
						Thread.sleep(1000 *(new Random().nextInt(3)));
						System.out.println(Thread.currentThread().getName() + "运行完毕");
						barrier.leave();
						System.out.println("同时推出运行.....");

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			},"t" + i ).start();;
		}
	}
}
