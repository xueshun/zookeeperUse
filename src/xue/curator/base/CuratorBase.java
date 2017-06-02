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
	/** zookeeper地址**/
	static final String CONNECT_ADDR = "192.168.1.191:2181";
	/** session超时时间**/
	static final int SESSION_OUTTIME =5000;
	
	public static void main(String[] args) throws Exception {
		//1  重试策略： 初试时间为1s 重试10次
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2 通过工厂创建连接 
		CuratorFramework cf = CuratorFrameworkFactory.builder()
				.connectString(CONNECT_ADDR)
				.connectionTimeoutMs(SESSION_OUTTIME)
				.retryPolicy(retryPolicy)
				//.namespace("super")
				.build();
		//3 开启连接
		cf.start();
		
		//System.out.println(States.CONNECTED);
		//System.out.println(cf.getState());
		
		//4. 建立节点    指定节点类型（不加withMode默认为持久类型节点）、路径、数据内容
		// create().creatingParentsIfNeeded()创建子节点
//		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
//				"/super/c1","c1 content".getBytes());
//		
//		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(
//				"/super/c2","c2 content".getBytes());
		
		
		//5 删除节点
		//deletingChildrenIfNeeded() 递归删除 不加上删除会抛异常
		//cf.delete().guaranteed().deletingChildrenIfNeeded().forPath("/super");
		
		
		
		//6 读取节点
//		String ret1 = new String(cf.getData().forPath("/super/c1"));
//		System.out.println(ret1);
		
		//7 修改节点
	/*	cf.setData().forPath("/super/c2","update node c2".getBytes());
		System.out.println(new String(cf.getData().forPath("/super/c2")));*/
		 
		//8 绑定回调函数
		//为什么使用线程池 ： 减少回调函数线程对cpu资源的消耗
		//code 为0 说明创建成功
		//type create read...
		ExecutorService pool = Executors.newCachedThreadPool();
		cf.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
		.inBackground(new BackgroundCallback() {
			@Override
			public void processResult(CuratorFramework cf, CuratorEvent ce) throws Exception {
				System.out.println("code:" + ce.getResultCode());
				System.out.println("type:" + ce.getType());
				System.out.println("线程为：" + Thread.currentThread().getName());
			}
		}).forPath("/super/c3","c3 content".getBytes());
		
		
		//读取子节点getChildren方法 和 判断节点是否存在checkExists方法
		List<String> list = cf.getChildren().forPath("/super");
		for (String p : list) {
			System.out.println(p);
		}
		//当stat 为null的时候 说明该节点不存在
		Stat stat = cf.checkExists().forPath("/super/c3");
		System.out.println(stat);
		cf.close();
	}
	
}
