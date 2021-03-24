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
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.mime.CMimeType;
import com.helger.commons.mime.MimeTypeDeterminator;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ResponseHandlerJson;

import eu.toop.connector.api.me.EMEProtocol;
import eu.toop.connector.api.me.incoming.IIncomingEDMResponse;
import eu.toop.connector.api.me.incoming.IncomingEDMErrorResponse;
import eu.toop.connector.api.me.incoming.IncomingEDMRequest;
import eu.toop.connector.api.me.incoming.IncomingEDMResponse;
import eu.toop.connector.api.me.incoming.MEIncomingTransportMetadata;
import eu.toop.connector.api.me.model.MEPayload;
import eu.toop.connector.api.rest.TCOutgoingMessage;
import eu.toop.connector.api.rest.TCOutgoingMetadata;
import eu.toop.connector.api.rest.TCPayload;
import eu.toop.connector.api.rest.TCRestJAXB;
import eu.toop.connector.app.incoming.DC_DP_TriggerViaHttp;
import eu.toop.edm.EDMErrorResponse;
import eu.toop.edm.EDMRequest;
import eu.toop.playground.dp.DPException;
import eu.toop.playground.dp.model.EDMResponseWithAttachment;
import eu.toop.playground.dp.service.ToopDP;
import eu.toop.simulator.SimulatorConfig;

/**
 * A MOCK class that generates and sends DP responses
 *
 * @author yerlibilgin
 */
public class MockDP {

  private static final Logger LOGGER = LoggerFactory.getLogger(MockDP.class);

  private static final ToopDP miniDP = new ToopDP();

  /**
   * Provide the message to miniDP and get back an {@link IIncomingEDMResponse}
   *
   * @param aTopLevel the request
   * @param aMetadata the metadata
   * @return the response
   */
  public static IIncomingEDMResponse eloniaCreateResponse(EDMRequest aTopLevel, MEIncomingTransportMetadata aMetadata) {

    //we need to create a new metadata where the sender and receiver are switched.
    final MEIncomingTransportMetadata aMetadataInverse = new MEIncomingTransportMetadata(
        aMetadata.getReceiverID(), aMetadata.getSenderID(),
        aMetadata.getDocumentTypeID(), aMetadata.getProcessID());

    try {
      //we have a response from DP, push it back
      EDMResponseWithAttachment edmResponse = miniDP.createEDMResponseWithAttachmentsFromRequest(aTopLevel);
      List<MEPayload> attachments = new ArrayList<>();
      if (!edmResponse.getAllAttachments().isEmpty()) {
        attachments =
                edmResponse.getAllAttachments().stream()
                        .map(
                                attachment -> {
                                  byte[] fileBytes = attachment.getAttachment ();

                                  return MEPayload.builder()
                                          .data(fileBytes)
                                          .mimeType(
                                                  MimeTypeDeterminator.getInstance()
                                                          .getMimeTypeFromBytes(fileBytes))
                                          .contentID(attachment.getAttachedFileCid())
                                          .build();
                                })
                        .collect(Collectors.toList());
      }
      return new IncomingEDMResponse(edmResponse.getEdmResponse(),"mock@toop",
          attachments,
          aMetadataInverse);

    } catch (DPException e) {
      EDMErrorResponse edmError = e.getEdmErrorResponse();
      //we have an error from DP, push it back

      return new IncomingEDMErrorResponse(edmError,"mock@toop",
          aMetadataInverse);
    }catch (IOException ex) {
      throw new UncheckedIOException (ex);
    }
  }


  /**
   * Send the request directly to the DP/to-dp
   *
   * @param edmRequest
   * @param aMetadata
   */
  public static void deliverRequestToDP(EDMRequest edmRequest, MEIncomingTransportMetadata aMetadata) {
    DC_DP_TriggerViaHttp.forwardMessage(new IncomingEDMRequest(edmRequest,"mock@toop",
        aMetadata), SimulatorConfig.getDpEndpoint());
  }

