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

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.io.stream.StreamHelper;

import eu.toop.simulator.SimulatorConfig;
import eu.toop.simulator.ToopSimulatorMain;
import eu.toop.simulator.mock.MockDC;
import eu.toop.simulator.mock.MockDP;

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
      "RegisteredOrganization::REGISTERED_ORGANIZATION_TYPE::CONCEPT##CCCEV::toop-edm:v2.1",
      "FinancialRatioDocument::FINANCIAL_RECORD_TYPE::UNSTRUCTURED::toop-edm:v2.1",
      "urn:eu:toop:ns:dataexchange-1p40::Request##urn:eu.toop.request.crewcertificate-list::1.40",
      "urn:eu:toop:ns:dataexchange-1p40::Request##urn:eu.toop.request.crewcertificate::1.40",
      "urn:eu:toop:ns:dataexchange-1p40::Request##urn:eu.toop.request.registeredorganization::1.40",
      "urn:eu:toop:ns:dataexchange-1p40::Request##urn:eu.toop.request.shipcertificate-list::1.40",
      "urn:eu:toop:ns:dataexchange-1p40::Request##urn:eu.toop.request.shipcertificate::1.40",
  };


  private static final String dpPredefinedDoctypes[] = new String[]{
      "RegisteredOrganization::REGISTERED_ORGANIZATION_TYPE::CONCEPT##CCCEV::toop-edm:v2.1",
      "FinancialRatioDocument::FINANCIAL_RECORD_TYPE::UNSTRUCTURED::toop-edm:v2.1",
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

    String docType = "RegisteredOrganization::REGISTERED_ORGANIZATION_TYPE::CONCEPT##CCCEV::toop-edm:v2.1";

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

    String docType = "QueryResponse::toop-edm:v2.1";

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
