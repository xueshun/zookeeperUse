package xue.zkclient.watcher;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

/**
 * zkc.subscribeDataChanges 
 * 		�Լ����ڵ�����ݱ仯���м��� �ӽڵ�����ݵı仯������
 * 		zkc.delete()���ܵݹ�ɾ������
 * @author Administrator
 *
 */
public class ZkClientWatcher2 {
	/** zookeeper��ַ**/
	static final String CONNECT_ADDR = "192.168.1.191:2181,192.168.1.220:2181,192.168.1.221:2181";
	/** session��ʱʱ��**/
	static final int SESSION_OUTTIME = 5000;//MS
	
	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR),10000);
		
		zkc.createPersistent("/super","123456");
		zkc.subscribeDataChanges("/super", new IZkDataListener() {
			
			@Override
			public void handleDataDeleted(String path) throws Exception {
				System.out.println("ɾ���Ľڵ�Ϊ��" + path);
			}
			
			@Override
			public void handleDataChange(String path, Object data) throws Exception {
				System.out.println("����Ľڵ�Ϊ��" + path + ",�������Ϊ��"+ data);
			}
		});
		
		Thread.sleep(1000);
		zkc.writeData("/super", "456",-1);
		Thread.sleep(5000);
		
		System.out.println(zkc.exists("/super/c1"));
		if(!zkc.exists("/super/c1")){
			zkc.createPersistent("/super/c1", "c1����");
			Thread.sleep(5000);
		}
		
		zkc.delete("/super/c1");
		zkc.delete("/super");
		zkc.close();
	}
}
