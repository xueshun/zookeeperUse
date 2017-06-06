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
		//1.���Բ��ԣ� ����ʱ��Ϊ1s������10��
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2.ͨ��������������
		CuratorFramework cf = CuratorFrameworkFactory.builder()
				.connectString(CONNECT_ADDR)
				.sessionTimeoutMs(SESSION_OUTTIME)
				.retryPolicy(retryPolicy)
				.build();
		//3.��������
		cf.start();
		
		Thread.sleep(3000);
		/*System.out.println(cf.getChildren().forPath("/super").get(0));*/
		
		//4.�����ڵ�
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
			.forPath("/super/c1","c1 content".getBytes());
		Thread.sleep(1000);
		
		cf.close();
	}
}
