package cn.leancloud;

import cn.leancloud.callback.FollowersAndFolloweesCallback;
import cn.leancloud.utils.ErrorUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import retrofit2.HttpException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class AVUserFollowshipTest extends TestCase {
  private boolean operationSucceed = false;
  public static final String JFENG_EMAIL = "jfeng@test.com";
  public static final String DENNIS_EMAIL = "dennis@test.com";
  public static final String JFENG_001_EMAIL = "jfeng001@test.com";
  public static String DEFAULT_PASSWD = "FER$@$@#Ffwe";

  private static String JFENG_OBJECT_ID = "5bff479067f3560066d00676";
  private static String DENNIS_OBJECT_ID = "5bff452afb4ffe0069a9893e";

  public AVUserFollowshipTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }

  public static Test suite() {
    return new TestSuite(AVUserFollowshipTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    try {
      prepareUser("jfeng", JFENG_EMAIL, true);
      prepareUser("dennis", DENNIS_EMAIL, true);
      prepareUser("jfeng001", JFENG_001_EMAIL, false);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    operationSucceed = false;
  }

  @Override
  protected void tearDown() throws Exception {
    ;
  }

  public static void prepareUser(String username, final String email, final boolean loginOnFailed) throws Exception {
    AVUser user = new AVUser();
    user.setEmail(email);
    user.setUsername(username);
    user.setPassword(DEFAULT_PASSWD);
    final CountDownLatch latch = new CountDownLatch(1);
    user.signUpInBackground().subscribe(new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {

      }

      public void onNext(AVUser avUser) {
        if (loginOnFailed) {
          if (email.startsWith("jfeng")) {
            JFENG_OBJECT_ID = avUser.getObjectId();
          } else if (email.startsWith("dennis")) {
            DENNIS_OBJECT_ID = avUser.getObjectId();
          }
        }
        latch.countDown();

      }

      public void onError(Throwable throwable) {
        if (loginOnFailed) {
          AVUser tmp = AVUser.loginByEmail(email, DEFAULT_PASSWD).blockingFirst();
          if (email.startsWith("jfeng")) {
            JFENG_OBJECT_ID = tmp.getObjectId();
          } else if (email.startsWith("dennis")) {
            DENNIS_OBJECT_ID = tmp.getObjectId();
          }
        }

        latch.countDown();
      }

      public void onComplete() {

      }
    });
    latch.await();
  }

  public void testFolloweeQuery() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    AVUser.logIn("jfeng", DEFAULT_PASSWD).subscribe(new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(AVUser avUser) {
        System.out.println("onNext. result=" + JSON.toJSONString(avUser, ObjectValueFilter.instance,
                SerializerFeature.WriteClassName,
                SerializerFeature.DisableCircularReferenceDetect));

        AVUser currentUser = AVUser.getCurrentUser();
        System.out.println("currentUser. result=" + JSON.toJSONString(currentUser, ObjectValueFilter.instance,
                SerializerFeature.WriteClassName,
                SerializerFeature.DisableCircularReferenceDetect));
        System.out.println("sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

        AVQuery<AVObject> query = avUser.followeeQuery();
        List<AVObject> followees = query.find();
        if (null == followees || followees.size() < 1) {
          avUser.followInBackground(DENNIS_OBJECT_ID).subscribe(new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable disposable) {

            }

            @Override
            public void onNext(JSONObject jsonObject) {
              System.out.println(jsonObject.toJSONString());
              operationSucceed = true;
              latch.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
              throwable.printStackTrace();
              latch.countDown();
            }

            @Override
            public void onComplete() {

            }
          });

        } else {
          operationSucceed = true;
          latch.countDown();
        }
      }

      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      public void onComplete() {
        System.out.println("onComplete");
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testFollowerQuery() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    AVUser.logIn("jfeng", DEFAULT_PASSWD).subscribe(new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(AVUser avUser) {
        System.out.println("onNext. result=" + JSON.toJSONString(avUser, ObjectValueFilter.instance,
                SerializerFeature.WriteClassName,
                SerializerFeature.DisableCircularReferenceDetect));

        AVUser currentUser = AVUser.getCurrentUser();
        System.out.println("currentUser. result=" + JSON.toJSONString(currentUser, ObjectValueFilter.instance,
                SerializerFeature.WriteClassName,
                SerializerFeature.DisableCircularReferenceDetect));
        System.out.println("sessionToken=" + currentUser.getSessionToken() + ", isAuthenticated=" + currentUser.isAuthenticated());

        AVQuery<AVObject> query = avUser.followerQuery();
        List<AVObject> followers = query.find();
        operationSucceed = true;
        latch.countDown();
      }

      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      public void onComplete() {
        System.out.println("onComplete");
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testFollow() throws Exception {
    AVUser logginUser = AVUser.logIn("jfeng001", DEFAULT_PASSWD).blockingFirst();
    logginUser.followInBackground(JFENG_OBJECT_ID).blockingFirst();

    AVUser jfeng = AVUser.logIn("jfeng", DEFAULT_PASSWD).blockingFirst();

    final CountDownLatch latch = new CountDownLatch(1);
    AVQuery query = jfeng.followerQuery();
    query.findInBackground().subscribe(new Observer<List<AVObject>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<AVObject> o) {
        for (AVObject tmp: o) {
          System.out.println("result User:" + tmp);
          if ("jfeng001".equals(tmp.getAVObject("follower").getString("username"))) {
            operationSucceed = true;
          }
        }
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testUnfollow() throws Exception {
    AVUser logginUser = AVUser.logIn("jfeng001", DEFAULT_PASSWD).blockingFirst();
    logginUser.unfollowInBackground(JFENG_OBJECT_ID).blockingFirst();

    AVUser jfeng = AVUser.logIn("jfeng", DEFAULT_PASSWD).blockingFirst();

    final CountDownLatch latch = new CountDownLatch(1);
    AVQuery query = jfeng.followerQuery();
    query.findInBackground().subscribe(new Observer<List<AVObject>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<AVObject> o) {
        System.out.println("onNext");
        operationSucceed = (null == o) || o.size() < 1;
        latch.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("onError");
        throwable.printStackTrace();
        latch.countDown();
      }

      @Override
      public void onComplete() {
        System.out.println("onComplete");
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testFollowUserNotLogin() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    AVQuery<? extends AVUser> query = AVUser.getQuery();
    query.findInBackground().subscribe(new Observer<List<? extends AVUser>>() {
      @Override
      public void onSubscribe(Disposable disposable) {

      }

      @Override
      public void onNext(List<? extends AVUser> avUsers) {
        AVUser target = avUsers.get(0);
        target.followInBackground("5bff479067f3560066d00676").subscribe(new Observer<JSONObject>() {
          @Override
          public void onSubscribe(Disposable disposable) {

          }

          @Override
          public void onNext(JSONObject jsonObject) {
            latch.countDown();
          }

          @Override
          public void onError(Throwable throwable) {
            throwable.printStackTrace();
            operationSucceed = true;
            latch.countDown();
          }

          @Override
          public void onComplete() {

          }
        });

      }

      @Override
      public void onError(Throwable throwable) {
        throwable.printStackTrace();
        latch.countDown();
      }

      @Override
      public void onComplete() {

      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

  public void testFolloweeAndFollowerQuery() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    AVUser.logIn("jfeng", DEFAULT_PASSWD).subscribe(new Observer<AVUser>() {
      public void onSubscribe(Disposable disposable) {
        System.out.println("onSubscribe " + disposable.toString());
      }

      public void onNext(AVUser avUser) {
        avUser.getFollowersAndFolloweesInBackground(new FollowersAndFolloweesCallback() {
          @Override
          public void done(Map avObjects, AVException avException) {
            operationSucceed = (null != avObjects);
            System.out.println(JSON.toJSONString(avObjects));
            latch.countDown();
          }

        });
      }

      public void onError(Throwable throwable) {
        latch.countDown();
      }

      public void onComplete() {
      }
    });
    latch.await();
    assertTrue(operationSucceed);
  }

}
