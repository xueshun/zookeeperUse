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
 * ʹ��NodeCache�ķ�ʽȥ�ͻ���ʵ����ע��һ���������棬Ȼ��ʵ�ֶ�Ӧ�ļ����������ɣ�
 *  ��Ҫ�����ּ�����ʽ NodeCacheListener:�����ڵ���������޸Ĳ���
 *  		   PathChildCacheListener:�����ӽڵ���������޸ġ�ɾ������
 */
public class CuratorWatcher1 {
	
	static final String CONNECT_ADDR = "192.168.1.191:2181";
	
	static final int SESSION_OUTTIME = 5000;	
	
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
		
		//4.����һ��cache����
		/**
		 * CuratorFramework client, cf
		 * String path,  ·��
		 * boolean dataIsCompressed �����Ƿ�ѹ��
		 */
		final NodeCache cache = new NodeCache(cf, "/super", false);
		cache.start(true);
		cache.getListenable().addListener(new NodeCacheListener() {
			@Override
			public void nodeChanged() throws Exception {
				System.out.println("·��Ϊ��" + cache.getCurrentData().getPath());
				System.out.println("����Ϊ��" + new String(cache.getCurrentData().getData()));
				System.out.println("״̬Ϊ��" + cache.getCurrentData().getStat());
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
