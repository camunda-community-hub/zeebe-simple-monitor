package io.zeebe.monitor.rest;

public class BpmnElementInfo {

  private String elementId;
  private String info;

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }
}
