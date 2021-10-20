package com.sonarsource;

import java.util.List;
import java.util.TimerTask;

public class SendPingsTask extends TimerTask {
  private final List<Client> clients;

  public SendPingsTask(List<Client> asyncContexts) {
    this.clients = asyncContexts;
  }

  @Override
  public void run() {
    clients.forEach(this::sendPing);
  }

  private void sendPing(Client client) {
    client.send("PING !!!");
  }
}
