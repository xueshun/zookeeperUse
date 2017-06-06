package xue.curator.cluster;

public class Client1 {
	public static void main(String[] args) throws Exception {
		CuratorWatcher watcher = new CuratorWatcher();
		System.out.println("client1 start");
		Thread.sleep(100000000);
	}
}
