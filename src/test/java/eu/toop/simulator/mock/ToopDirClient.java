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

import com.helger.commons.string.StringHelper;
import com.helger.commons.url.SimpleURL;
import com.helger.pd.searchapi.PDSearchAPIReader;
import com.helger.pd.searchapi.v1.MatchType;
import com.helger.pd.searchapi.v1.ResultListType;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This class is the bridge between DSD and TOOP directory. It queries the TOOP
 * directory and returns the responses as a list of <code>MatchType</code>
 * objects
 *
 * @author yerlibilgin
 */
public class ToopDirClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ToopDirClient.class);

  /**
   * Query TOOP-DIR with country code and doctype. Return the result as a list of
   * <code>MatchType</code> objects
   *
   *
   * @param toopDirBaseURL  the base URL of Toop Directory
   * @param sCountryCode    two letter Country Code, @Nullable
   * @param aDocumentTypeID doc type id, @Nullable
   * @return list of <code>MatchType</code> objects
   * @throws IOException if a communication problem occurs
   */
  public static List<MatchType> performSearch(final String toopDirBaseURL, @Nullable final String sCountryCode,
      @Nullable final String aDocumentTypeID) throws IOException {

    return performSearchResultsLists(toopDirBaseURL, sCountryCode, aDocumentTypeID).getMatch();

  }

  public static ResultListType performSearchResultsLists(final String toopDirBaseURL, @Nullable final String sCountryCode,
      @Nullable final String aDocumentTypeID) throws IOException {
    if (StringHelper.hasNoText(toopDirBaseURL))
      throw new IllegalStateException("The Directory base URL configuration is missing");

    // Build base URL and fetch all records per HTTP request
    final SimpleURL aBaseURL = new SimpleURL(toopDirBaseURL + "/search/1.0/xml");
    // More than 1000 is not allowed
    aBaseURL.add("rpc", 100);
    // Constant defined in CCTF-103
    aBaseURL.add("identifierScheme", "DataSubjectIdentifierScheme");
    // Parameters to this servlet
    if (sCountryCode != null && !sCountryCode.isEmpty()) {
      aBaseURL.add("country", sCountryCode);
    }

    if (aDocumentTypeID != null && !aDocumentTypeID.isEmpty()) {
      aBaseURL.add("doctype", aDocumentTypeID);
    }

    if (LOGGER.isInfoEnabled())
      LOGGER.info("Querying " + aBaseURL.getAsStringWithEncodedParameters());

    try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
      final HttpGet aGet = new HttpGet(aBaseURL.getAsURI());

      final HttpResponse response = httpClient.execute(aGet);

      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new IllegalStateException("Request failed " + response.getStatusLine().getStatusCode());
      }

      try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
        response.getEntity().writeTo(stream);
        final byte[] s_bytes = stream.toByteArray();

        final String s_result = new String(s_bytes, StandardCharsets.UTF_8);
        LOGGER.debug(s_result);

        // Read from bytes to avoid charset error
        final ResultListType read = PDSearchAPIReader.resultListV1().read(s_bytes);
        return read;
      }
    }
  }

}
