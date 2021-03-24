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
package eu.toop.simulator.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.stream.StreamHelper;
import com.helger.peppolid.simple.doctype.SimpleDocumentTypeIdentifier;
import com.helger.peppolid.simple.participant.SimpleParticipantIdentifier;
import com.helger.peppolid.simple.process.SimpleProcessIdentifier;

import eu.toop.connector.api.me.incoming.IIncomingEDMResponse;
import eu.toop.connector.api.me.incoming.MEIncomingTransportMetadata;
import eu.toop.connector.api.rest.TCIdentifierType;
import eu.toop.connector.api.rest.TCIncomingMessage;
import eu.toop.connector.api.rest.TCIncomingMetadata;
import eu.toop.connector.api.rest.TCRestJAXB;
import eu.toop.edm.EDMRequest;
import eu.toop.simulator.SimulatorConfig;
import eu.toop.simulator.mock.MockDP;

@WebServlet("/to-dp")
public class MockDPServlet extends HttpServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger(MockDPServlet.class);

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    byte[] bytes = StreamHelper.getAllBytes(req.getInputStream());

    LOGGER.info("DP Servlet got message:");
    final String sIncomingMessage = new String(bytes, StandardCharsets.UTF_8);
    LOGGER.info(sIncomingMessage);
    resp.setStatus(HttpServletResponse.SC_OK);

    if(SimulatorConfig.isDpResponseAuto()) {
      LOGGER.debug("Automatic response will be created and sent");
      new Thread(() -> {
        final TCIncomingMessage tcIncomingMessage = TCRestJAXB.incomingMessage().read(sIncomingMessage);
        final TCIncomingMetadata metadata = tcIncomingMessage.getMetadata();

        LOGGER.info("DP Received Metadata: " + metadata);
        tcIncomingMessage.getPayload().forEach(tcPayload -> {
          LOGGER.info("DP Received Payload  Content ID: " + tcPayload.getContentID());
          LOGGER.info("DP Received Payload  Mime Type: " + tcPayload.getMimeType());

          final EDMRequest edmRequest = EDMRequest.reader().read(tcPayload.getValue());
          LOGGER.info("DP Received Payload:\n" + edmRequest.getWriter().getAsString());

          final TCIdentifierType receiverID = metadata.getReceiverID();
          final TCIdentifierType senderID = metadata.getSenderID();
          final TCIdentifierType docTypeID = metadata.getDocTypeID();
          final TCIdentifierType processID = metadata.getProcessID();
          MEIncomingTransportMetadata meIncomingTransportMetadata = new MEIncomingTransportMetadata(
              new SimpleParticipantIdentifier(senderID.getScheme(), senderID.getValue()),
              new SimpleParticipantIdentifier(receiverID.getScheme(), receiverID.getValue()),
              new SimpleDocumentTypeIdentifier(docTypeID.getScheme(), docTypeID.getValue()),
              new SimpleProcessIdentifier(processID.getScheme(), processID.getValue())
          );

          final IIncomingEDMResponse response = MockDP.eloniaCreateResponse(edmRequest, meIncomingTransportMetadata);
          final String sDestURL = "http://localhost:" + SimulatorConfig.getConnectorPort() + "/api/user/submit/response";
          LOGGER.info("MOCKDPServlet sending back response to " + sDestURL);
          MockDP.buildAndSendResponse(response);
        });
      }).start();
    } else {
      LOGGER.debug("Automatic response is disabled. Having a rest");
    }
  }
}
