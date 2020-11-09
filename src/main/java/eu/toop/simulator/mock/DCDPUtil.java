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
package eu.toop.simulator.mock;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.mime.CMimeType;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ResponseHandlerJson;
import com.helger.json.IJson;
import com.helger.json.serialize.JsonWriter;
import com.helger.json.serialize.JsonWriterSettings;
import eu.toop.connector.api.me.EMEProtocol;
import eu.toop.connector.api.rest.TCOutgoingMessage;
import eu.toop.connector.api.rest.TCOutgoingMetadata;
import eu.toop.connector.api.rest.TCPayload;
import eu.toop.connector.api.rest.TCRestJAXB;
import eu.toop.simulator.SimulatorConfig;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;
import java.io.*;

/**
 * A utility class that contains methods for submitting requests/responses to the connector
 *
 * @author yerlibilgin
 */
public class DCDPUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(DCDPUtil.class);

  /**
   * Send a TC outgoing message to the
   * @param sender the sender participant id. optional
   * @param receiver the receiver participant id. optional
   * @param docTypeId doctype id. optional
   * @param sFileName optional edm filename. optional
   * @param defaultResourceName the default edm classpath resource if the file doesn't exist. May not be null
   * @param connectorEndpoint the connector endpoint to submit the message. May not be null
   */
  public static void sendTCOutgoingMessage(@Nullable String sender, @Nullable String receiver, @Nullable String docTypeId,
                                           @Nullable String sFileName, @Nonnull  String defaultResourceName,
                                           @Nonnull String connectorEndpoint) {

    ValueEnforcer.notNull(defaultResourceName, "defaultResourceName");
    ValueEnforcer.notNull(connectorEndpoint, "connectorEndpoint");

    InputStream edmSourceStream;
    if (sFileName != null) {
      final File file = new File(sFileName);
      if (!file.exists() || !file.isFile()) {
        throw new IllegalArgumentException("The file with name " + sFileName + " does not exist or is not a file");
      }

      try {
        edmSourceStream = new FileInputStream(sFileName);
      } catch (FileNotFoundException e) {
        throw new IllegalStateException(e);
      }
    } else {
      edmSourceStream = MockDC.class.getResourceAsStream(defaultResourceName);
    }

    try {
      sendRequest(sender, receiver, docTypeId, edmSourceStream, connectorEndpoint);
    } catch (IOException e) {
      LOGGER.error("IOException during submission to " + connectorEndpoint + ": " + sFileName + ": " + e.getMessage());
    }
  }

  private static void sendRequest(String sender, String receiver, String docType, InputStream edmSourceStream, String connectorEndpoint) throws IOException {
    ValueEnforcer.notNull(edmSourceStream, "edmSourceStream");

    final TCOutgoingMessage aOM = new TCOutgoingMessage();
    {
      final TCOutgoingMetadata aMetadata = new TCOutgoingMetadata();
      aMetadata.setSenderID(TCRestJAXB.createTCID(SimulatorConfig.getSenderScheme(), sender));
      aMetadata.setReceiverID(TCRestJAXB.createTCID(SimulatorConfig.getReceiverScheme(), receiver));

      aMetadata.setDocTypeID(TCRestJAXB.createTCID("toop-doctypeid-qns",
          docType));
      aMetadata.setProcessID(TCRestJAXB.createTCID("toop-procid-agreement", "urn:eu.toop.process.dataquery"));
      aMetadata.setTransportProtocol(EMEProtocol.AS4.getTransportProfileID());
      aOM.setMetadata(aMetadata);
    }
    {
      final TCPayload aPayload = new TCPayload();
      aPayload.setValue(IOUtils.toByteArray(edmSourceStream));

      aPayload.setMimeType(CMimeType.APPLICATION_XML.getAsString());
      aPayload.setContentID("simualtorrequest@toop");
      aOM.addPayload(aPayload);
    }

    LOGGER.info(TCRestJAXB.outgoingMessage().getAsString(aOM));

    try (HttpClientManager aHCM = new HttpClientManager()) {
      final HttpPost aPost = new HttpPost("http://localhost:" + SimulatorConfig.getConnectorPort() + connectorEndpoint);
      final byte[] asBytes = TCRestJAXB.outgoingMessage().getAsBytes(aOM);
      LOGGER.info(new String(asBytes));
      aPost.setEntity(new ByteArrayEntity(asBytes));
      final IJson aJson = aHCM.execute(aPost, new ResponseHandlerJson());
      LOGGER.info (new JsonWriter(new JsonWriterSettings().setIndentEnabled (true)).writeAsString (aJson));
    }
  }
}
