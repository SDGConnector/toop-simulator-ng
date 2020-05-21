/**
 * Copyright (C) 2018-2020 toop.eu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.toop.simulator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class manages and serves the paths for resources that are being used by the simulator.
 *
 * @author yerlibilgin
 */
public class ToopSimulatorResources {

  /**
   * The root directory for the resources
   */
    public static final String SIMULATOR_CONFIG_DIR = resolveFromEnvOrSystem("SIMULATOR_CONFIG_DIR", "./config");

  /**
   * The path for disovery-data.xml
   */
  public static final String CONFIG_DISCOVERY_DATA_XML = SIMULATOR_CONFIG_DIR + "discovery-data.xml";

  /**
   * The path for toop-simulator.conf
   */
  public static final String CONFIG_TOOP_SIMULATOR_CONF = SIMULATOR_CONFIG_DIR + "toop-simulator.conf";
  private static final String DEFAULT_CONFIG_RESOURCE = "/config/toop-simulator.conf";
  private static final String DEFAULT_DISCOVERY_DATA_RESOURCE = "/config/discovery-data.xml";


  /**
   * Try to get the value of the given <code>key</code> from ENV. If it doesn't exit, then System properties are
   * tried. Finally the <code>defaultValue</code> is returned if System property also does not exist.
   *
   * @param key          the key to be resolved
   * @param defaultValue the default value when the key is not found in the ENV or System properties.
   * @return the resolved value
   */
  private static String resolveFromEnvOrSystem(String key, String defaultValue) {
    String value = System.getenv(key);
    if (value == null) {
      value = System.getProperty(key, defaultValue);
    }

    //make sure that the path has a "/" at the end
    if (!value.endsWith("/"))
      value += "/";

    return value;
  }


  /**
   * Returns the <code>toop-simulator.conf</code> resource URL
   * @return the <code>toop-simulator.conf</code> resource URL
   */
  public static URL getSimulatorConfResource() {
    String fileCandidate = CONFIG_TOOP_SIMULATOR_CONF;
    String cpResourceCandidate = DEFAULT_CONFIG_RESOURCE;

    return getUrlForEither(fileCandidate, cpResourceCandidate);
  }



  /**
   * Returns the <code>discovery-data.xml</code> resource URL
   * @return the <code>discovery-data.xml</code> resource URL
   */
  public static URL getDiscoveryDataResURL() {
    String fileCandidate = CONFIG_DISCOVERY_DATA_XML;
    String cpResourceCandidate = DEFAULT_DISCOVERY_DATA_RESOURCE;

    return getUrlForEither(fileCandidate, cpResourceCandidate);
  }

  /**
   * Check if the file specified with <code>fileCandidate</code> exists, and returns its URL,
   * if the file doesn't exist, then the classpath resource <code>cpResourceCandidate</code> is loaded
   * otherwise go for classpath resource
   *
   * @param fileCandidate the file that if exists will be tried first
   * @param cpResourceCandidate if the <code>fileCandidate</code> doesn't exist then this classpath resources URL is returned
   * @return the URL of one of the parameters
   */
  private static URL getUrlForEither(String fileCandidate, String cpResourceCandidate) {
    File confFile = new File(fileCandidate);

    if (confFile.exists()){
      try {
        return confFile.toURI().toURL();
      } catch (MalformedURLException e) {
        throw new IllegalStateException(); //not possible
      }
    }

    return ToopSimulatorResources.class.getResource(cpResourceCandidate);
  }



  /**
   * Copy the toop-simulator.conf and discovery-data.xml from classpath
   * to the current directory, so that the user can edit them without
   * dealing with the jar file. <br>
   * Don't touch if they exist
   */
  public static void transferResourcesToFileSystem() {
    Util.transferResourceToDirectory(DEFAULT_DISCOVERY_DATA_RESOURCE, SIMULATOR_CONFIG_DIR);
    Util.transferResourceToDirectory(DEFAULT_CONFIG_RESOURCE, SIMULATOR_CONFIG_DIR);

    new File("data").mkdir();

    Util.transferResourceToDirectory("toop-request.xml", "data");
  }
}
