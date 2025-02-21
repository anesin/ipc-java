package com.seculetter.ipc;


import java.io.Serializable;


public class Message implements Serializable {

  private static final long serialVersionUID = 1L;

  private String content;


  public Message(String content) {
    this.content = content;
  }


  public String getContent() {
    return content;
  }


  public void setContent(String content) {
    this.content = content;
  }

}
