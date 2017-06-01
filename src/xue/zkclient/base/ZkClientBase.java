package xue.zkclient.base;

import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

/**
 * ZkClient �� ԭ����zookeeper������
 * 	1.���Եݹ鴴���ڵ�
 * 	2.��ʹ��Watcher
 * @author Administrator
 *
 */
public class ZkClientBase {

	/** zookeeper��ַ**/
	static final String CONNECT_ADDR="192.168.1.191:2181,192.168.1.220:2181,192.168.1.221:2181";
	/** session��ʱʱ��**/
	static final int SESSION_OUTTIME = 5000;//ms
	
	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR), 10000);
		
		/**
		 * 1.ZkClient �����ڵ�
		 * 	 ������ʽ������
		 * 		һ ��zkc.create(path, data, mode); ·�������ݣ����ݵ�ģʽ
		 * 		�� �� zkc.createEphemeral zkc.createEphemeral(path, data); "·��" ����
		 */
		/*zkc.createEphemeral("/temp");
		zkc.createPersistent("/super/c1", true);
		Thread.sleep(10000);*/
		
		//2. ����path��data ���Ҷ�ȡ�ӽڵ��ÿ���ڵ������
//		zkc.createPersistent("/super", "1234");
//		zkc.createPersistent("/super/c1", "c1����");
//		zkc.createPersistent("/super/c2", "c2����");
//		List<String> list = zkc.getChildren("/super");
//		for(String p : list){
//			System.out.println(p);
//			String rp = "/super/" + p;
//			String data = zkc.readData(rp);
//			System.out.println("�ڵ�Ϊ��" + rp + "������Ϊ: " + data);
//		}
		
		//3. ���º��жϽڵ��Ƿ����
//		zkc.writeData("/super/c1", "������");
//		System.out.println(zkc.readData("/super/c1"));
//		System.out.println(zkc.exists("/super/c1"));
		
		//4.�ݹ�ɾ��/super����
//		zkc.deleteRecursive("/super");	
		
		zkc.close();
		
	}
	
}
