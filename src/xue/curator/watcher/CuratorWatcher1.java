package xue.curator.watcher;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * 
 * @author Administrator
 * 使用NodeCache的方式去客户端实例中注册一个监听缓存，然后实现对应的监听方法即可，
 *  主要有两种监听方式 NodeCacheListener:监听节点的新增、修改操作
 *  		   PathChildCacheListener:监听子节点的新增、修改、删除操作
 */
public class CuratorWatcher1 {
	
	static final String CONNECT_ADDR = "192.168.1.191:2181";
	
	static final int SESSION_OUTTIME = 5000;	
	
	public static void main(String[] args) throws Exception {
		//1.重试策略： 初试时间为1s，重试10次
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2.通过工厂创建连接
		CuratorFramework cf = CuratorFrameworkFactory.builder()
				.connectString(CONNECT_ADDR)
				.sessionTimeoutMs(SESSION_OUTTIME)
				.retryPolicy(retryPolicy)
				.build();
		//3.建立连接
		cf.start();
		
		//4.建立一个cache缓存
		/**
		 * CuratorFramework client, cf
		 * String path,  路径
		 * boolean dataIsCompressed 数据是否压缩
		 */
		final NodeCache cache = new NodeCache(cf, "/super", false);
		cache.start(true);
		cache.getListenable().addListener(new NodeCacheListener() {
			@Override
			public void nodeChanged() throws Exception {
				System.out.println("路径为：" + cache.getCurrentData().getPath());
				System.out.println("数据为：" + new String(cache.getCurrentData().getData()));
				System.out.println("状态为：" + cache.getCurrentData().getStat());
				System.out.println("-----------------------------");
			}
		});
		
		Thread.sleep(1000);
		cf.create().forPath("/super","123".getBytes());
		
		Thread.sleep(1000);
		cf.setData().forPath("/super","456".getBytes());
		
		Thread.sleep(1000);
		cf.delete().forPath("/super");
		
		Thread.sleep(Integer.MAX_VALUE);
		cf.close();
		
	}
}
