package xue.zookeeper.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

/**
 * Zookeeper �ڵ���Ȩ
 * @author Administrator
 *	ACL(Access ControlList) Zookeeper��Ϊһ���ֲ�ʽЭ����ܣ����ڲ��洢�Ķ���һЩ���ڷֲ�ʽϵͳ
 *	����ʱ״̬��Ԫ���ݣ���������Ƶ�һ�·ֲ�ʽ����Masterѡ�ٺ�Э����Ӧ�ó�����������Ҫ��Ч�ı�֤Zookeeper�е����ݰ�ȫ
 * 
 * ZK�ṩ������ģʽ��Ȩ��ģʽ����Ȩ����Ȩ�ޡ�
 * 
 *  Ȩ��ģʽ��scheme���������ʹ�õ���������Ȩ��ģʽ
 *  
 *  IP��ipģʽͨ��ip��ַ���������п���Ȩ�ޣ����������ˣ�IP:192.168.1.191 ����ʾȨ�޿��ƶ���������ip��ַ��
 *  	ͬʱҲ֧�ְ����η��䣺���磺192.168.1.*
 *  Digest: digest����õ�Ȩ�޿���ģʽ��Ҳ���������Ƕ�Ȩ�޵���ʶ����������"username:password"��ʽ
 *  	��Ȩ�ޱ�ʶ����ȫ�����á�ZK����γɵ�Ȩ�ޱ�ʶ�Ⱥ�������α���������ֱ���SHA-1�����㷨��Base64����
 *  World:world��һֱ�����Ȩ�޿���ģʽ������ģʽ���Կ���Ϊ�����Digest,��������һ����ʶ���ѡ�
 *  Super �� �����û�ģʽ���ڳ����û�ģʽ�¿��Զ�ZK������в�����
 *  
 *  Ȩ�޶���ָ����Ȩ�޸�����û�����һ��ָ����ʵ�壬����ip��ַ������ȡ��ڲ�ͬģʽ�£���Ȩ�����ǲ�ͬ�ġ�����ģʽ��Ȩ�޶���һһ��Ӧ
 *  
 *  Ȩ�� : Ȩ�޾���ָ��Щͨ��Ȩ�޼���α�����ִ�еĲ�������zk�У������ݵĲ���Ȩ�޷�Ϊ��������ࣺ
 *  	Create delete read write admin
 *
 */
public class ZookeeperAuth implements Watcher{
	/** ���ӵ�ַ */
	final static String CONNECT_ADDR = "192.168.1.191:2181";
	/** ����·�� */
	final static String PATH = "/testAuth";
	final static String PATH_DEL = "/testAuth/delNode";
	/** ��֤���� */
	final static String authentication_type = "digest";
	/** ��֤��ȷ���� */
	final static String correctAuthentication = "123456";
	/** ��֤���󷽷� */
	final static String badAuthentication = "654321";

	static ZooKeeper zk = null;
	/** ��ʱ�� */
	AtomicInteger seq = new AtomicInteger();
	/** ��ʶ */
	private static final String LOG_PREFIX_OF_MAIN = "��Main��";

	private CountDownLatch connectedSemaphore = new CountDownLatch(1);

