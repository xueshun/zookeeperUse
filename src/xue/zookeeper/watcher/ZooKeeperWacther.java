package xue.zookeeper.watcher;


import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

/**
 * zk��watcherֻ�ܶԸ��ڵ�ı仯���м�����
 *  �����ӽڵ�ֻ��һ��״̬�� childrennodechanged �����ӽڵ����ɾ��û�������� 
 *  �پ���watcher ������һ���ԣ�ÿ�ζ�����Ҫ����new����
 * @author Administrator
 *
 */
public class ZooKeeperWacther implements Watcher{
	
	/** ����ԭ�ӱ���**/
	AtomicInteger seq = new AtomicInteger();
	/** ����sessionʧЧʱ��**/
	private static final int SESSION_TIMEOUT=10000;
	/**zookeeper��������ַ**/
	private static final String CONNECTION_ADDR = "192.168.1.191:2181";
	/** zk��·������**/
	private static final String PARENT_PATH = "/testRoot";
	/** zk����·��**/
	private static final String CHILDREN_PATH = "/testRoot/children";
	/** �����ʶ**/
	private static final String LOG_PREFIX_OF_MAIN = "��Main��";
	/** zk����**/
	private ZooKeeper zk =null;
	/** �ź������ã����ڵȴ�zookeeper���ӽ���֮�� ֪ͨ���������������ִ��**/
	private CountDownLatch connectedSemaphore = new CountDownLatch(1);
	
