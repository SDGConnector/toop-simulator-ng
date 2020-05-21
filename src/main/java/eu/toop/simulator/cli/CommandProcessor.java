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
package eu.toop.simulator.cli;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.io.stream.StreamHelper;
import eu.toop.simulator.ToopSimulatorMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

/**
 * Process the command line input and executes the related services.
 */
public class CommandProcessor {
  /**
   * Logger instance
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(CommandProcessor.class);

  /**
   * Help message displayed for command input
   */
  private static String helpMessage;

  /**
   * Process the send-dc-request command
   *
   * @param command the input command
   */
  public static void processSendRequestCommand(CliCommand command) {
    ValueEnforcer.notNull(command, "Empty command list");
    String mainCommand = command.getMainCommand();

    boolean hasFileOption = command.hasOption("f");

    //the -new and -f options are exclusive
    if (hasFileOption) {
      List<String> fileArgs = command.getArguments("f");
      ValueEnforcer.isEqual(fileArgs.size(), 1, "-f option needs exactly one argument");
      sendDCRequest(fileArgs.get(0));
    } else {
      //TODO send the default request.
    }
  }

  /**
   * Print help message.
   */
  public static void printHelpMessage() {
    if (helpMessage == null) {
      //we are single threaded so no worries
      try (InputStream is = ToopSimulatorMain.class.getResourceAsStream("/cli-help.txt")) {
        helpMessage = new String(StreamHelper.getAllBytes(is));
      } catch (Exception ex) {
        helpMessage = "Couldn't load help message";
      }
    }
    System.out.println(helpMessage);
  }


  private static void sendDCRequest(String s) {

  }


}
