package com.sonarsource;

import java.io.File;

public class Main {

  public static void main(String[] args) throws Exception {
    new EmbeddedTomcat().start(getRootFolder());
  }

  private static File getRootFolder() {
    return new File("");
  }
}
