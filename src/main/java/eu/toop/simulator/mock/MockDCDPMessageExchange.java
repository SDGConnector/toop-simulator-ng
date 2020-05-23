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
import com.helger.commons.annotation.IsSPIImplementation;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.error.level.EErrorLevel;
import eu.toop.connector.api.me.IMessageExchangeSPI;
import eu.toop.connector.api.me.incoming.*;
import eu.toop.connector.api.me.model.MEMessage;
import eu.toop.connector.api.me.model.MEPayload;
import eu.toop.connector.api.me.outgoing.IMERoutingInformation;
import eu.toop.connector.api.me.outgoing.MEOutgoingException;
import eu.toop.connector.app.incoming.MPTrigger;
import eu.toop.edm.EDMErrorResponse;
import eu.toop.edm.EDMRequest;
import eu.toop.edm.EDMResponse;
import eu.toop.edm.IEDMTopLevelObject;
import eu.toop.edm.xml.EDMPayloadDeterminator;
import eu.toop.kafkaclient.ToopKafkaClient;
import eu.toop.playground.dp.DPException;
import eu.toop.playground.dp.service.ToopDP;
import eu.toop.simulator.SimulationMode;
import eu.toop.simulator.SimulatorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import java.io.InputStream;

/**
 * TOOP {@link eu.toop.connector.api.me.IMessageExchangeSPI} implementation using ph-as4.
 *
 * @author yerlibilgin
 */
@IsSPIImplementation
public class MockDCDPMessageExchange implements IMessageExchangeSPI {
  public static final String ID = "mem-mockdcdp";
  private static final Logger LOGGER = LoggerFactory.getLogger(MockDCDPMessageExchange.class);

  private IMEIncomingHandler m_aIncomingHandler;

  private final ToopDP miniDP = new ToopDP();

  public MockDCDPMessageExchange() {
  }

  @Nonnull
  @Nonempty
  public String getID() {
    return ID;
  }

  @Override
  public void registerIncomingHandler(@Nonnull final ServletContext aServletContext,
                                      @Nonnull final IMEIncomingHandler aIncomingHandler) {
    ValueEnforcer.notNull(aServletContext, "ServletContext");
    ValueEnforcer.notNull(aIncomingHandler, "IncomingHandler");
    if (m_aIncomingHandler != null)
      throw new IllegalStateException("Another incoming handler was already registered!");
    m_aIncomingHandler = aIncomingHandler;
  }

  @Override
  public void sendOutgoing(@Nonnull IMERoutingInformation imeRoutingInformation, @Nonnull MEMessage meMessage) throws MEOutgoingException {
    try {
      final MEPayload aHead = meMessage.payloads().getFirst();
      final InputStream inputStream = aHead.getData().getInputStream();
      final IEDMTopLevelObject aTopLevel = EDMPayloadDeterminator.parseAndFind(inputStream);
      // TODO get metadata in here
      final MEIncomingTransportMetadata aMetadata = new MEIncomingTransportMetadata(null, null, null, null);
      if (aTopLevel instanceof EDMRequest) {
        if (SimulatorConfig.mode == SimulationMode.DP) {
          loopBackFromElonia((EDMRequest) aTopLevel, aMetadata);
        } else {
          //send it to the configured /to-dp
          sendRequestToDp((EDMRequest) aTopLevel);
        }
      } else if (aTopLevel instanceof EDMResponse) {
        // Response, send to freedonia
        final ICommonsList<MEPayload> aAttachments = new CommonsArrayList<>();
        for (final MEPayload aItem : meMessage.payloads())
          if (aItem != aHead)
            aAttachments.add(aItem);
        m_aIncomingHandler.handleIncomingResponse(new IncomingEDMResponse((EDMResponse) aTopLevel,
            aAttachments,
            aMetadata));
      } else if (aTopLevel instanceof EDMErrorResponse) {
        // Error response
        m_aIncomingHandler.handleIncomingErrorResponse(new IncomingEDMErrorResponse((EDMErrorResponse) aTopLevel,
            aMetadata));
      } else {
        // Unknown
        ToopKafkaClient.send(EErrorLevel.ERROR, () -> "Unsupported Message: " + aTopLevel);
      }
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
      throw new MEOutgoingException(ex.getMessage(), ex);
    }
  }

  /**
   * Send the request directly to the DP/to-dp
   *
   * @param aTopLevel
   */
  private void sendRequestToDp(EDMRequest aTopLevel) {

  }

  private void loopBackFromElonia(EDMRequest aTopLevel, MEIncomingTransportMetadata aMetadata) throws MEOutgoingException {
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
    try {
      m_aIncomingHandler.handleIncomingResponse(new IncomingEDMResponse(edmResponse,
          new CommonsArrayList<>(),
          aMetadata));
    } catch (MEIncomingException e) {
      throw new MEOutgoingException(e.getMessage(), e);
    }
  }


  public void shutdown(@Nonnull final ServletContext aServletContext) {
  }

  @Override
  public String toString() {
    return "MockDCDPMessageExchange: Connector to DC-DP direct message submitter";
  }
}
