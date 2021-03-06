package jane.test;

import java.net.InetSocketAddress;
import jane.core.BeanCodec;
import jane.core.DBManager;
import jane.core.ExitManager;
import jane.core.Log;
import jane.bean.AllBeans;
import jane.bean.AllTables;
import jane.tool.StatusServer;

public final class TestMain
{
	public static void main(String[] args)
	{
		try
		{
			Log.removeAppendersFromArgs(args);
			Log.info("================================ startup: begin");
			Log.logSystemProperties(args);
			Log.logJarCreateTime();
			DBManager.instance().startup();
			AllTables.register();
			BeanCodec.registerAllBeans(AllBeans.getAllBeans());
			TestServer.instance().startServer(new InetSocketAddress("0.0.0.0", 9123));
			TestClient.instance().startClient(new InetSocketAddress("127.0.0.1", 9123));
			new StatusServer().startServer(new InetSocketAddress("0.0.0.0", 80));
			Log.info("================================ startup: end");
			ExitManager.waitStdInToExit();
		}
		catch(Throwable e)
		{
			Log.error("startup exception:", e);
			e.printStackTrace(System.err);
		}
	}
}
