package xue.curator.cluster;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

public class CuratorWatcher {
	
	/** ���ڵ�path**/
	static final String PARENT_PATH = "/super" ;
	/** zookeeper���ӵ�ַ**/
	public static final String 	CONNECT_ADDR = "192.168.1.191:2181,192.168.1.220:2181,192.168.1.221:2181";
	/** sessionʧЧʱ��**/
	public static final int SESSION_OUTTIME = 5000;//ms
	
	public CuratorWatcher() throws Exception{
		//1.���Բ��ԣ�����ʱ��Ϊ1s������10��
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2.ͨ��������������
		CuratorFramework cf = CuratorFrameworkFactory.builder()
				.connectString(CONNECT_ADDR)
				.sessionTimeoutMs(SESSION_OUTTIME)
				.retryPolicy(retryPolicy)
				.build();
		//3.��������
		cf.start();
		
		//4.�������ڵ�
		if(cf.checkExists().forPath(PARENT_PATH) == null){
			cf.create().withMode(CreateMode.PERSISTENT).forPath(PARENT_PATH,"super init ".getBytes());
		}
		
		//5.����һ��PathChildrenCache���棬����������Ϊ�Ƿ���ܽڵ��������ݣ����Ϊfalse�򲻽���
		PathChildrenCache cache = new PathChildrenCache(cf, PARENT_PATH, true);
		
		//6.�ڳ�ʼ����ʱ��ͽ��м���
		cache.start(StartMode.POST_INITIALIZED_EVENT);
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			
			@Override
			public void childEvent(CuratorFramework cf, PathChildrenCacheEvent event) throws Exception {
				switch (event.getType()) {
				case CHILD_ADDED:
					System.out.println("CHILD_ADDED :" + event.getData().getPath());
					System.out.println("CHILD_ADDED :" + new String(event.getData().getData()));
					break;
				case CHILD_UPDATED:
					System.out.println("CHILD_UPDATED :" + event.getData().getPath());
					System.out.println("CHILD_UPDATED :" + new String(event.getData().getData()));
					break;
				case CHILD_REMOVED:
					System.out.println("CHILD_REMOVED :" + event.getData().getPath());
					System.out.println("CHILD_REMOVED :" + new String(event.getData().getData()));
					break;
				default:
					break;
				}
			}
		});
		
	
	}
}
