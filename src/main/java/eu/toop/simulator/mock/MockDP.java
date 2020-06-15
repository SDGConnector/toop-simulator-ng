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
import com.helger.commons.url.SimpleURL;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ResponseHandlerJson;
import com.helger.json.IJson;
import eu.toop.connector.api.me.EMEProtocol;
import eu.toop.connector.api.me.incoming.*;
import eu.toop.connector.api.rest.TCOutgoingMessage;
import eu.toop.connector.api.rest.TCOutgoingMetadata;
import eu.toop.connector.api.rest.TCPayload;
import eu.toop.connector.api.rest.TCRestJAXB;
import eu.toop.connector.app.incoming.MPTrigger;
import eu.toop.edm.EDMErrorResponse;
import eu.toop.edm.EDMRequest;
import eu.toop.edm.EDMResponse;
import eu.toop.playground.dp.DPException;
import eu.toop.playground.dp.service.ToopDP;
import eu.toop.simulator.SimulatorConfig;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
      EDMResponse edmResponse = miniDP.createEDMResponseFromRequest(aTopLevel);
      //we have a response from DP, push it back

      return new IncomingEDMResponse(edmResponse,
          //NOTE: attachments are empty for now
          new CommonsArrayList<>(),
          aMetadataInverse);

    } catch (DPException e) {
      EDMErrorResponse edmError = e.getEdmErrorResponse();
      //we have an error from DP, push it back

      return new IncomingEDMErrorResponse(edmError,
          aMetadataInverse);
    }
  }


  /**
   * Send the request directly to the DP/to-dp
   *
   * @param edmRequest
   * @param aMetadata
   */
  public static void deliverRequestToDP(EDMRequest edmRequest, MEIncomingTransportMetadata aMetadata) {
    MPTrigger.forwardMessage(new IncomingEDMRequest(edmRequest,
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
      aMetadata.setDocTypeID (TCRestJAXB.createTCID ("toop-doctypeid-qns",
          "RegisteredOrganization::REGISTERED_ORGANIZATION_TYPE::CONCEPT##CCCEV::toop-edm:v2.0"));
      aMetadata.setProcessID(TCRestJAXB.createTCID(response.getMetadata().getProcessID().getScheme(), response.getMetadata().getProcessID().getValue()));

      aMetadata.setTransportProtocol(EMEProtocol.AS4.getTransportProfileID());
      aOM.setMetadata(aMetadata);
    }
    {
      final TCPayload aPayload = new TCPayload();
      if (response instanceof IncomingEDMResponse)
        aPayload.setValue(((IncomingEDMResponse) response).getResponse().getWriter().getAsBytes());
      if (response instanceof IncomingEDMErrorResponse)
        aPayload.setValue(((IncomingEDMErrorResponse) response).getErrorResponse().getWriter().getAsBytes());

      aPayload.setMimeType(CMimeType.APPLICATION_XML.getAsString());
      aPayload.setContentID("mock-response@toop");
      aOM.addPayload(aPayload);
    }

    LOGGER.info(TCRestJAXB.outgoingMessage().getAsString(aOM));

    try (HttpClientManager aHCM = new HttpClientManager()) {
      final HttpPost aPost = new HttpPost("http://localhost:" + SimulatorConfig.getConnectorPort() + "/api/user/submit/response");
      aPost.setEntity(new ByteArrayEntity(TCRestJAXB.outgoingMessage().getAsBytes(aOM)));
      aHCM.execute(aPost, new ResponseHandlerJson());
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
    }
  }
}
