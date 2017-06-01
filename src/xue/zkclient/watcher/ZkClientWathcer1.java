package xue.zkclient.watcher;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

/**
 * zkc.subscribeChildChanges() 
 *   只会监听父节点下的子节点的添加与删除 
 *   	对子节点添加或删除节点的数据不监听
 * @author Administrator
 *
 */
public class ZkClientWathcer1 {
	/** zookeeper地址**/
	static final String CONNECT_ADDR = "192.168.1.191:2181,192.168.1.220:2181,192.168.1.221:2181";
	/** session超时时间**/
	static final int SESSION_OUTTIME = 5000;//MS
	
	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR),10000);
		zkc.createPersistent("/super","1234");
		
		//对父节点添加监听子节点变化
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
