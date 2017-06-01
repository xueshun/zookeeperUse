package xue.zkclient.watcher;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

/**
 * zkc.subscribeChildChanges() 
 *   ֻ��������ڵ��µ��ӽڵ�������ɾ�� 
 *   	���ӽڵ���ӻ�ɾ���ڵ�����ݲ�����
 * @author Administrator
 *
 */
public class ZkClientWathcer1 {
	/** zookeeper��ַ**/
	static final String CONNECT_ADDR = "192.168.1.191:2181,192.168.1.220:2181,192.168.1.221:2181";
	/** session��ʱʱ��**/
	static final int SESSION_OUTTIME = 5000;//MS
	
	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR),10000);
		zkc.createPersistent("/super","1234");
		
		//�Ը��ڵ���Ӽ����ӽڵ�仯
		zkc.subscribeChildChanges("/super", new IZkChildListener() {
			@Override
			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				System.out.println("parentPath: " + parentPath);
				System.out.println("currentChilds : "+ currentChilds);
			}
		});
		
		Thread.sleep(3000);
		
		/*zkc.createPersistent("/super");
		Thread.sleep(1000);*/
		
		zkc.createPersistent("/super" + "/" + "c1","c1 content");
		Thread.sleep(1000);
		
		zkc.createPersistent("/super" + "/" + "c2","c2 content");
		Thread.sleep(1000);
		
		zkc.delete("/super/c2");
		Thread.sleep(1000);
		
		zkc.delete("/super/c1");
		Thread.sleep(1000);
		
		zkc.delete("/super");
		Thread.sleep(Integer.MAX_VALUE);
		
		zkc.close();
	}
}
