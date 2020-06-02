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

import com.helger.photon.jetty.JettyStarter;
import eu.toop.connector.api.TCConfig;
import eu.toop.connector.webapi.TCAPIConfig;
import eu.toop.simulator.cli.Cli;
import eu.toop.simulator.mock.DiscoveryProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.JarFileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.URLResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * The program entry point
 *
 * @author muhammet yildiz
 */
public class ToopSimulatorMain {

  private static final Logger LOGGER = LoggerFactory.getLogger(ToopSimulatorMain.class);
  /**
   * program entry point
   */
  public static void main(String[] args) throws Exception {

    LOGGER.info("Starting TOOP Infrastructure simulator NG");
    ToopSimulatorResources.transferResourcesToFileSystem();
    prepareMocks();


    final SimulationMode simulationMode = SimulatorConfig.mode;

    //Start the simulator in a new thread, and get its thread so that we can wait on it.
    Thread simulatorThread = startSimulator(simulationMode);

    //now prepare and run commander if we are not in SOLE mode
    //if (simulationMode == SimulationMode.DC) {
      Cli.startCli();
    //}


    //wait for the simulator thread to exit
    simulatorThread.join();
  }

  private static Thread startSimulator(SimulationMode simulationMode) throws InterruptedException {
    final Object serverLock = new Object();

    //start jetty
    Thread simulatorThread = runJetty(serverLock, SimulatorConfig.connectorPort);

    synchronized (serverLock) {
      //wait for the server to come up
      serverLock.wait();
    }

    return simulatorThread;
  }

  /**
   * Start simulator server
   * @param serverLock used to notify all the threads waiting on this lock to wake up
   *                   after server start.
   * @param httpPort the port to publish the jetty on
   * @return
   */
  private static Thread runJetty(final Object serverLock, final int httpPort) {

    Thread simulatorThread = new Thread(() -> {
      try {
        final JettyStarter js = new JettyStarter(ToopSimulatorMain.class) {
          @Override
          protected void onServerStarted(@Nonnull Server aServer) {
            synchronized (serverLock) {
              serverLock.notify();
            }
          }
        }.setPort(httpPort)
            .setStopPort(httpPort + 100)
            .setSessionCookieName("TOOP_TS_SESSION")
            .setWebXmlResource(ToopSimulatorMain.class.getClassLoader().getResource("WEB-INF/web.xml").toString())
            .setContainerIncludeJarPattern(JettyStarter.CONTAINER_INCLUDE_JAR_PATTERN_ALL)
            .setWebInfIncludeJarPattern(JettyStarter.CONTAINER_INCLUDE_JAR_PATTERN_ALL)
            .setAllowAnnotationBasedConfig(true);


        LOGGER.info("JETTY RESOURCE BASE " + js.getResourceBase());
        LOGGER.info("JETTY WEBXML RES BASE " + js.getWebXmlResource());
        js.run();

      } catch (Exception ex) {
        throw new IllegalStateException(ex.getMessage(), ex);
      }
    });

    //start the simulator
    simulatorThread.start();
    return simulatorThread;
  }

  /**
   * Prepare three simulators (Directory, SMP and SMS) here
   *
   * @throws Exception
   */
  private static void prepareMocks() {
    TCAPIConfig.setDDServiceGroupHrefProvider(DiscoveryProvider.getInstance());
    TCAPIConfig.setDDServiceMetadataProvider(DiscoveryProvider.getInstance());
    TCAPIConfig.setDSDDatasetResponseProvider(DiscoveryProvider.getInstance());
  }

}
