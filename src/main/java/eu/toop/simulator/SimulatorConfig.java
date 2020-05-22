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

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The utility class for reading the toop-simulator.conf file/classpath resource.
 * <p>
 * If a file with name "toop-simulator.conf" exists in the current directory, then it
 * is read, otherwise, the classpath is checked for /toop-sumlator.conf and if it exists
 * its read.
 * <p>
 * If none of the above paths are valid, then an Exception is thrown.
 *
 * @author yerlibilgin
 */
public class SimulatorConfig {
  /**
   * Logger instance
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SimulatorConfig.class);

  /**
   * Simulation mode.
   */
  public static final SimulationMode mode;

  /**
   * The port that the connector HTTP server will be published on
   */
  public static final int connectorPort;



  /**
   * A flag that indicates whether the gateway communication should be mocked (<code>true</code>) or not (<code>false</code>)
   */
  public static final boolean mockGateway;

  /**
   * The /to-dc endpoint URL of the DC (used only when not in DC mode, i.e. DC is not being simulated)
   */
  public static final String dcEndpoint;

  /**
   * The /to-dp endpoint URL of the DP (used only when not in DP mode, i.e. DP is not being simulated)
   */
  public static final String dpEndpoint;

  static {
    Config conf = Util.resolveConfiguration(ToopSimulatorResources.getSimulatorConfResource(), true);

    try {
      mode = SimulationMode.valueOf(conf.getString("toop-simulator.mode"));
    } catch (IllegalArgumentException ex) {
      LOGGER.error(ex.getMessage(), ex);
      throw ex;
    }
    connectorPort = conf.getInt("toop-simulator.connectorPort");
    dcEndpoint = conf.getString("toop-simulator.dcEndpoint");
    dpEndpoint = conf.getString("toop-simulator.dpEndpoint");

    mockGateway = conf.getBoolean("toop-simulator.mockGateway");

    LOGGER.debug("mode: " + mode);
    LOGGER.debug("dcEndpoint: " + dcEndpoint);
    LOGGER.debug("dpEndpoint: " + dpEndpoint);
    LOGGER.debug("connectorPort: " + connectorPort);
    LOGGER.debug("mockGateway: " + mockGateway);
  }
}
