/**
 * This work is protected under copyrights held by the members of the
 * TOOP Project Consortium as indicated at
 * http://wiki.ds.unipi.gr/display/TOOP/Contributors
 * (c) 2018-2021. All rights reserved.
 *
 * This work is dual licensed under Apache License, Version 2.0
 * and the EUPL 1.2.
 *
 *  = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
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