	/**
	 * ����ZK����
	 * @param connectAddr  ZK��������ַ�б�
	 * @param sessionTimeout Session��ʱʱ��
	 */
	public void createConnection(String connectAddr, int sessionTimeout){
		this.releaseConnection();
		try {
			zk = new ZooKeeper(CONNECTION_ADDR, SESSION_TIMEOUT, this);
			System.out.println(LOG_PREFIX_OF_MAIN + "��ʼ����ZK������");
			connectedSemaphore.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �ر�����
	 */
	public void releaseConnection(){
		if(this.zk !=null){
			try {
				this.zk.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * �����ڵ� 
	 * @param path �ڵ�·��
	 * @param data ��������
	 * @return
	 */
	public boolean createPath(String path, String data){
		try {
			//���ü��(����zookeeper�ļ�ض���һ���Ե����� ÿ�α������ü��)
			this.zk.exists(path, true);
			System.out.println(LOG_PREFIX_OF_MAIN + "�ڵ㴴���ɹ�, Path: " + 
							   this.zk.create(	path, /**·��*/ 
									   			data.getBytes(),/**����*/
								   				Ids.OPEN_ACL_UNSAFE, /**���пɼ�*/
								   				CreateMode.PERSISTENT ) + 	/**���ô洢*/
							   ", content: " + data);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * ��ȡָ���ڵ���������
	 * @param path �ڵ�·��
	 * @param needWatch �Ƿ���Ҫwatch
	 * @return
	 */
	public String readData(String path,boolean needWatch){
		try {
			return new String(this.zk.getData(path, needWatch, null));
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * ����ָ���ڵ���������
	 * @param path �ڵ�·��
	 * @param data ��������
	 * @return
	 */
	public boolean updateData(String path,String data){ 
		try {
			System.out.println(LOG_PREFIX_OF_MAIN +
					"�������ݳɹ���path:" + path
					+",stat:" + this.zk.setData(path, data.getBytes(), -1));
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return false;	
	}
	
	/**
	 * ɾ��ָ���ڵ�
	 * @param path �ڵ�·��
	 */
	public void deleteNode(String path){
		try {
			this.zk.delete(path, -1);
			System.out.println(LOG_PREFIX_OF_MAIN
					+"ɾ���ڵ�ɹ���path: " + path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �жϽڵ��Ƿ����
	 * @param path  �ڵ�·��
	 * @param needWatch  �Ƿ���Ҫwatch
	 * @return
	 */
	public Stat exists(String path, boolean needWatch){
		try {
			return this.zk.exists(path, needWatch);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	/**
	 * ��ȡ�ӽڵ�
	 * @param path �ڵ�·��
	 * @param needWatch
	 * @return
	 */
	private List<String> GetChildren(String path, boolean needWatch){
		try {
			return this.zk.getChildren(path, needWatch);
		}  catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * ɾ�����нڵ�
	 */
	public void deleteAllTestPath() {
		if(this.exists(CHILDREN_PATH, false) != null){
			this.deleteNode(CHILDREN_PATH);
		}
		if(this.exists(PARENT_PATH, false) != null){
			this.deleteNode(PARENT_PATH);
		}		
	}
	
	/**
	 * �յ�����Server��Watcher֪ͨ��Ĵ���
	 */
	@Override
	public void process(WatchedEvent event) {
		System.out.println("���� process ...... event = " + event);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(event == null){
			return ;
		}
		
		//����״̬
		KeeperState keeperState = event.getState();
		//�¼�����
		EventType eventType = event.getType();
		//��Ӱ���path
		String path = event.getPath();
		
		String logPrefix = "��Watcher-" + this.seq.incrementAndGet() + "��";
		
		System.out.println(logPrefix  + "�յ�Watcher֪ͨ");
		System.out.println(logPrefix + "����״̬��\t" + keeperState);
		System.out.println(logPrefix + "�¼����ͣ�\t" + eventType.toString());
		
		if(KeeperState.SyncConnected == keeperState){
			//�ɹ�������ZK������
			if(EventType.None == eventType){
				System.out.println(logPrefix + "�ɹ�������ZK������");
				connectedSemaphore.countDown();
			}else if(EventType.NodeCreated == eventType){ //�����ڵ�
				System.out.println(logPrefix + "�ڵ㴴��");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.exists(path, true);
			}else if(EventType.NodeDataChanged == eventType){ //���½ڵ�
				System.out.println(logPrefix + "�ڵ����ݸ���");
				System.out.println();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(logPrefix + "�������ݣ�" + this.readData(PARENT_PATH, true));
			}else if(EventType.NodeChildrenChanged == eventType){ //�����ӽڵ�
				System.out.println(logPrefix + "�ӽڵ���");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(logPrefix + "�ӽڵ��б�" + this.GetChildren(PARENT_PATH, true));
			}else if(EventType.NodeDeleted == eventType){ //ɾ���ڵ�
				System.out.println(logPrefix + "�ڵ�" + path + "��ɾ��"); 
			}else ;
		}else if(KeeperState.Disconnected == keeperState){
			System.out.println(logPrefix + "��ZK�������Ͽ�����");
		}else if(KeeperState.AuthFailed == keeperState){
			System.out.println(logPrefix + "Ȩ�޼��ʧ��");
		}else if(KeeperState.Expired == keeperState){
			System.out.println(logPrefix + "�ỰʧЧ");
		}else ;
		System.out.println("---------------------------------------");
	}
	
	public static void main(String[] args) throws Exception {
		//����Watcher
		ZooKeeperWacther zkWacth = new ZooKeeperWacther();
		//��������
		zkWacth.createConnection(CONNECTION_ADDR, SESSION_TIMEOUT);
		
		Thread.sleep(1000);
		
		zkWacth.deleteAllTestPath();
		//�����ڵ�
		if(zkWacth.createPath(PARENT_PATH, System.currentTimeMillis() + "")){
			Thread.sleep(1000);
			
			//��ȡ����
			System.out.println("--------------read parent----------");
			zkWacth.readData(PARENT_PATH, true);
			
			//�����ӽڵ�
			zkWacth.createPath(CHILDREN_PATH, System.currentTimeMillis() + "");
		}
		
		Thread.sleep(5000);
		//����ڵ�
		/*zkWacth.deleteAllTestPath();*/
		Thread.sleep(1000);
		zkWacth.releaseConnection();
	}

}
