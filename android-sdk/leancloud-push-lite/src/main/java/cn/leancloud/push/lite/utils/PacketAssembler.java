package cn.leancloud.push.lite.utils;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.leancloud.push.lite.proto.CommandPacket;
import cn.leancloud.push.lite.proto.Messages;
import cn.leancloud.push.lite.proto.PushAckPacket;

public class PacketAssembler {
  private static final String TAG = PacketAssembler.class.getSimpleName();
  static AtomicInteger acu = new AtomicInteger(-65536);
  private static PacketAssembler instance = new PacketAssembler();
  public static PacketAssembler getInstance() {
    return instance;
  }

  public static int getNextIMRequestId() {
    int val = acu.incrementAndGet();
    if (val > 65535) {
      while (val > 65535 && !acu.compareAndSet(val, -65536)) {
        val = acu.get();
      }
      return val;
    } else {
      return val;
    }
  }

//  public CommandPacket assembleSessionOpenPacket(String clientId, String tag, Signature signature, long lastNotifyTime,
//                                                 long lastPatchTime, boolean reConnect, int requestId) {
//    SessionControlPacket scp = SessionControlPacket.genSessionCommand(
//        clientId, null,
//        SessionControlPacket.SessionControlOp.OPEN, signature,
//        lastNotifyTime, lastPatchTime, requestId);
//    scp.setTag(tag);
//    scp.setReconnectionRequest(reConnect);
//    return scp;
//  }
//
//  public CommandPacket assembleSessionPacket(String selfId, List<String> peers,
//                                             String op, Signature signature, Integer requestId) {
//    SessionControlPacket scp = SessionControlPacket.genSessionCommand(selfId, peers, op, signature, requestId);
//    return scp;
//  }

  public CommandPacket assemblePushAckPacket(String installationId, List<String> messageIds) {
    PushAckPacket pushAckPacket = new PushAckPacket();
    pushAckPacket.setInstallationId(installationId);
    pushAckPacket.setMessageIds(messageIds);
    return pushAckPacket;
  }


  public Messages.GenericCommand disassemblePacket(ByteBuffer bytes) {
    try {
      return Messages.GenericCommand.parseFrom(bytes);
    } catch (InvalidProtocolBufferException ex) {
      Log.e(TAG, "failed to disassemble packet.", ex);
      return null;
    }
  }
}
