package com.sonarsource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Inspired by https://www.howopensource.com/2016/01/java-sse-chat-example/
 * and https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events
 */
@WebServlet(name = "EventStreamServlet", urlPatterns = "/api/sonarlint/streamEvents", asyncSupported = true)
public class EventStreamServlet extends HttpServlet {
  public static final long TIMEOUT = 10 * 60 * 1000L;

  private final Timer eventTimer;
  private final List<Client> clients = Collections.synchronizedList(new ArrayList<>());

  public EventStreamServlet() {
    eventTimer = new Timer();
    eventTimer.schedule(new SendPingsTask(clients), 0, 5_000L);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!isSse(request)) {
      return;
    }

    // Set header fields
    response.setContentType("text/event-stream");
    response.setHeader("Cache-Control", "no-cache");

    clients.add(newClient(request));
  }

  private static boolean isSse(HttpServletRequest request) {
    return "text/event-stream".equals(request.getHeader("Accept"));
  }

  private Client newClient(HttpServletRequest request) {
    // Start asynchronous context and add listeners to remove it in case of errors
    final AsyncContext context = request.startAsync();
    context.setTimeout(TIMEOUT);
    List<String> projectKeys = getProjectKeys(request);
    Client client = new Client(context, projectKeys);
    System.out.println("New client connected for projects:" + projectKeys);
    context.addListener(new AsyncListener() {
      @Override
      public void onComplete(AsyncEvent event) {
        System.out.println("Client completed");
        clients.remove(client);
      }

      @Override
      public void onError(AsyncEvent event) {
        System.out.println("Error happened");
        clients.remove(client);
      }

      @Override
      public void onStartAsync(AsyncEvent event) {
        // Do nothing
      }

      @Override
      public void onTimeout(AsyncEvent event) {
        System.out.println("Client timed out");
        clients.remove(client);
      }
    });
    return client;
  }

  private List<String> getProjectKeys(HttpServletRequest request) {
    String projectKeysParameter = request.getParameter("p");
    if (projectKeysParameter == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(projectKeysParameter.split(","));
  }

  @Override
  public void destroy() {
    eventTimer.cancel();
    clients.clear();
  }
}
