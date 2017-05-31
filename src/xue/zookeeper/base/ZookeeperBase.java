package xue.zookeeper.base;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.AsyncCallback.VoidCallback;
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
		 * byte[] data �� �ڵ����ݣ�Ҫ���������ֽ����� ��֧��java���л�����
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
		
		/*String create = zk.create("/testRoot/children", "children data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		System.out.println(create);*/
		
		
//		zk.delete("/testRoot", -1, new VoidCallback() {
//			/**
//			 * rc Ϊ�������Ӧ�룬0 ��ʾ���óɹ���-4��ʾ�˿����ӣ�-110��ʾָ���ڵ���ڣ�-112��ʾ�Ự�Ѿ�����
//			 * path:�ӿڵ���ʱ����spi�����ݽڵ��·������
//			 * ctx��Ϊ���ýӿڴ���api��ctxֵ
//			 * name��ʵ���ڷ������˴����ڵ������
//			 */
//			@Override
//			public void processResult(int rc, String path, Object ctx) {
//				try {
//					Thread.sleep(1000);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				System.out.println(rc);
//				System.out.println(path);
//				System.out.println(ctx);
//			}
//		}, "a");
		
		//��ȡ�ڵ��е�����
		/*byte[] data = zk.getData("/testRoot", false, null);
		System.out.println(new String(data));*/
		
		//��ȡ�ӽڵ�
		/*List<String> children = zk.getChildren("/testRoot", false);
		for (String path : children) {
			System.out.println(path);
			String realPath = "/testRoot" + path;
			System.out.println(new String(zk.getData(realPath, false, null)));
		}*/
		
		//�޸Ľڵ�
		/*zk.setData("/testRoot", "modify data root".getBytes(), -1);
		byte[] data = zk.getData("/testRoot", false, null);
		System.out.println(new String(data));
		*/
		
		//�ж�һ���ڵ��Ƿ����
		System.out.println(zk.exists("/testRoot", false));
		
		zk.close();
	}
}