	/**
	 * ����ZK����
	 * @param connectionString ZK���������ӵ�ַ�б�
	 * @param sessionTimeout Session��ʱʱ��
	 */
	public void createConnection(String connectionString,int sessionTimeout){
		this.releaseConnection();
		try {
			zk = new ZooKeeper(connectionString,sessionTimeout,this);
			//��ӽڵ���Ȩ
			zk.addAuthInfo(authentication_type, correctAuthentication.getBytes());
			System.out.println(LOG_PREFIX_OF_MAIN+"��ʼ����ZK������");
			//�����ȴ�
			connectedSemaphore.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �ر�����
	 */
	public void releaseConnection(){
		if(this.zk!=null){
			try {
				this.zk.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void process(WatchedEvent event) {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if(event==null){
			return ;
		}
		// ����״̬
		KeeperState keeperState = event.getState();
		// �¼�����
		EventType eventType = event.getType();
		// ��Ӱ���path
		String path = event.getPath();

		String logPrefix = "��Watcher-" + this.seq.incrementAndGet() + "��";

		System.out.println(logPrefix + "�յ�Watcher֪ͨ");
		System.out.println(logPrefix + "����״̬:\t" + keeperState.toString());
		System.out.println(logPrefix + "�¼�����:\t" + eventType.toString());
		if (KeeperState.SyncConnected == keeperState) {
			// �ɹ�������ZK������
			if (EventType.None == eventType) {
				System.out.println(logPrefix + "�ɹ�������ZK������");
				connectedSemaphore.countDown();
			} 
		} else if (KeeperState.Disconnected == keeperState) {
			System.out.println(logPrefix + "��ZK�������Ͽ�����");
		} else if (KeeperState.AuthFailed == keeperState) {
			System.out.println(logPrefix + "Ȩ�޼��ʧ��");
		} else if (KeeperState.Expired == keeperState) {
			System.out.println(logPrefix + "�ỰʧЧ");
		}
		System.out.println("--------------------------------------------");
	}
	/** ��ȡ���ݣ����ô��������**/
	static void getDataByBadAuthentication(){
		String prefix = "[ʹ�ô������Ȩ��Ϣ]"; 
		try {
			ZooKeeper badzk = new ZooKeeper(CONNECT_ADDR,2000,null);
			//��Ȩ
			badzk.addAuthInfo(authentication_type, badAuthentication.getBytes());
			Thread.sleep(2000);
			System.out.println(prefix + "��ȡ���ݣ�" + PATH);
			System.out.println(prefix + "�ɹ���ȡ���ݣ�" + badzk.getData(PATH, false, null));
		} catch (Exception e) {
			System.err.println(prefix + "��ȡ����ʧ�ܣ�ԭ��" + e.getMessage());
		}
	}
	
	/** ��ȡ���ݣ�����������**/
	static void getDataByNoAuthentication(){
		String prefix = "[ʹ�ô������Ȩ��Ϣ]"; 
		try {
			ZooKeeper badzk = new ZooKeeper(CONNECT_ADDR,2000,null);
			Thread.sleep(2000);
			System.out.println(prefix + "��ȡ���ݣ�" + PATH);
			System.out.println(prefix + "�ɹ���ȡ���ݣ�" + badzk.getData(PATH, false, null));
		} catch (Exception e) {
			System.err.println(prefix + "��ȡ����ʧ�ܣ�ԭ��" + e.getMessage());
		}
	}
	
	/** ��ȡ���ݣ�ʹ����ȷ����**/
	static void getDataByCorrectAuthentication() {
		String prefix = "[ʹ����ȷ����Ȩ��Ϣ]";
		try {
			System.out.println(prefix + "��ȡ���ݣ�" + PATH);
			System.out.println(prefix + "�ɹ���ȡ���ݣ�" + zk.getData(PATH, false, null));
		} catch (Exception e) {
			System.out.println(prefix + "��ȡ����ʧ�ܣ�ԭ��" + e.getMessage());
		}
	}
	/**
	 * �������ݣ�����������
	 */
	static void updateDataByNoAuthentication() {

		String prefix = "[��ʹ���κ���Ȩ��Ϣ]";

		System.out.println(prefix + "�������ݣ� " + PATH);
		try {
			ZooKeeper nozk = new ZooKeeper(CONNECT_ADDR, 2000, null);
			Thread.sleep(2000);
			Stat stat = nozk.exists(PATH, false);
			if (stat!=null) {
				nozk.setData(PATH, prefix.getBytes(), -1);
				System.out.println(prefix + "���³ɹ�");
			}
		} catch (Exception e) {
			System.err.println(prefix + "����ʧ�ܣ�ԭ���ǣ�" + e.getMessage());
		}
	}

	/**
	 * �������ݣ����ô��������
	 */
	static void updateDataByBadAuthentication() {

		String prefix = "[ʹ�ô������Ȩ��Ϣ]";

		System.out.println(prefix + "�������ݣ�" + PATH);
		try {
			ZooKeeper badzk = new ZooKeeper(CONNECT_ADDR, 2000, null);
			//��Ȩ
			badzk.addAuthInfo(authentication_type,badAuthentication.getBytes());
			Thread.sleep(2000);
			Stat stat = badzk.exists(PATH, false);
			if (stat!=null) {
				badzk.setData(PATH, prefix.getBytes(), -1);
				System.out.println(prefix + "���³ɹ�");
			}
		} catch (Exception e) {
			System.err.println(prefix + "����ʧ�ܣ�ԭ���ǣ�" + e.getMessage());
		}
	}

	/**
	 * �������ݣ�������ȷ������
	 */
	static void updateDataByCorrectAuthentication() {

		String prefix = "[ʹ����ȷ����Ȩ��Ϣ]";

		System.out.println(prefix + "�������ݣ�" + PATH);
		try {
			Stat stat = zk.exists(PATH, false);
			if (stat!=null) {
				zk.setData(PATH, prefix.getBytes(), -1);
				System.out.println(prefix + "���³ɹ�");
			}
		} catch (Exception e) {
			System.err.println(prefix + "����ʧ�ܣ�ԭ���ǣ�" + e.getMessage());
		}
	}

	/**
	 * ��ʹ������ ɾ���ڵ�
	 */
	static void deleteNodeByNoAuthentication() throws Exception {

		String prefix = "[��ʹ���κ���Ȩ��Ϣ]";

		try {
			System.out.println(prefix + "ɾ���ڵ㣺" + PATH_DEL);
			ZooKeeper nozk = new ZooKeeper(CONNECT_ADDR, 2000, null);
			Thread.sleep(2000);
			Stat stat = nozk.exists(PATH_DEL, false);
			if (stat!=null) {
				nozk.delete(PATH_DEL,-1);
				System.out.println(prefix + "ɾ���ɹ�");
			}
		} catch (Exception e) {
			System.err.println(prefix + "ɾ��ʧ�ܣ�ԭ���ǣ�" + e.getMessage());
		}
	}

	/**
	 * ���ô��������ɾ���ڵ�
	 */
	static void deleteNodeByBadAuthentication() throws Exception {

		String prefix = "[ʹ�ô������Ȩ��Ϣ]";

		try {
			System.out.println(prefix + "ɾ���ڵ㣺" + PATH_DEL);
			ZooKeeper badzk = new ZooKeeper(CONNECT_ADDR, 2000, null);
			//��Ȩ
			badzk.addAuthInfo(authentication_type,badAuthentication.getBytes());
			Thread.sleep(2000);
			Stat stat = badzk.exists(PATH_DEL, false);
			if (stat!=null) {
				badzk.delete(PATH_DEL, -1);
				System.out.println(prefix + "ɾ���ɹ�");
			}
		} catch (Exception e) {
			System.err.println(prefix + "ɾ��ʧ�ܣ�ԭ���ǣ�" + e.getMessage());
		}
	}

	/**
	 * ʹ����ȷ������ɾ���ڵ�
	 */
	static void deleteNodeByCorrectAuthentication() throws Exception {

		String prefix = "[ʹ����ȷ����Ȩ��Ϣ]";

		try {
			System.out.println(prefix + "ɾ���ڵ㣺" + PATH_DEL);
			Stat stat = zk.exists(PATH_DEL, false);
			if (stat!=null) {
				zk.delete(PATH_DEL, -1);
				System.out.println(prefix + "ɾ���ɹ�");
			}
		} catch (Exception e) {
			System.out.println(prefix + "ɾ��ʧ�ܣ�ԭ���ǣ�" + e.getMessage());
		}
	}

	/**
	 * ʹ����ȷ������ɾ���ڵ�
	 */
	static void deleteParent() throws Exception {
		try {
			Stat stat = zk.exists(PATH_DEL, false);
			if (stat == null) {
				zk.delete(PATH, -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		ZookeeperAuth testAuth = new ZookeeperAuth();
		testAuth.createConnection(CONNECT_ADDR, 2000);
		List<ACL> acls = new ArrayList<ACL>();
		for (ACL ids_acl : Ids.CREATOR_ALL_ACL) {
			acls.add(ids_acl);
		}
		
		try {//�����ڵ�
			if(zk.exists(PATH, true) == null){
				zk.create(PATH, "init content".getBytes(), acls, CreateMode.PERSISTENT);
				System.out.println("ʹ����Ȩkey��" + correctAuthentication + "�����ڵ�"
						+PATH+",��ʼ�������ǣ�init content");
			}else{
				System.out.println("�ýڵ��Ѿ����ڡ�������");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		getDataByNoAuthentication();
		getDataByBadAuthentication();
		getDataByCorrectAuthentication();
	}

}
