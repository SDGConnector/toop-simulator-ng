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
package eu.toop.simulator.mock;

import java.io.IOException;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A MOCK class that generates and sends DC requests
 *
 * @author yerlibilgin
 */
public class MockDC {

  private static final Logger LOGGER = LoggerFactory.getLogger(MockDC.class);

  /**
   * Sends a request that is contained in a file with name <code>sFileName</code>
   *
   * @param sender    the identifier of the sender. Optional
   * @param receiver  the identifier of the recevier. Optional
   * @param docTypeId The doctypeid. Optional
   * @param sFileName the file that contains the request. Optional
   */
  public static void sendDCRequest(@Nullable String sender, @Nullable String receiver, @Nullable String docTypeId, @Nullable String sFileName) {

    final String defaultResourceName = "/datasets/edm-conceptRequest-lp.xml";
    final String connectorEndpoint = "/api/user/submit/request";

    DCDPUtil.sendTCOutgoingMessage(sender, receiver, docTypeId, sFileName, defaultResourceName, connectorEndpoint);
  }

  public static void main(String[] args) throws IOException {
    //buildAndSendDefaultRequest(sender, receiver, docType);
  }
}
