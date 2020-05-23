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
import com.typesafe.config.impl.ConfigImpl;
import eu.toop.simulator.cli.Cli;
import org.eclipse.jetty.server.Server;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * The program entry point
 *
 * @author muhammet yildiz
 */
public class ToopSimulatorMain {

  /**
   * program entry point
   */
  public static void main(String[] args) throws Exception {

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
            .setContainerIncludeJarPattern(JettyStarter.CONTAINER_INCLUDE_JAR_PATTERN_ALL);
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
    //TCAPIConfig.setDDServiceGroupHrefProvider(DiscoveryProvider.getInstance());
    //TCAPIConfig.setDDServiceMetadataProvider(DiscoveryProvider.getInstance());
    //TCAPIConfig.setDSDPartyIDIdentifier(DiscoveryProvider.getInstance());
  }

}
