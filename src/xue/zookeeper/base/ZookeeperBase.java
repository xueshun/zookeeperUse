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
	
	/*zookeeper地址*/
	static final String CONNECT_ADDR ="192.168.1.191:2181,192.168.1.220:2181,192.168.1.221:2181";
	/**session超时时间 **/
	static final int SESSION_OUTTIME = 2000;//ms
	/**信号量，阻塞程序执行，用于等待zookeeper连接成功，发送成功信号 connectedSemaphore.await();
	 * 因为zookeeper客户端连接服务端是异步连接的
	 * **/
	static final CountDownLatch connectedSemaphore = new CountDownLatch(1);
	
	public static void main(String[] args) throws Exception {
		ZooKeeper zk =new ZooKeeper(CONNECT_ADDR, SESSION_OUTTIME, new Watcher(){
			@Override
			public void process(WatchedEvent event) {
				/*获取事件的状态*/
				KeeperState keeperState = event.getState();
				//获取事件的类型
				EventType eventType = event.getType();
				//如果建立连接
				if(KeeperState.SyncConnected == keeperState){
					if(EventType.None ==eventType){
						//如果建立连接成功，则发送信号量，让后续阻塞程序向下执行
						connectedSemaphore.countDown();
						System.out.println("zk 建立连接");
					}
				}
			}
		});
		
		//进行阻塞
		connectedSemaphore.await();
		System.out.println("..");
		
		//创建父节点
		/**
		 * String path : 节点路径在那个目录下创建节点 不允许递归创建节点，也就是说父节点不存在的情况下，不允许创建子节点
		 * byte[] data ： 节点内容：要求类型是字节数组
		 * List<ACL> acl ： 节点权限
		 * CreateMode createMode ： 节点的创建方式
		 * 		PERSISTENT ： 持久节点
		 * 		PERSISTENT_SEQUENTIAL : 持久顺序节点
		 * 		EPHEMERAL ： 临时节点  本次有效   
		 * 		EPHEMERAL_SEQUENTIAL : 临时顺序节点
		 * 
		 * 节点的名字不可以重复
		 */
	/*	String create = zk.create("/testRoot", "testRoot".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		System.out.println(create);*/
		
		String create = zk.create("/testRoot/children", "children data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		System.out.println(create);
		
		zk.close();
	}
}
