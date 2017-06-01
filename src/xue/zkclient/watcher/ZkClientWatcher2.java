package xue.zkclient.watcher;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;

/**
 * zkc.subscribeDataChanges 
 * 		对监听节点的数据变化进行监听 子节点的数据的变化不接听
 * 		zkc.delete()不能递归删除？？
 * @author Administrator
 *
 */
public class ZkClientWatcher2 {
	/** zookeeper地址**/
	static final String CONNECT_ADDR = "192.168.1.191:2181,192.168.1.220:2181,192.168.1.221:2181";
	/** session超时时间**/
	static final int SESSION_OUTTIME = 5000;//MS
	
	public static void main(String[] args) throws Exception {
		ZkClient zkc = new ZkClient(new ZkConnection(CONNECT_ADDR),10000);
		
		zkc.createPersistent("/super","123456");
		zkc.subscribeDataChanges("/super", new IZkDataListener() {
			
			@Override
			public void handleDataDeleted(String path) throws Exception {
				System.out.println("删除的节点为：" + path);
			}
			
			@Override
			public void handleDataChange(String path, Object data) throws Exception {
				System.out.println("变更的节点为：" + path + ",变更内容为："+ data);
			}
		});
		
		Thread.sleep(1000);
		zkc.writeData("/super", "456",-1);
		Thread.sleep(5000);
		
		System.out.println(zkc.exists("/super/c1"));
		if(!zkc.exists("/super/c1")){
			zkc.createPersistent("/super/c1", "c1内容");
			Thread.sleep(5000);
		}
		
		zkc.delete("/super/c1");
		zkc.delete("/super");
		zkc.close();
	}
}
