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
   * One of three modes, DP DC or SOLE
   */
  private static SimulationMode mode;

  private static int connectorPort;

  /**
   * A variable to defined whether the gateway will be mocked or not
   */
  private static boolean mockGateway;

  /**
   * The endpoint of the sender
   */
  private static String dcEndpoint;

  /**
   * The endpoint URL the the DP
   */
  private static String dpEndpoint;

  /**
   * The scheme component of the sender participant id.
   * <br> The scheme used in TOOP is iso6523-actorid-upis
   */
  private static String senderScheme;

  /**
   * The sender of the request
   */
  private static String sender;

  /**
   * The recipient of the toop request.
   */
  private static String receiver;

  /**
   * The scheme component of the receiver participant id.
   * <br> The scheme used in TOOP is iso6523-actorid-upis
   */
  private static String receiverScheme;
  /**
   * Determines whether the simulator will generate responses automatically
   * when in DP mode
   */
  private static boolean dpResponseAuto = true;
  private static String gatewayEndpoint = "http://gw-freedonia.dev.exchange.toop.eu:9082/holodeckb2b/as4";

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
    senderScheme = conf.getString("toop-simulator.senderScheme");
    sender = conf.getString("toop-simulator.sender");

    receiverScheme = conf.getString("toop-simulator.receiverScheme");
    receiver = conf.getString("toop-simulator.receiver");

    dpResponseAuto = conf.getBoolean("toop-simulator.dpResponseAuto");

    mockGateway = conf.getBoolean("toop-simulator.MEM.mockGateway");
    gatewayEndpoint = conf.getString("toop-simulator.MEM.gatewayEndpoint");

    LOGGER.debug("mode: " + mode);
    LOGGER.debug("dcEndpoint: " + dcEndpoint);
    LOGGER.debug("dpEndpoint: " + dpEndpoint);
    LOGGER.debug("sender: " + sender);
    LOGGER.debug("receiver: " + receiver);
    LOGGER.debug("dpResponseAuto: " + dpResponseAuto);
    LOGGER.debug("connectorPort: " + connectorPort);
    LOGGER.debug("mockGateway: " + mockGateway);
  }

  /**
   * Simulation mode.
   *
   * @return the mode
   */
  public static SimulationMode getMode() {
    return mode;
  }

  /**
   * The port that the connector HTTP server will be published on
   *
   * @return the connector port
   */
  public static int getConnectorPort() {
    return connectorPort;
  }

  /**
   * A flag that indicates whether the gateway communication should be mocked (<code>true</code>) or not (<code>false</code>)
   *
   * @return the boolean
   */
  public static boolean isMockGateway() {
    return mockGateway;
  }

  /**
   * The /to-dc endpoint URL of the DC (used only when not in DC mode, i.e. DC is not being simulated)
   *
   * @return the dc endpoint
   */
  public static String getDcEndpoint() {
    return dcEndpoint;
  }

  /**
   * The /to-dp endpoint URL of the DP (used only when not in DP mode, i.e. DP is not being simulated)
   *
   * @return the dp endpoint
   */
  public static String getDpEndpoint() {
    return dpEndpoint;
  }

  /**
   * The ID of this instance as DC or DP
   *
   * @return the sender
   */
  public static String getSender() {
    return sender;
  }

  /**
   * The ID of the receiver (i.e. the other side) as DC and DP
   *
   * @return the receiver
   */
  public static String getReceiver() {
    return receiver;
  }

  /**
   * Sets mode.
   *
   * @param mode the mode
   */
  public static void setMode(SimulationMode mode) {
    SimulatorConfig.mode = mode;
  }

  /**
   * Sets connector port.
   *
   * @param connectorPort the connector port
   */
  public static void setConnectorPort(int connectorPort) {
    SimulatorConfig.connectorPort = connectorPort;
  }

  /**
   * Sets mock gateway.
   *
   * @param mockGateway the mock gateway
   */
  public static void setMockGateway(boolean mockGateway) {
    SimulatorConfig.mockGateway = mockGateway;
  }

  /**
   * Sets dc endpoint.
   *
   * @param dcEndpoint the dc endpoint
   */
  public static void setDcEndpoint(String dcEndpoint) {
    SimulatorConfig.dcEndpoint = dcEndpoint;
  }

  /**
   * Sets dp endpoint.
   *
   * @param dpEndpoint the dp endpoint
   */
  public static void setDpEndpoint(String dpEndpoint) {
    SimulatorConfig.dpEndpoint = dpEndpoint;
  }

  /**
   * Sets sender.
   *
   * @param sender the sender
   */
  public static void setSender(String sender) {
    SimulatorConfig.sender = sender;
  }

  /**
   * Sets receiver.
   *
   * @param receiver the receiver
   */
  public static void setReceiver(String receiver) {
    SimulatorConfig.receiver = receiver;
  }

  /**
   * Is dp response auto boolean.
   *
   * @return the boolean
   */
  public static boolean isDpResponseAuto() {
    return dpResponseAuto;
  }

  /**
   * Sets dp response auto.
   *
   * @param dpResponseAuto the dp response auto
   */
  public static void setDpResponseAuto(boolean dpResponseAuto) {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Updating the value of DP response auto to " + dpResponseAuto);

    SimulatorConfig.dpResponseAuto = dpResponseAuto;
  }

  /**
   * Gets sender scheme.
   *
   * @return the sender scheme
   */
  public static String getSenderScheme() {
    return senderScheme;
  }

  /**
   * Sets sender scheme.
   *
   * @param senderScheme the sender scheme
   */
  public static void setSenderScheme(String senderScheme) {
    SimulatorConfig.senderScheme = senderScheme;
  }

  /**
   * Gets receiver scheme.
   *
   * @return the receiver scheme
   */
  public static String getReceiverScheme() {
    return receiverScheme;
  }

  /**
   * Sets receiver scheme.
   *
   * @param receiverScheme the receiver scheme
   */
  public static void setReceiverScheme(String receiverScheme) {
    SimulatorConfig.receiverScheme = receiverScheme;
  }

  public static String getGatewayEndpoint() {
    return gatewayEndpoint;
  }
}
