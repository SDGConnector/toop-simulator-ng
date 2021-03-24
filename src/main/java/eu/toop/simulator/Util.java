/**
 * This work is protected under copyrights held by the members of the
 * TOOP Project Consortium as indicated at
 * http://wiki.ds.unipi.gr/display/TOOP/Contributors
 * (c) 2018-2021. All rights reserved.
 *
 * This work is licensed under the EUPL 1.2.
 *
 *  = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL
 * (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *         https://joinup.ec.europa.eu/software/page/eupl
 */
package eu.toop.simulator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.io.stream.StreamHelper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * A class that contains utility functions
 */
public class Util {
  /**
   * Logger instance
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

  /**
   * <p>Try to parse and resolve the given URL as a HOCON resource. If <code>includeSys == true</code> then
   * a System Properties are also added as fallback</p>
   *
   * @param url        the url of the config to be parsed
   * @param includeSys a flag to indicate whether to include the System.properties or not.
   * @return the parsed Config object
   */
  public static Config resolveConfiguration(URL url, boolean includeSys) {
    Config config;

    LOGGER.info("Loading config from the URL \"" + url);
    config = ConfigFactory.parseURL(url);

    if (includeSys) {
      config = config.withFallback(ConfigFactory.systemProperties());
    }

    return config.resolve();
  }

  /**
   * Transfer the classpath resource to the provided directory if it doesn't already exist there
   *
   * @param path the resource to be copied
   */
  public static void transferResourceToDirectory(String path, String targetDirName) {
    ValueEnforcer.notEmpty(path, "The resource path");
    ValueEnforcer.notEmpty(targetDirName, "The target directory");

    //the resource is already an absolute path. So remove a "/" if it exists
    if (path.startsWith("/"))
      path = path.substring(1);

    URL resource = Util.class.getClassLoader().getResource(path);

    if (resource == null)
      throw new IllegalArgumentException("Couldn't find the resource " + path);

    String resourceName = FilenameHelper.getWithoutPath(path);

    File targetDir = new File(targetDirName);

    targetDir.mkdirs(); //ignore the result

    File targetFile = new File(targetDir, resourceName);

    //if a file with the same name doesn't exist in the target dir, then create one and transfer the resource
    if (!targetFile.exists()) {
      //try to create the path
      targetDir.mkdirs();

      try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
           InputStream inputStream = resource.openStream()) {

        //copy the stream
        StreamHelper.copyInputStreamToOutputStream(inputStream, fileOutputStream);

      } catch (Exception ex) {
        LOGGER.error("Failed to copy resource " + path + " to the local directroy.", ex);
      }
    }
  }
}
