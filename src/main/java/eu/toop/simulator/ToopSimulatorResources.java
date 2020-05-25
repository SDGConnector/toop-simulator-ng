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

  private static final String DEFAULT_CONFIG_RESOURCE = "/toop-simulator.conf";
  private static final String DEFAULT_DISCOVERY_DATA_RESOURCE = "/discovery-data.xml";

  /**
   * Returns the <code>toop-simulator.conf</code> resource URL
   * @return the <code>toop-simulator.conf</code> resource URL
   */
  public static URL getSimulatorConfResource() {
    return ToopSimulatorResources.class.getResource(DEFAULT_CONFIG_RESOURCE);
  }

  /**
   * Returns the <code>discovery-data.xml</code> resource URL
   * @return the <code>discovery-data.xml</code> resource URL
   */
  public static URL getDiscoveryDataResURL() {
    return ToopSimulatorResources.class.getResource(DEFAULT_DISCOVERY_DATA_RESOURCE);
  }

  /**
   * Copy the toop-simulator.conf and discovery-data.xml from classpath
   * to the current directory, so that the user can edit them without
   * dealing with the jar file. <br>
   * Don't touch if they exist
   */
  public static void transferResourcesToFileSystem() {
    new File("data").mkdir();
    Util.transferResourceToDirectory("toop-request.xml", "data");
    Util.transferAllResourcesToFileSystem("datasets");
  }
}
