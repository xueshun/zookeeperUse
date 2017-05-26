package xue.zookeeper.base;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

/**
 * zookeeper helloworld
 * @author Administrator
 *
 */
public class ZookeeperBase {
	
	/*zookeeper��ַ*/
	static final String CONNECT_ADDR ="192.168.1.191:2181,192.168.1.220:2181,192.168.1.221:2181";
	/**session��ʱʱ�� **/
	static final int SESSION_OUTTIME = 2000;//ms
	/**�ź�������������ִ�У����ڵȴ�zookeeper���ӳɹ������ͳɹ��ź� connectedSemaphore.await();
	 * ��Ϊzookeeper�ͻ������ӷ�������첽���ӵ�
	 * **/
	static final CountDownLatch connectedSemaphore = new CountDownLatch(1);
	
	public static void main(String[] args) throws Exception {
		ZooKeeper zk =new ZooKeeper(CONNECT_ADDR, SESSION_OUTTIME, new Watcher(){
			@Override
			public void process(WatchedEvent event) {
				/*��ȡ�¼���״̬*/
				KeeperState keeperState = event.getState();
				//��ȡ�¼�������
				EventType eventType = event.getType();
				//�����������
				if(KeeperState.SyncConnected == keeperState){
					if(EventType.None ==eventType){
						//����������ӳɹ��������ź������ú���������������ִ��
						connectedSemaphore.countDown();
						System.out.println("zk ��������");
					}
				}
			}
		});
		
		//��������
		connectedSemaphore.await();
		System.out.println("..");
		
		//�������ڵ�
		/**
		 * String path : �ڵ�·�����Ǹ�Ŀ¼�´����ڵ� ������ݹ鴴���ڵ㣬Ҳ����˵���ڵ㲻���ڵ�����£����������ӽڵ�
		 * byte[] data �� �ڵ����ݣ�Ҫ���������ֽ�����
		 * List<ACL> acl �� �ڵ�Ȩ��
		 * CreateMode createMode �� �ڵ�Ĵ�����ʽ
		 * 		PERSISTENT �� �־ýڵ�
		 * 		PERSISTENT_SEQUENTIAL : �־�˳��ڵ�
		 * 		EPHEMERAL �� ��ʱ�ڵ�  ������Ч   
		 * 		EPHEMERAL_SEQUENTIAL : ��ʱ˳��ڵ�
		 * 
		 * �ڵ�����ֲ������ظ�
		 */
	/*	String create = zk.create("/testRoot", "testRoot".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		System.out.println(create);*/
		
		String create = zk.create("/testRoot/children", "children data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		System.out.println(create);
		
		zk.close();
	}
}
