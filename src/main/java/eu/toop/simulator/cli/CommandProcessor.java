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
import com.helger.commons.string.StringHelper;
import com.helger.commons.url.SimpleURL;
import com.helger.pd.searchapi.PDSearchAPIReader;
import com.helger.pd.searchapi.v1.ResultListType;
import eu.toop.simulator.SimulatorConfig;
import eu.toop.simulator.ToopSimulatorMain;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

  /**
   * Process the send-dc-request command
   *
   * @param command the input command
   */
  public static void processSendRequestCommand(CliCommand command) {
    ValueEnforcer.notNull(command, "Empty command list");

    boolean hasFileOption = command.hasOption("f");

    if (hasFileOption) {
      List<String> fileArgs = command.getArguments("f");
      ValueEnforcer.isEqual(fileArgs.size(), 1, "-f option needs exactly one argument");
      sendDCRequest(fileArgs.get(0));
    } else {
      LOGGER.debug("Using the default file data/toop-request.xml");
      sendDCRequest("data/toop-request.xml");
    }
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


  private static void sendDCRequest(String s) {
    File file = new File(s);

    if (!file.exists()) {
      System.err.println("The file with name " + s + " does not exist");

      return;
    }


    try {
      sendRequest(file);
    } catch (IOException e) {
      LOGGER.error("IOException during send-dc-request " + s + ": " + e.getMessage());
    }
  }


  private static void sendRequest(File file) throws IOException {
    // Build base URL and fetch all records per HTTP request
    final SimpleURL aBaseURL = new SimpleURL("http://localhost:" + SimulatorConfig.connectorPort + "/api/user/submit/request");
    LOGGER.info("Sending the request to  " + aBaseURL);

    try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
      final HttpPost aPost = new HttpPost(aBaseURL.getAsURI());
      aPost.setEntity(new FileEntity(file));
      aPost.setHeader("Content-type", "application/xml");
      final HttpResponse response = httpClient.execute(aPost);

      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new IllegalStateException("Request failed " + response.getStatusLine().getStatusCode());
      }

      LOGGER.info("Request sent successfully");
    }
  }

}