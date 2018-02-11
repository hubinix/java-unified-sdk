package cn.leancloud;

import android.os.Parcel;
import android.os.Parcelable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class AVObject extends cn.leancloud.core.AVObject implements Parcelable {
  public AVObject(String className) {
    super(className);
  }

  public AVObject(Parcel in) {
    super("");
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int i) {
    out.writeString(this.getClassName());
    out.writeString(this.getCreatedAt());
    out.writeString(this.getUpdatedAt());
    out.writeString(this.getObjectId());
//    out.writeString(JSON.toJSONString(serverData, new ObjectValueFilter(),
//            SerializerFeature.NotWriteRootClassName, SerializerFeature.WriteClassName));
//    out.writeString(JSON.toJSONString(operationQueue, SerializerFeature.WriteClassName,
//            SerializerFeature.NotWriteRootClassName));
  }

  public static transient final Creator CREATOR = AVObjectCreator.instance;

  public static class AVObjectCreator implements Creator {
    public static AVObjectCreator instance = new AVObjectCreator();

    private AVObjectCreator() {

    }

    @Override
    public AVObject createFromParcel(Parcel parcel) {
      AVObject avobject = new AVObject(parcel);
//      Class<? extends AVObject> subClass = AVUtils.getAVObjectClassByClassName(avobject.getClassName());
//      if (subClass != null) {
//        try {
//          AVObject returnValue = AVObject.cast(avobject, subClass);
//          return returnValue;
//        } catch (Exception e) {
//        }
//      }
      return avobject;
    }

    @Override
    public AVObject[] newArray(int i) {
      return new AVObject[i];
    }
  }

}