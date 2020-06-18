package cn.leancloud;

import cn.leancloud.callback.CountCallback;
import cn.leancloud.callback.DeleteCallback;
import cn.leancloud.callback.FindCallback;
import cn.leancloud.convertor.ObserverBuilder;
import cn.leancloud.query.AVQueryResult;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class QueryUnitTest extends TestCase {
  private static String className = QueryUnitTest.class.getSimpleName();
  private List<AVObject> resultObjects = new ArrayList<>(10);

  public QueryUnitTest(String name) {
    super(name);
    Configure.initializeRuntime();
  }
  public static Test suite() {
    return new TestSuite(QueryUnitTest.class);
  }

  @Override
  protected void setUp() throws Exception {
    AVQuery.clearAllCachedResults();
    setUpClass();
  }

  @Override
  protected void tearDown() throws Exception {
    for (AVObject o : resultObjects) {
      o.delete();
    }
    resultObjects.clear();
  }

  public void setUpClass() {
    resultObjects.clear();
    try {
      for (int i = 0; i < 5; i++) {
        AVObject player = new AVObject("QueryUnitTestPlayer");
        player.put("name", "player" + i);
        player.put("age", 30 + i);
        if (i % 2 == 0) {
          player.put("image", i);
        }
        AVObject obj = new AVObject(className);
        obj.put("playerName", "player" + i);
        obj.put("player", player);
        obj.put("score", i * 100);
        obj.addAll("scores", Arrays.asList(i, i + 1, i + 2, i + 3));
        obj.save();
        resultObjects.add(player);
        resultObjects.add(obj);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }


  public void testBasicQuery() throws Exception {
    AVQuery<AVObject> query = new AVQuery<AVObject>(className);
    query.whereEqualTo("playerName", "player1");
    FindCallback cb = new FindCallback<AVObject>() {
      public void done(List<AVObject> avObjects, AVException e) {
        assertNull(e);
        for (AVObject obj : avObjects) {
          assertEquals("player1", obj.get("playerName"));
        }
      }
    };
    query.findInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb));
    List<AVObject> avObjects = query.find();
    for (AVObject obj : avObjects) {
      assertEquals("player1", obj.get("playerName"));
    }
    assertTrue(avObjects != null && avObjects.size() > 0);
  }

  public void testQueryWithConditions() throws Exception {
    // whereNotEqualTo
    AVQuery<AVObject> query = new AVQuery<AVObject>(className);
    query.whereNotEqualTo("playerName", "player1");

    FindCallback<AVObject> cb = new FindCallback<AVObject>() {
      public void done(List<AVObject> avObjects, AVException e) {
        assertNull(e);
        for (AVObject obj : avObjects) {
          assertFalse("player1".equals(obj.get("playerName")));
        }
      }
    };
    query.findInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb));

    // whereGreaterThan
    query = new AVQuery<AVObject>(className);
    query.whereGreaterThan("score", 200);
    List<AVObject> avObjects = query.find();
    assertFalse(avObjects.isEmpty());
    for (AVObject obj : avObjects) {
      assertTrue(obj.getInt("score") > 200);
    }

    // whereLessThan
    query = new AVQuery<AVObject>(className);
    query.whereLessThan("score", 200);
    avObjects = query.find();
    assertFalse(avObjects.isEmpty());
    for (AVObject obj : avObjects) {
      assertTrue(obj.getInt("score") < 200);
    }

    // whereLessThanOrEqualTo
    query = new AVQuery<AVObject>(className);
    query.whereLessThanOrEqualTo("score", 200);
    avObjects = query.find();
    assertFalse(avObjects.isEmpty());
    for (AVObject obj : avObjects) {
      assertTrue(obj.getInt("score") <= 200);
    }

    // whereGreaterThanOrEqualTo
    query = new AVQuery<AVObject>(className);
    query.whereGreaterThanOrEqualTo("score", 200);
    avObjects = query.find();
    assertFalse(avObjects.isEmpty());
    for (AVObject obj : avObjects) {
      assertTrue(obj.getInt("score") >= 200);
    }

    // whereNotContainedIn
    query = new AVQuery<AVObject>(className);
    List<String> names = Arrays.asList("player1", "player0");
    query.whereNotContainedIn("playerName", names);
    avObjects = query.find();
    assertFalse(avObjects.isEmpty());
    for (AVObject obj : avObjects) {
      assertNotContains(obj.get("playerName"), names);
    }

    // whereContainedIn
    query = new AVQuery<AVObject>(className);
    query.whereContainedIn("playerName", names);
    avObjects = query.find();
    assertFalse(avObjects.isEmpty());
    for (AVObject obj : avObjects) {
      assertContains(obj.get("playerName"), names);
    }

    // whereMatches
    query = new AVQuery<AVObject>(className);
    query.whereMatches("playerName", "^[a-zA-Z]+1");
    avObjects = query.find();
    for (AVObject obj : avObjects) {
      assertEquals("player1", obj.get("playerName"));
    }
    assertTrue(avObjects != null && avObjects.size() > 0);

    // whereContains
    query = new AVQuery<AVObject>(className);
    query.whereContains("playerName", "0");
    avObjects = query.find();
    assertFalse(avObjects.isEmpty());
    for (AVObject obj : avObjects) {
      assertEquals("player0", obj.get("playerName"));
    }

    // whereEndsWith
    query = new AVQuery<AVObject>(className);
    query.whereEndsWith("playerName", "0");
    avObjects = query.find();
    assertFalse(avObjects.isEmpty());
    for (AVObject obj : avObjects) {
      assertEquals("player0", obj.get("playerName"));
    }

  }

  public void testQueryArrayKey() throws Exception {
    AVQuery query = new AVQuery(className);
    query.whereEqualTo("scores", 2);
    List<AVObject> avObjects = query.find();
    assertFalse(avObjects.isEmpty());
    for (AVObject obj : avObjects) {
      assertContains(2l, obj.getList("scores"));
    }

    // whereContainsAll
    ArrayList<Long> numbers = new ArrayList<Long>();
    numbers.add(2l);
    numbers.add(3l);
    numbers.add(4l);
    numbers.add(5l);
    query = new AVQuery(className);
    query.whereContainsAll("scores", numbers);
    avObjects = query.find();
    assertFalse(avObjects.isEmpty());
    for (AVObject obj : avObjects) {
      assertTrue(numbers.equals(obj.getList("scores")));
    }

  }

  public void testCountObjects() throws Exception {
    AVQuery query = new AVQuery(className);
    query.whereEqualTo("scores", 2);
    CountCallback cb = new CountCallback() {

      @Override
      public void done(int count, AVException e) {
        assertNull(e);
      }
    };
    query.countInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb));
    assertTrue(query.count() > 0);
  }

  public void testQueryWithPointer() throws Exception {
    AVQuery<AVObject> query = new AVQuery<AVObject>("QueryUnitTestPlayer");
    final AVObject player = query.getFirst();

    query = AVQuery.getQuery(className);
    query.whereEqualTo("player", player);

    FindCallback<AVObject> cb = new FindCallback<AVObject>() {

      @Override
      public void done(List<AVObject> avObjects, AVException avException) {
        assertNotNull(avObjects);
        assertTrue(avObjects.size() > 0);
        for (AVObject obj : avObjects) {
          assertTrue(obj.get("player").equals(player));
        }
      }

    };
    query.findInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb));
  }

  public void testWhereMatchesQuery() throws Exception {
    AVQuery<AVObject> innerQuery = AVQuery.getQuery("QueryUnitTestPlayer");
    innerQuery.whereExists("image");
    AVQuery<AVObject> query = AVQuery.getQuery(className);
    query.whereMatchesQuery("player", innerQuery);
    query.include("player");

    FindCallback<AVObject> cb = new FindCallback<AVObject>() {
      public void done(List<AVObject> avObjects, AVException e) {
        assertNotNull(avObjects);
        assertTrue(avObjects.size() > 0);
        for (AVObject obj : avObjects) {
          assertTrue(obj.getAVObject("player").has("image"));
        }
      }
    };
    query.findInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb));
  }

  public void testWhereDoesNotMatchQuery() throws Exception {
    AVQuery<AVObject> innerQuery = AVQuery.getQuery("QueryUnitTestPlayer");
    innerQuery.whereExists("image");
    AVQuery<AVObject> query = AVQuery.getQuery(className);
    query.whereDoesNotMatchQuery("player", innerQuery);
    query.include("player");
    List<AVObject> biggerResult = query.find();
    Set<String> playerIds = new HashSet<String>();
    for(AVObject o:biggerResult){
      if (null != o.getAVObject("player")) {
        playerIds.add(o.getAVObject("player").getObjectId());
      }
    }
    List<AVObject> innerResult = innerQuery.find();
    boolean flag = false;
    for(AVObject o:innerResult){
      if(playerIds.contains(o.getObjectId())){
        flag = true;
        break;
      }
    }
    Assert.assertFalse(flag);
  }

  public void testQueryCacheOnly() throws Exception {
    final AVQuery<AVObject> query = AVQuery.getQuery(className);
    query.setCachePolicy(AVQuery.CachePolicy.CACHE_ONLY);

    List<AVObject> objects = query.find();
    assertTrue(objects.size() < 1);
  }

  public void testQueryNetworkOnly() throws Exception {
    AVQuery<AVObject> query = AVQuery.getQuery(className);
    query.setCachePolicy(AVQuery.CachePolicy.NETWORK_ONLY);
    List<AVObject> objects = query.find();
    assertTrue(objects.size() > 0);
    assertTrue(query.hasCachedResult());

    query = AVQuery.getQuery(className);
    query.setCachePolicy(AVQuery.CachePolicy.CACHE_ONLY);
    List<AVObject> objectsInCache = query.find();
    assertFalse(objectsInCache.isEmpty());
//    assertEquals(objectsInCache, objects);

    // clear cache
    AVQuery.clearAllCachedResults();
    final AVQuery<AVObject> missQuery = AVQuery.getQuery(className);
    missQuery.setCachePolicy(AVQuery.CachePolicy.CACHE_ONLY);
    List<AVObject> result = missQuery.find();
    assertTrue(result.size() < 1);
  }

  public void testQueryCacheElseNetwork() throws Exception {
    AVQuery<AVObject> query = AVQuery.getQuery(className);
    query.setCachePolicy(AVQuery.CachePolicy.CACHE_ELSE_NETWORK);
    List<AVObject> objects = query.find();
    assertTrue(objects.size() > 0);

    // Next time,we can get it from cache
    query = AVQuery.getQuery(className);
    query.setCachePolicy(AVQuery.CachePolicy.CACHE_ONLY);
    List<AVObject> objectsInCache = query.find();
    assertFalse(objectsInCache.isEmpty());
//    assertEquals(objectsInCache, objects);
  }

  public void testQueryCacheDeserializer() throws Exception {
    String content = "{\"className\":\"QueryUnitTest\",\"count\":0,\"results\":[{\"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":0,\"createdAt\":\"2019-01-03T05:03:26.361Z\",\"playerName\":\"player0\",\"scores\":[0,1,2,3],\"objectId\":\"5c2d979e1579a3005f9296e5\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c2d971b4773f717360327d2\"}},\"updatedAt\":\"2019-01-03T05:03:26.364Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":100,\"createdAt\":\"2019-01-03T05:03:30.704Z\",\"playerName\":\"player1\",\"scores\":[1,2,3,4],\"objectId\":\"5c2d97a24773f71736032f49\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c2d97a14773f71736032f3e\"}},\"updatedAt\":\"2019-01-03T05:03:30.782Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":200,\"createdAt\":\"2019-01-03T05:03:34.395Z\",\"playerName\":\"player2\",\"scores\":[2,3,4,5],\"objectId\":\"5c2d97a667f356005fdd9a8f\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c2d97a47f6fd3005dc5ae6c\"}},\"updatedAt\":\"2019-01-03T05:03:34.398Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":300,\"createdAt\":\"2019-01-03T05:03:37.446Z\",\"playerName\":\"player3\",\"scores\":[3,4,5,6],\"objectId\":\"5c2d97a967f356005fdd9ad9\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c2d97a7fb4ffe005fdde06a\"}},\"updatedAt\":\"2019-01-03T05:03:37.449Z\"}},{\"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":400,\"createdAt\":\"2019-01-03T05:03:40.987Z\",\"playerName\":\"player4\",\"scores\":[4,5,6,7],\"objectId\":\"5c2d97ac9f54540070306b8a\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c2d97ab1579a3005f929777\"}},\"updatedAt\":\"2019-01-03T05:03:40.991Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":0,\"createdAt\":\"2019-01-07T02:46:53.538Z\",\"playerName\":\"player0\",\"scores\":[0,1,2,3],\"objectId\":\"5c32bd9d44d904005d310fcd\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c32bd9d9f545400720f629b\"}},\"updatedAt\":\"2019-01-07T02:46:53.541Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":100,\"createdAt\":\"2019-01-07T02:46:53.627Z\",\"playerName\":\"player1\",\"scores\":[1,2,3,4],\"objectId\":\"5c32bd9d67f35600634f5401\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c32bd9d0b61600067a811d8\"}},\"updatedAt\":\"2019-01-07T02:46:53.630Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":200,\"createdAt\":\"2019-01-07T02:46:53.768Z\",\"playerName\":\"player2\",\"scores\":[2,3,4,5],\"objectId\":\"5c32bd9d0b61600067a811dc\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c32bd9d44d904005d310fcf\"}},\"updatedAt\":\"2019-01-07T02:46:53.772Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":300,\"createdAt\":\"2019-01-07T02:46:53.839Z\",\"playerName\":\"player3\",\"scores\":[3,4,5,6],\"objectId\":\"5c32bd9dfb4ffe1186b40677\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c32bd9d9f545400720f62a2\"}},\"updatedAt\":\"2019-01-07T02:46:53.843Z\"}},{ \"_version\":\"5\",\"className\":\"QueryUnitTest\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"score\":400,\"createdAt\":\"2019-01-07T02:46:53.919Z\",\"playerName\":\"player4\",\"scores\":[4,5,6,7],\"objectId\":\"5c32bd9d67f35600634f540e\",\"player\":{ \"@type\":\"cn.leancloud.AVObject\",\"_version\":\"5\",\"className\":\"QueryUnitTestPlayer\",\"serverData\":{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"className\":\"QueryUnitTestPlayer\",\"objectId\":\"5c32bd9d9f545400720f62a5\"}},\"updatedAt\":\"2019-01-07T02:46:53.922Z\"}}]}";
    AVQueryResult result = AVQueryResult.fromJSONString(content);
    assertNotNull(result);
    assertTrue(result.getResults().size() > 0);
  }
  public void testQueryCacheThenNetwork() throws Exception {
    AVQuery<AVObject> query = AVQuery.getQuery(className);
    query.setCachePolicy(AVQuery.CachePolicy.CACHE_THEN_NETWORK);
    final AtomicInteger counter = new AtomicInteger(0);
    FindCallback<AVObject> cb = new FindCallback<AVObject>() {

      @Override
      public void done(List<AVObject> avObjects, AVException avException) {
        assertTrue((avObjects != null && avObjects.size() > 0) || avException != null);
        if (avException != null) {
          assertEquals(AVException.CACHE_MISS, avException.getCode());
        }
        if (counter.incrementAndGet() < 2) {
          fail();
        }
      }

    };
    query.findInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb));
  }

  public void testCompondQuery() throws Exception {
    AVQuery<AVObject> lotsOfWins = AVQuery.getQuery(className);
    lotsOfWins.whereGreaterThan("score", 150);

    AVQuery<AVObject> fewWins = AVQuery.getQuery(className);
    fewWins.whereLessThan("score", 5);

    List<AVQuery<AVObject>> queries = new ArrayList<AVQuery<AVObject>>();
    queries.add(lotsOfWins);
    queries.add(fewWins);

    AVQuery<AVObject> mainQuery = AVQuery.or(queries);
    FindCallback<AVObject> cb = new FindCallback<AVObject>() {
      public void done(List<AVObject> results, AVException e) {
        assertTrue(results.size() > 0);
      }
    };
    mainQuery.findInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb));
  }

  public void testDeleteAll() throws Exception {
    for (int i = 0; i < 5; i++) {
      AVObject obj = new AVObject("QueryUnitTest_DeleteAll");
      obj.put("number", i);
      obj.save();
    }
    AVQuery<AVObject> query = new AVQuery<AVObject>("QueryUnitTest_DeleteAll");
    List<AVObject> list = query.find();
    assertTrue(list.size() > 0);
    query.deleteAll();
    list = query.find();
    assertTrue(list.size() == 0);
  }

  public void testWhereSizeEqual() throws Exception {
    AVQuery<AVObject> query = new AVQuery<AVObject>(className);
    query.whereSizeEqual("scores", 4);
    List<AVObject> objects = query.find();
    assertTrue(objects.size() > 0);
    for (AVObject obj : objects) {
      assertTrue(4 == obj.getList("scores").size());
    }
  }

  public void testDeleteAllInBackground() throws Exception {
    for (int i = 0; i < 5; i++) {
      AVObject obj = new AVObject("QueryUnitTest_DeleteAll");
      obj.put("number", i);
      obj.save();
    }
    AVQuery<AVObject> query = new AVQuery<AVObject>("QueryUnitTest_DeleteAll");
    List<AVObject> list = query.find();
    assertTrue(list.size() > 0);

    DeleteCallback cb = new DeleteCallback() {
      @Override
      public void done(AVException e) {
        if (null != e) {
          fail();
        }

      }
    };
    query.deleteAllInBackground().subscribe(ObserverBuilder.buildSingleObserver(cb));

    list = query.find();
    assertTrue(list.size() == 0);
  }

  private void assertNotContains(Object x, Collection<?> list) {
    for (Object obj : list) {
      assertFalse(x.equals(obj));
    }
  }

  private void assertContains(Object x, Collection<?> list) {
    boolean contains = false;
    for (Object obj : list) {
      if (x.equals(obj)) {
        contains = true;
      }
    }
    assertTrue(contains);
  }
}
