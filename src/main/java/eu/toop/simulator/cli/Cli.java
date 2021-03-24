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
package eu.toop.simulator.cli;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.toop.simulator.SimulationMode;
import eu.toop.simulator.SimulatorConfig;

/**
 * @author yerlibilgin
 */
public class Cli {
  /**
   * The Logger instance
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(Cli.class);


  /**
   * Start the clie
   *
   * @throws Exception
   */
  public static void startCli() throws Exception {
    //check if the logs directory exists, otherwise create it (used for saving the last_response.xml
    new File("logs").mkdirs();
    LOGGER.info("Entering CLI mode");
    SimulatorCliHelper simulatorCliHelper = new SimulatorCliHelper();
    while (simulatorCliHelper.readLine()) {
      try {

        CliCommand command = CliCommand.parse(simulatorCliHelper.getWords(), true);

        switch (command.getMainCommand()) {
          case SimulatorCliHelper.CMD_SEND_DC_REQUEST: {
            if (SimulatorConfig.getMode() == SimulationMode.DC) {
              CommandProcessor.processSendDCRequest(command);
            } else {
              System.out.println("Ignoring command in nonDC mode");
            }
            break;
          }


          case SimulatorCliHelper.CMD_SEND_DP_RESPONSE: {
            if (SimulatorConfig.getMode() == SimulationMode.DP) {
              CommandProcessor.processSendDPResponse(command);
            } else {
              System.out.println("Ignoring command in nonDP mode");
            }
            break;
          }

          case SimulatorCliHelper.CMD_QUIT:
            System.exit(0);
            break;

          case SimulatorCliHelper.CMD_HELP:
          default:
            CommandProcessor.printHelpMessage();
            break;
        }
      } catch (NullPointerException ex) {
        LOGGER.error(ex.getMessage(), ex);
        break;
      } catch (Exception ex) {
        LOGGER.error(ex.getMessage(), ex);
      } finally {
        Thread.sleep(100);
      }
    }
  }
}
