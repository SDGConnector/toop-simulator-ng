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

import com.helger.commons.ValueEnforcer;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.mime.CMimeType;
import com.helger.commons.url.SimpleURL;
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
import eu.toop.simulator.cli.CommandProcessor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A MOCK class that generates and sends DC requests
 *
 * @author yerlibilgin
 */
public class MockDC {

  private static final Logger LOGGER = LoggerFactory.getLogger(MockDC.class);

  /**
   * Sends a request that is contained in a file with name <code>sFileName</code>
   * @param sFileName the file that contains the request. May not be null
   */
  public static void sendDCRequest(@Nonnull String sFileName) {

    ValueEnforcer.notNull(sFileName, "file name");
    File file = new File(sFileName);
    if (!file.exists()) {
      LOGGER.error("The file with name " + sFileName + " does not exist");
      return;
    }

    try {
      sendRequest(file);
    } catch (IOException e) {
      LOGGER.error("IOException during send-dc-request " + sFileName + ": " + e.getMessage());
    }
  }

  private static void sendRequest(File file) throws IOException {
    // Build base URL and fetch all records per HTTP request
    final SimpleURL aBaseURL = new SimpleURL("http://localhost:" + SimulatorConfig.connectorPort + "/api/user/submit/request");
    LOGGER.info("Sending the request to  " + aBaseURL);

    try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
      final HttpPost aPost = new HttpPost(aBaseURL.getAsURI());

      final byte[] bytes = Files.readAllBytes(file.toPath());
      aPost.setEntity(new ByteArrayEntity(bytes));
      aPost.setHeader("Content-type", "application/xml");
      final HttpResponse response = httpClient.execute(aPost);

      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new IllegalStateException("Request failed " + response.getStatusLine().getStatusCode());
      }

      LOGGER.info("Request sent successfully");
    }
  }

  /**
   * Builds a default request and sends it the the connector
   * @throws IOException
   */
  public static void buildAndSendDefaultRequest() throws IOException {
    final TCOutgoingMessage aOM = new TCOutgoingMessage ();
    {
      final TCOutgoingMetadata aMetadata = new TCOutgoingMetadata ();
      aMetadata.setSenderID (TCRestJAXB.createTCID ("iso6523-actorid-upis", "9914:tc-ng-test-sender"));
      aMetadata.setReceiverID (TCRestJAXB.createTCID ("iso6523-actorid-upis", "9915:tooptest"));
      aMetadata.setDocTypeID (TCRestJAXB.createTCID ("toop-doctypeid-qns",
          "urn:eu:toop:ns:dataexchange-1p40::Response##urn:eu.toop.response.registeredorganization::1.40"));
      aMetadata.setProcessID (TCRestJAXB.createTCID ("toop-procid-agreement", "urn:eu.toop.process.datarequestresponse"));
      aMetadata.setTransportProtocol (EMEProtocol.AS4.getTransportProfileID ());
      aOM.setMetadata (aMetadata);
    }
    {
      final TCPayload aPayload = new TCPayload ();
      aPayload.setValue (StreamHelper.getAllBytes (new ClassPathResource("edm-conceptRequest-lp.xml")));
      aPayload.setMimeType (CMimeType.APPLICATION_XML.getAsString ());
      aPayload.setContentID ("mock-request@toop");
      aOM.addPayload (aPayload);
    }

    LOGGER.info (TCRestJAXB.outgoingMessage ().getAsString (aOM));

    try (HttpClientManager aHCM = new HttpClientManager ())
    {
      final HttpPost aPost = new HttpPost ("http://localhost:" + SimulatorConfig.connectorPort + "/api/user/submit/request");
      aPost.setEntity (new ByteArrayEntity (TCRestJAXB.outgoingMessage ().getAsBytes (aOM)));
      final IJson aJson = aHCM.execute (aPost, new ResponseHandlerJson());
      //LOGGER.info (new JsonWriter(new JsonWriterSettings().setIndentEnabled (true)).writeAsString (aJson));
    }
  }

}
