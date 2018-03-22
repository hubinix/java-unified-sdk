package cn.leancloud;

public abstract class AVLogAdapter {
  private AVLogger.Level level = AVLogger.Level.INFO;
  public void setLevel(AVLogger.Level level) {
    this.level = level;
  }
  public AVLogger.Level getLevel() {
    return this.level;
  }

  public AVLogger getLogger(Class clazz) {
    if (null == clazz) {
      return null;
    }
    return getLogger(clazz.getCanonicalName());
  }
  public abstract AVLogger getLogger(String tag);
}