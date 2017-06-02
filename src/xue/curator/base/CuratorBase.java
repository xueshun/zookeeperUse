package xue.curator.base;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;

public class CuratorBase {
	/** zookeeper��ַ**/
	static final String CONNECT_ADDR = "192.168.1.191:2181";
	/** session��ʱʱ��**/
	static final int SESSION_OUTTIME =5000;
	
	public static void main(String[] args) throws Exception {
		//1  ���Բ��ԣ� ����ʱ��Ϊ1s ����10��
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2 ͨ�������������� 
		CuratorFramework cf = CuratorFrameworkFactory.builder()
				.connectString(CONNECT_ADDR)
				.connectionTimeoutMs(SESSION_OUTTIME)
				.retryPolicy(retryPolicy)
				//.namespace("super")
				.build();
		//3 ��������
		cf.start();
		
		//System.out.println(States.CONNECTED);
		//System.out.println(cf.getState());
		
		//4. �����ڵ�    ָ���ڵ����ͣ�����withModeĬ��Ϊ�־����ͽڵ㣩��·������������
		// create().creatingParentsIfNeeded()�����ӽڵ�
//		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
//				"/super/c1","c1 content".getBytes());
//		
//		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
//				"/super/c2","c2 content".getBytes());
		
		
		//5 ɾ���ڵ�
		//deletingChildrenIfNeeded() �ݹ�ɾ�� ������ɾ�������쳣
		//cf.delete().guaranteed().deletingChildrenIfNeeded().forPath("/super");
		
		
		
		//6 ��ȡ�ڵ�
//		String ret1 = new String(cf.getData().forPath("/super/c1"));
//		System.out.println(ret1);
		
		//7 �޸Ľڵ�
	/*	cf.setData().forPath("/super/c2","update node c2".getBytes());
		System.out.println(new String(cf.getData().forPath("/super/c2")));*/
		 
		//8 �󶨻ص�����
		//Ϊʲôʹ���̳߳� �� ���ٻص������̶߳�cpu��Դ������
		//code Ϊ0 ˵�������ɹ�
		//type create read...
		ExecutorService pool = Executors.newCachedThreadPool();
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
		.inBackground(new BackgroundCallback() {
			@Override
			public void processResult(CuratorFramework cf, CuratorEvent ce) throws Exception {
				System.out.println("code:" + ce.getResultCode());
				System.out.println("type:" + ce.getType());
				System.out.println("�߳�Ϊ��" + Thread.currentThread().getName());
			}
		}).forPath("/super/c3","c3 content".getBytes());
		
		
		//��ȡ�ӽڵ�getChildren���� �� �жϽڵ��Ƿ����checkExists����
		List<String> list = cf.getChildren().forPath("/super");
		for (String p : list) {
			System.out.println(p);
		}
		//��stat Ϊnull��ʱ�� ˵���ýڵ㲻����
		Stat stat = cf.checkExists().forPath("/super/c3");
		System.out.println(stat);
		cf.close();
	}
	
}
