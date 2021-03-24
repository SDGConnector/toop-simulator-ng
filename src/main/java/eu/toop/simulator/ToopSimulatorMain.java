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

import javax.annotation.Nonnull;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.photon.jetty.JettyStarter;

import eu.toop.connector.app.api.TCAPIConfig;
import eu.toop.simulator.cli.Cli;
import eu.toop.simulator.mock.DiscoveryProvider;

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


    final SimulationMode simulationMode = SimulatorConfig.getMode();

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
    Thread simulatorThread = runJetty(serverLock, SimulatorConfig.getConnectorPort());

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
    //if gateway is not simulated, then check the gateway endpoint to set
    if (!SimulatorConfig.isMockGateway()){
      System.setProperty("toop.mem.as4.endpoint", SimulatorConfig.getGatewayEndpoint());
    }

    TCAPIConfig.setDDServiceGroupHrefProvider(DiscoveryProvider.getInstance());
    TCAPIConfig.setDDServiceMetadataProvider(DiscoveryProvider.getInstance());
    TCAPIConfig.setDSDDatasetResponseProvider(DiscoveryProvider.getInstance());
  }

}
