package xue.curator.watcher;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class CuratorWatcher2 {
	
	static final String CONNECT_ADDR = "192.168.1.191:2181";
	
	static final int SESSION_OUTTIME = 5000;//ms
	
	public static void main(String[] args) throws Exception {
		
		//1.重试策略: 初试时间为1s，重试10次
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		
		//2.通过工厂创建连接
		CuratorFramework cf = CuratorFrameworkFactory.builder()
				.connectString(CONNECT_ADDR)
				.sessionTimeoutMs(SESSION_OUTTIME)
				.retryPolicy(retryPolicy)
				.build();
		//3.建立连接
		cf.start();
		
		//4.建立一个PathChildrenCache缓存
		/**
		 * (CuratorFramework client,本次连接
		 *  String path, 节点路径
		 *   boolean cacheData 是否接受节点数据内容，如果为false，则是不接受)
		 */
		PathChildrenCache cache = new PathChildrenCache(cf, "/super", true);
		//5.在初始化的时候进行缓存监听
		cache.start(StartMode.POST_INITIALIZED_EVENT);
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			
			/**
			 * 监视子节点变更
			 *  新建、修改、删除
			 * 
			 */
			@Override
			public void childEvent(CuratorFramework cf, PathChildrenCacheEvent event) throws Exception {
				switch (event.getType()) {
				case CHILD_ADDED:
					System.out.println("CHILD_ADDED : " + event.getData().getPath());
					System.out.println("CHILD_ADDED : " + new String(event.getData().getData(),"utf-8"));
					break;
				case CHILD_UPDATED:
					System.out.println("CHILD_UPDATED : " + event.getData().getPath());
					System.out.println("CHILD_UPDATED : " + new String(event.getData().getData(),"utf-8"));
					break;
				case CHILD_REMOVED:
					System.out.println("CHILD_REMOVED : " + event.getData().getPath());
					System.out.println("CHILD_REMOVED : " + new String(event.getData().getData(),"utf-8"));
					break;
				default:
					break;
				}
			}
		});
		
		//创建本身节点不发生变化
		cf.create().forPath("/super","init".getBytes());
		
		//添加子节点
		Thread.sleep(1000);
		cf.create().forPath("/super/c1","c1 content".getBytes());
		Thread.sleep(1000);
		cf.create().forPath("/super/c2","c2 content".getBytes());
		
		//修改节点
		Thread.sleep(1000);
		cf.setData().forPath("/super/c1","c1 update".getBytes());
		
		//删除节点
		Thread.sleep(1000);
		cf.delete().forPath("/super/c2");
		
		//删除本身节点
		Thread.sleep(1000);
		cf.delete().deletingChildrenIfNeeded().forPath("/super");
		
		cf.close();
	}
}
