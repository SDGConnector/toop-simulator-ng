/**
 * Copyright (C) 2018-2020 toop.eu
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.toop.simulator;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.io.stream.StreamHelper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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

    String resourceName = FilenameUtils.getName(path);

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

  /**
   *
   * BOGUS. Doesnt work with jars. TODO: Visit later
   * Transfer ALL the resources residing under the provided classpath resource to the
   * filesystem with the same directory structure.
   *
   * @param path
   */
  public static void transferAllResourcesToFileSystem(String path) throws IOException {
    LOGGER.debug("Inspect " + path);
    if (path.endsWith(".yaml") || path.endsWith(".xml")) {
      LOGGER.debug("Transfer " + path);

      String trimmedPath = path.substring(0, path.lastIndexOf('/'));
      if (trimmedPath.startsWith("/"))
        trimmedPath = trimmedPath.substring(1);

      Util.transferResourceToDirectory(path, trimmedPath);
      return;
    }

    final URL resource = Util.class.getResource(path);
    if (resource == null) {
      LOGGER.warn("NULL resource, NOOP");
      return;
    }

    final URLConnection urlConnection = resource.openConnection();
    if (urlConnection == null) {
      LOGGER.warn("Cannot open connection, NOOP");
      return;
    }

    final InputStream resourceAsStream = urlConnection.getInputStream();
    if (urlConnection == null) {
      LOGGER.warn("Cannot open stream, NOOP");
      return;
    }

    BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
    String line;
    while ((line = br.readLine()) != null) {
      Util.transferAllResourcesToFileSystem(path + "/" + line);
    }
  }
}
