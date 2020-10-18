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

import com.helger.commons.ValueEnforcer;
import com.helger.commons.io.stream.StreamHelper;
import eu.toop.simulator.SimulationMode;
import eu.toop.simulator.SimulatorConfig;
import eu.toop.simulator.ToopSimulatorMain;
import eu.toop.simulator.mock.MockDC;
import eu.toop.simulator.mock.MockDP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

  private static final String dcPredefinedDoctypes[] = new String[]{
      "RegisteredOrganization::REGISTERED_ORGANIZATION_TYPE::CONCEPT##CCCEV::toop-edm:v2.0",
      "FinancialRatioDocument::FINANCIAL_RECORD_TYPE::UNSTRUCTURED::toop-edm:v2.0",
      "urn:eu:toop:ns:dataexchange-1p40::Request##urn:eu.toop.request.crewcertificate-list::1.40",
      "urn:eu:toop:ns:dataexchange-1p40::Request##urn:eu.toop.request.crewcertificate::1.40",
      "urn:eu:toop:ns:dataexchange-1p40::Request##urn:eu.toop.request.registeredorganization::1.40",
      "urn:eu:toop:ns:dataexchange-1p40::Request##urn:eu.toop.request.shipcertificate-list::1.40",
      "urn:eu:toop:ns:dataexchange-1p40::Request##urn:eu.toop.request.shipcertificate::1.40",
  };


  private static final String dpPredefinedDoctypes[] = new String[]{
      "RegisteredOrganization::REGISTERED_ORGANIZATION_TYPE::CONCEPT##CCCEV::toop-edm:v2.0",
      "FinancialRatioDocument::FINANCIAL_RECORD_TYPE::UNSTRUCTURED::toop-edm:v2.0",
      "urn:eu:toop:ns:dataexchange-1p40::Response##urn:eu.toop.response.crewcertificate-list::1.40",
      "urn:eu:toop:ns:dataexchange-1p40::Response##urn:eu.toop.response.crewcertificate::1.40",
      "urn:eu:toop:ns:dataexchange-1p40::Response##urn:eu.toop.response.registeredorganization::1.40",
      "urn:eu:toop:ns:dataexchange-1p40::Response##urn:eu.toop.response.shipcertificate-list::1.40",
      "urn:eu:toop:ns:dataexchange-1p40::Response##urn:eu.toop.response.shipcertificate::1.40",
      "urn:eu:toop:ns:dataexchange-1p40::Response##urn:eu.toop.response.evidence::1.40",
  };

  /**
   * Process the send-dc-request command
   *
   * @param command the input command
   */
  public static void processSendDCRequest(CliCommand command) {
    ValueEnforcer.notNull(command, "Empty command list");

    String[] predefinedTypes = CommandProcessor.dcPredefinedDoctypes;

    //[-f edm response] [-s sender] [-r receiver] [-d doctype | -pd predefinedDocType]

    String sender = SimulatorConfig.getSender();
    if (command.hasOption("s")) {
      sender = command.getOption("s").get(0);
    }
    String receiver = SimulatorConfig.getReceiver();
    if (command.hasOption("r")) {
      receiver = command.getOption("r").get(0);
    }

    String docType = "RegisteredOrganization::REGISTERED_ORGANIZATION_TYPE::CONCEPT##CCCEV::toop-edm:v2.0";

    if (command.hasOption("d")) {
      docType = command.getOption("d").get(0);
    } else {
      if (command.hasOption("pd")) {
        int index = Integer.parseInt(command.getOption("pd").get(0));
        index = index - 1; //counting starts from 1.
        if (index < 0 || index >= predefinedTypes.length) {
          throw new IllegalArgumentException("invalid predefined doctype index");
        }

        docType = predefinedTypes[index];
      }
    }

    LOGGER.debug("Creating a message as " + sender + " --> " + receiver + " ");
    LOGGER.debug(" and doctype " + docType);

    String file = null;

    if (command.hasOption("f")) {
      List<String> fileArgs = command.getOption("f");
      ValueEnforcer.isEqual(fileArgs.size(), 1, "-f option needs exactly one argument");
      file = fileArgs.get(0);
    }

    MockDC.sendDCRequest(sender, receiver, docType, file);
  }



  /**
   * Process the send-dc-request command
   *
   * @param command the input command
   */
  public static void processSendDPResponse(CliCommand command) {
    ValueEnforcer.notNull(command, "Empty command list");

    String[] predefinedTypes = CommandProcessor.dpPredefinedDoctypes;

    //[-f edm response] [-s sender] [-r receiver] [-d doctype | -pd predefinedDocType]

    String sender = SimulatorConfig.getSender();
    if (command.hasOption("s")) {
      sender = command.getOption("s").get(0);
    }
    String receiver = SimulatorConfig.getReceiver();
    if (command.hasOption("r")) {
      receiver = command.getOption("r").get(0);
    }

    String docType = "QueryResponse::toop-edm:v2.0";

    if (command.hasOption("d")) {
      docType = command.getOption("d").get(0);
    } else {
      if (command.hasOption("pd")) {
        int index = Integer.parseInt(command.getOption("pd").get(0));
        index = index - 1; //counting starts from 1.
        if (index < 0 || index >= predefinedTypes.length) {
          throw new IllegalArgumentException("invalid predefined doctype index");
        }

        docType = predefinedTypes[index];
      }
    }

    LOGGER.debug("Creating a message as " + sender + " --> " + receiver + " ");
    LOGGER.debug(" and doctype " + docType);

    String file = null;

    if (command.hasOption("f")) {
      List<String> fileArgs = command.getOption("f");
      ValueEnforcer.isEqual(fileArgs.size(), 1, "-f option needs exactly one argument");
      file = fileArgs.get(0);
    }

    MockDP.sendDPResponse(sender, receiver, docType, file);
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

}
