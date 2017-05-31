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
		 * byte[] data ： 节点内容：要求类型是字节数组 不支持java序列化对象
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
		
		/*String create = zk.create("/testRoot/children", "children data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		System.out.println(create);*/
		
		
//		zk.delete("/testRoot", -1, new VoidCallback() {
//			/**
//			 * rc 为服务端响应码，0 表示调用成功，-4表示端口连接，-110表示指定节点存在，-112表示会话已经过期
//			 * path:接口调用时传入spi的数据节点的路径参数
//			 * ctx：为调用接口传入api的ctx值
//			 * name：实际在分区器端创建节点的名称
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
		
		//获取节点中的数据
		/*byte[] data = zk.getData("/testRoot", false, null);
		System.out.println(new String(data));*/
		
		//获取子节点
		/*List<String> children = zk.getChildren("/testRoot", false);
		for (String path : children) {
			System.out.println(path);
			String realPath = "/testRoot" + path;
			System.out.println(new String(zk.getData(realPath, false, null)));
		}*/
		
		//修改节点
		/*zk.setData("/testRoot", "modify data root".getBytes(), -1);
		byte[] data = zk.getData("/testRoot", false, null);
		System.out.println(new String(data));
		*/
		
		//判断一个节点是否存在
		System.out.println(zk.exists("/testRoot", false));
		
		zk.close();
	}
}
