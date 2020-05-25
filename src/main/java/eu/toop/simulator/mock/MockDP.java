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
package eu.toop.simulator.mock;

import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.mime.CMimeType;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ResponseHandlerJson;
import com.helger.json.IJson;
import eu.toop.connector.api.me.EMEProtocol;
import eu.toop.connector.api.me.incoming.IncomingEDMRequest;
import eu.toop.connector.api.me.incoming.IncomingEDMResponse;
import eu.toop.connector.api.me.incoming.MEIncomingTransportMetadata;
import eu.toop.connector.api.me.outgoing.MEOutgoingException;
import eu.toop.connector.api.rest.TCOutgoingMessage;
import eu.toop.connector.api.rest.TCOutgoingMetadata;
import eu.toop.connector.api.rest.TCPayload;
import eu.toop.connector.api.rest.TCRestJAXB;
import eu.toop.connector.app.incoming.MPTrigger;
import eu.toop.edm.EDMRequest;
import eu.toop.edm.EDMResponse;
import eu.toop.playground.dp.DPException;
import eu.toop.playground.dp.service.ToopDP;
import eu.toop.simulator.SimulatorConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A MOCK class that generates and sends DP responses
 *
 * @author yerlibilgin
 */
public class MockDP {

  private static final Logger LOGGER = LoggerFactory.getLogger(MockDP.class);

  private static final ToopDP miniDP = new ToopDP();

  /**
   * Provide the message to miniDP and get back an {@link IncomingEDMResponse}
   *
   * @param aTopLevel the request
   * @param aMetadata the metadata
   * @return the response
   * @throws MEOutgoingException if a problem occurs
   */
  public static IncomingEDMResponse eloniaCreateResponse(EDMRequest aTopLevel, MEIncomingTransportMetadata aMetadata) throws MEOutgoingException {
    EDMRequest request = aTopLevel;
    byte[] responseBytes;
    try {
      responseBytes = miniDP.createXMLResponseFromRequest(request.getWriter().getAsBytes());
      if (responseBytes == null)
        throw new IllegalStateException("Coudln't get automatic response from elonia");
    } catch (DPException e) {
      throw new MEOutgoingException(e.getMessage(), e);
    }
    EDMResponse edmResponse = EDMResponse.reader().read(responseBytes);
    //we have a response from DP, push it back
    //we need to create a new metadata where the sender and receiver are switched.
    final MEIncomingTransportMetadata aMetadataInverse = new MEIncomingTransportMetadata(
        aMetadata.getReceiverID(), aMetadata.getSenderID(),
        aMetadata.getDocumentTypeID(), aMetadata.getProcessID());

    return new IncomingEDMResponse(edmResponse,
        //NOTE: attachments are empty for now
        new CommonsArrayList<>(),
        aMetadataInverse);

  }


  /**
   * Send the request directly to the DP/to-dp
   *
   * @param edmRequest
   * @param aMetadata
   */
  public static void sendRequestToDp(EDMRequest edmRequest, MEIncomingTransportMetadata aMetadata) {
    MPTrigger.forwardMessage(new IncomingEDMRequest(edmRequest,
        aMetadata), SimulatorConfig.dpEndpoint);
  }

  /**
   * Build a TCOutgoingMessage from the response and send it to the connector
   *
   * @param response the response
   */
  public static void buildAndSendResponse(IncomingEDMResponse response) {
    final TCOutgoingMessage aOM = new TCOutgoingMessage();
    {
      final TCOutgoingMetadata aMetadata = new TCOutgoingMetadata();
      //invert sender and receiver
      aMetadata.setSenderID(TCRestJAXB.createTCID(response.getMetadata().getReceiverID().getScheme(), response.getMetadata().getReceiverID().getValue()));
      aMetadata.setReceiverID(TCRestJAXB.createTCID(response.getMetadata().getSenderID().getScheme(), response.getMetadata().getSenderID().getValue()));
      aMetadata.setDocTypeID(TCRestJAXB.createTCID(response.getMetadata().getDocumentTypeID().getScheme(), response.getMetadata().getDocumentTypeID().getValue()));
      aMetadata.setProcessID(TCRestJAXB.createTCID(response.getMetadata().getProcessID().getScheme(), response.getMetadata().getProcessID().getValue()));
      aMetadata.setTransportProtocol(EMEProtocol.AS4.getTransportProfileID());
      aOM.setMetadata(aMetadata);
    }
    {
      final TCPayload aPayload = new TCPayload();
      aPayload.setValue(response.getResponse().getWriter().getAsBytes());
      aPayload.setMimeType(CMimeType.APPLICATION_XML.getAsString());
      aPayload.setContentID("mock-response@toop");
      aOM.addPayload(aPayload);
    }

    LOGGER.info(TCRestJAXB.outgoingMessage().getAsString(aOM));

    try (HttpClientManager aHCM = new HttpClientManager()) {
      final HttpPost aPost = new HttpPost("http://localhost:" + SimulatorConfig.connectorPort + "/api/user/submit/response");
      aPost.setEntity(new ByteArrayEntity(TCRestJAXB.outgoingMessage().getAsBytes(aOM)));
      aHCM.execute(aPost, new ResponseHandlerJson());
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }
}
