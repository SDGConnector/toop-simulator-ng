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
package eu.toop.simulator.cli;

import eu.toop.simulator.SimulationMode;
import eu.toop.simulator.SimulatorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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
              CommandProcessor.processCommand(command);
            } else {
              System.out.println("Ignoring command in nonDC mode");
            }
            break;
          }


          case SimulatorCliHelper.CMD_SEND_DP_RESPONSE: {
            if (SimulatorConfig.getMode() == SimulationMode.DP) {
              CommandProcessor.processCommand(command);
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
