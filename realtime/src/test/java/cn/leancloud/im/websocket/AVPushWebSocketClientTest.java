package cn.leancloud.im.websocket;

import cn.leancloud.AVLogger;
import cn.leancloud.Configure;
import cn.leancloud.core.AVOSCloud;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import okhttp3.Response;
import okio.ByteString;

public class AVPushWebSocketClientTest extends TestCase {
  public AVPushWebSocketClientTest(String testname) {
    super(testname);
    AVOSCloud.setRegion(AVOSCloud.REGION.NorthChina);
    AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
    AVOSCloud.initialize(Configure.TEST_APP_ID, Configure.TEST_APP_KEY);
  }

  public static Test suite()
  {
    return new TestSuite( AVPushWebSocketClientTest.class );
  }

  public void testConnect() throws Exception {
    String wsUrl = "wss://rtm51.leancloud.cn";
    AVPushWebSocketClient client = new AVPushWebSocketClient(new WsStatusListener(){
      public void onOpen(Response response) {
        System.out.println("websockdet opened!");
      }
      public void onMessage(String text) {

      }
      public void onMessage(ByteString bytes) {

      }
      public void onReconnect() {

      }
      public void onClosing(int code, String reason) {

      }
      public void onClosed(int code, String reason) {

      }
      public void onFailure(Throwable t, Response response) {

      }
    }, true);
    client.connect(wsUrl);
    Thread.sleep(6000);
    client.close();
    Thread.sleep(3000);
  }
}
