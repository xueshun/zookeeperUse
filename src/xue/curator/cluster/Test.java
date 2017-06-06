package xue.curator.cluster;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class Test {
	
	static final String CONNECT_ADDR = "192.168.1.191:2181,"
			+ "192.168.1.220:2181,192.168.1.221:2181";
	
	static final int SESSION_OUTTIME = 5000;//ms
	
	public static void main(String[] args) throws Exception {
		//1.重试策略： 初试时间为1s，重试10次
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2.通过工厂创建连接
		CuratorFramework cf = CuratorFrameworkFactory.builder()
				.connectString(CONNECT_ADDR)
				.sessionTimeoutMs(SESSION_OUTTIME)
				.retryPolicy(retryPolicy)
				.build();
		//3.开启连接
		cf.start();
		
		Thread.sleep(3000);
		/*System.out.println(cf.getChildren().forPath("/super").get(0));*/
		
		//4.创建节点
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
			.forPath("/super/c1","c1 content".getBytes());
		Thread.sleep(1000);
		
		cf.close();
	}
}
