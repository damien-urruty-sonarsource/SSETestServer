package com.sonarsource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.EmptyResourceSet;
import org.apache.catalina.webresources.StandardRoot;

/**
 * Inspired by https://github.com/SonarSource/sonar-enterprise/blob/master/server/sonar-webserver/src/main/java/org/sonar/server/app/EmbeddedTomcat.java
 */
public class EmbeddedTomcat {

  void start(File rootFolder) throws IOException {
    // '%2F' (slash /) and '%5C' (backslash \) are permitted as path delimiters in URLs
    System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");

    System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");
    // prevent Tomcat from shutting down our logging when stopping
    System.setProperty("logbackDisableServletContainerInitializer", "true");

    Tomcat tomcat = new Tomcat();
    tomcat.setPort(8080);
    // Initialize directories
    Path tempPath = Files.createTempDirectory("tomcat-base-dir");
    tomcat.setBaseDir(tempPath.toString());
    tomcat.getHost().setAutoDeploy(false);
    tomcat.getHost().setCreateDirs(false);
    tomcat.getHost().setDeployOnStartup(true);
    configure(tomcat, rootFolder);
    try {
      tomcat.start();
      tomcat.getServer().await();
    } catch (LifecycleException e) {
      e.printStackTrace();
    }
  }

  private static void configure(Tomcat tomcat, File rootFolder) {
    // Declare an alternative location for your "WEB-INF/classes" dir
    // Servlet 3.0 annotation will work
    File additionWebInfClassesFolder = new File(rootFolder.getAbsolutePath(), "build/classes");
    try {
      StandardContext context = (StandardContext) tomcat.addWebapp("", rootFolder.getAbsolutePath());
      WebResourceRoot resources = new StandardRoot(context);
      WebResourceSet resourceSet;
      if (additionWebInfClassesFolder.exists()) {
        resourceSet = new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClassesFolder.getAbsolutePath(), "/");
        System.out.println("loading WEB-INF resources from as '" + additionWebInfClassesFolder.getAbsolutePath() + "'");
      } else {
        resourceSet = new EmptyResourceSet(resources);
      }
      resources.addPreResources(resourceSet);
      context.setResources(resources);
//      context.setClearReferencesHttpClientKeepAliveThread(false);
//      context.setClearReferencesStopThreads(false);
//      context.setClearReferencesStopTimerThreads(false);
//      context.setClearReferencesStopTimerThreads(false);
//      context.setAntiResourceLocking(false);
//      context.setReloadable(false);
//      context.setUseHttpOnly(true);
//      context.setTldValidation(false);
//      context.setXmlValidation(false);
//      context.setXmlNamespaceAware(false);
//      context.setUseNaming(false);
//      context.setDelegate(true);
//      context.setAllowCasualMultipartParsing(true);
//      context.setCookies(false);
      // disable JSP and WebSocket support
//      context.setContainerSciFilter("org.apache.tomcat.websocket.server.WsSci|org.apache.jasper.servlet.JasperInitializer");
    } catch (Exception e) {
      throw new IllegalStateException("Fail to configure webapp from " + rootFolder, e);
    }
  }
}
