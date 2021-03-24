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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.stream.StreamHelper;

import eu.toop.connector.api.rest.TCIncomingMessage;
import eu.toop.connector.api.rest.TCIncomingMetadata;
import eu.toop.connector.api.rest.TCRestJAXB;
import eu.toop.edm.EDMErrorResponse;
import eu.toop.edm.EDMResponse;
import eu.toop.edm.IEDMTopLevelObject;
import eu.toop.edm.xml.EDMPayloadDeterminator;

@WebServlet("/to-dc")
public class MockDCServlet extends HttpServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger(MockDCServlet.class);

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    byte []bytes = StreamHelper.getAllBytes(req.getInputStream());

    LOGGER.info("DC Servlet got message:");
    final String sIncomingMessage = new String(bytes, StandardCharsets.UTF_8);
    LOGGER.info(sIncomingMessage);

    final TCIncomingMessage tcIncomingMessage = TCRestJAXB.incomingMessage().read(sIncomingMessage);
    final TCIncomingMetadata metadata = tcIncomingMessage.getMetadata();

    LOGGER.info("DC Received Metadata: " + metadata);
    tcIncomingMessage.getPayload().forEach(tcPayload -> {
      LOGGER.info("DC Received Payload  Content ID: " + tcPayload.getContentID());
      LOGGER.info("DC Received Payload  Mime Type: " + tcPayload.getMimeType());
      InputStream bis = new ByteArrayInputStream(tcPayload.getValue());
      IEDMTopLevelObject aTLO = EDMPayloadDeterminator.parseAndFind(bis);

      if (aTLO != null && aTLO instanceof EDMResponse) {
        EDMResponse edmResponse = (EDMResponse) aTLO;

        LOGGER.debug("DC received EDMResponse payload:\n {}", edmResponse.getWriter().getAsString());
      } else if (aTLO != null && aTLO instanceof EDMErrorResponse) {
        EDMErrorResponse edmErrorResponse = (EDMErrorResponse) aTLO;

        LOGGER.debug("DC received EDMErrorResponse payload:\n {}", edmErrorResponse.getWriter().getAsString());

      } else {
        LOGGER.debug("DC unable to parse supplied response.");
      }
    });
    resp.setStatus(HttpServletResponse.SC_OK);
  }
}
