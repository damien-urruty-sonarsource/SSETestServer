package com.sonarsource;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.AsyncContext;

public class Client {
  private final AsyncContext clientContext;

  public Client(AsyncContext asyncContext) {
    this.clientContext = asyncContext;
  }

  public void send(String payload) {
    PrintWriter writer;
    try {
      writer = clientContext.getResponse().getWriter();
      // a message always:
      // 1. starts with data:
      // 2. ended by \n\n
      // see https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#event_stream_format
      writer.print("data: " + payload);
      writer.println();
      writer.println();
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