  /**
   * Sends a request that is contained in a file with name <code>sFileName</code>
   *
   * @param sender    the identifier of the sender. Optional
   * @param receiver  the identifier of the recevier. Optional
   * @param docTypeId The doctypeid. Optional
   * @param sFileName the file that contains the request. Optional
   */
  public static void sendDPResponse(@Nullable String sender, @Nullable String receiver, @Nullable String docTypeId,
                                   @Nullable String sFileName) {

    final String defaultResourceName = "/datasets/edm-conceptResponse-lp.xml";
    final String connectorEndpoint = "/api/user/submit/response";

    DCDPUtil.sendTCOutgoingMessage(sender, receiver, docTypeId, sFileName, defaultResourceName, connectorEndpoint);
  }


  /**
   * Build a TCOutgoingMessage from the response and send it to the connector
   *
   * @param response the response
   */
  public static void buildAndSendResponse(IIncomingEDMResponse response) {
    final TCOutgoingMessage aOM = new TCOutgoingMessage();
    {
      final TCOutgoingMetadata aMetadata = new TCOutgoingMetadata();
      aMetadata.setSenderID(TCRestJAXB.createTCID(response.getMetadata().getSenderID().getScheme(), response.getMetadata().getSenderID().getValue()));
      aMetadata.setReceiverID(TCRestJAXB.createTCID(response.getMetadata().getReceiverID().getScheme(), response.getMetadata().getReceiverID().getValue()));
      aMetadata.setDocTypeID(
              TCRestJAXB.createTCID("toop-doctypeid-qns", "QueryResponse::toop-edm:v2.1"));
      aMetadata.setProcessID(TCRestJAXB.createTCID(response.getMetadata().getProcessID().getScheme(), response.getMetadata().getProcessID().getValue()));

      aMetadata.setTransportProtocol(EMEProtocol.AS4.getTransportProfileID());
      aOM.setMetadata(aMetadata);
    }
    {

      final TCPayload aPayload = new TCPayload();
      final List<TCPayload> filePayloads = new ArrayList<>();
      byte[] payload = null;

      if (response instanceof IncomingEDMResponse) {
        payload = ((IncomingEDMResponse) response).getResponse().getWriter().getAsBytes();

        filePayloads.addAll(
                ((IncomingEDMResponse) response)
                        .attachments().values().stream()
                        .map(
                                m -> {
                                  TCPayload attachedFilePayload = new TCPayload();
                                  attachedFilePayload.setContentID(m.getContentID());
                                  attachedFilePayload.setMimeType(m.getMimeTypeString());
                                  attachedFilePayload.setValue(m.getData().bytes());
                                  return attachedFilePayload;
                                })
                        .collect(Collectors.toList()));

        aPayload.setContentID(
                ((IncomingEDMResponse) response).getResponse().getRequestID() + "@elonia-dev");
      }
      if (response instanceof IncomingEDMErrorResponse) {
        payload =
                ((IncomingEDMErrorResponse) response)
                        .getErrorResponse()
                        .getWriter()
                        .getAsBytes();
        aPayload.setContentID(((IncomingEDMErrorResponse) response).getErrorResponse().getRequestID()+"@elonia");

      }

      aPayload.setValue(payload);
      aPayload.setMimeType(CMimeType.APPLICATION_XML.getAsString());
      aOM.addPayload(aPayload);
      if(!filePayloads.isEmpty()){
        filePayloads.forEach(aOM::addPayload);
      }
    }

    LOGGER.info(TCRestJAXB.outgoingMessage().getAsString(aOM));

    try (HttpClientManager aHCM = new HttpClientManager()) {
      HttpPost post;
      if (response instanceof IncomingEDMResponse)
        post = new HttpPost("http://localhost:" + SimulatorConfig.getConnectorPort() + "/api/user/submit/error");
      else
        post = new HttpPost("http://localhost:" + SimulatorConfig.getConnectorPort() + "/api/user/submit/response");
      post.setEntity(new ByteArrayEntity(TCRestJAXB.outgoingMessage().getAsBytes(aOM)));
      aHCM.execute(post, new ResponseHandlerJson());
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }
}
