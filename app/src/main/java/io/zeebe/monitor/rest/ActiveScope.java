package io.zeebe.monitor.rest;

public class ActiveScope {

  private long scopeKey;
  private String scopeName;

  public ActiveScope(long scopeKey, String scopeName) {
    this.scopeKey = scopeKey;
    this.scopeName = scopeName;
  }

  public long getScopeKey() {
    return scopeKey;
  }

  public void setScopeKey(long scopeKey) {
    this.scopeKey = scopeKey;
  }

  public String getScopeName() {
    return scopeName;
  }

  public void setScopeName(String scopeName) {
    this.scopeName = scopeName;
  }
}
