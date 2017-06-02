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
		
		//1.���Բ���: ����ʱ��Ϊ1s������10��
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		
		//2.ͨ��������������
		CuratorFramework cf = CuratorFrameworkFactory.builder()
				.connectString(CONNECT_ADDR)
				.sessionTimeoutMs(SESSION_OUTTIME)
				.retryPolicy(retryPolicy)
				.build();
		//3.��������
		cf.start();
		
		//4.����һ��PathChildrenCache����
		/**
		 * (CuratorFramework client,��������
		 *  String path, �ڵ�·��
		 *   boolean cacheData �Ƿ���ܽڵ��������ݣ����Ϊfalse�����ǲ�����)
		 */
		PathChildrenCache cache = new PathChildrenCache(cf, "/super", true);
		//5.�ڳ�ʼ����ʱ����л������
		cache.start(StartMode.POST_INITIALIZED_EVENT);
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			
			/**
			 * �����ӽڵ���
			 *  �½����޸ġ�ɾ��
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
		
		//��������ڵ㲻�����仯
		cf.create().forPath("/super","init".getBytes());
		
		//����ӽڵ�
		Thread.sleep(1000);
		cf.create().forPath("/super/c1","c1 content".getBytes());
		Thread.sleep(1000);
		cf.create().forPath("/super/c2","c2 content".getBytes());
		
		//�޸Ľڵ�
		Thread.sleep(1000);
		cf.setData().forPath("/super/c1","c1 update".getBytes());
		
		//ɾ���ڵ�
		Thread.sleep(1000);
		cf.delete().forPath("/super/c2");
		
		//ɾ������ڵ�
		Thread.sleep(1000);
		cf.delete().deletingChildrenIfNeeded().forPath("/super");
		
		cf.close();
	}
}
