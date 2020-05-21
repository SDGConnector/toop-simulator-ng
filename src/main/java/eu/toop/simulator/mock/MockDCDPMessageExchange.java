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
import com.helger.commons.annotation.IsSPIImplementation;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.ICommonsList;
import eu.toop.connector.api.me.IMessageExchangeSPI;
import eu.toop.connector.api.me.incoming.IMEIncomingHandler;
import eu.toop.connector.api.me.model.MEMessage;
import eu.toop.connector.api.me.model.MEPayload;
import eu.toop.connector.api.me.outgoing.IMERoutingInformation;
import eu.toop.connector.api.me.outgoing.MEOutgoingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import java.io.IOException;

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
      processRequestResponse(meMessage);
    } catch (Exception ex) {
      throw new MEOutgoingException(ex.getMessage(), ex);
    }
  }

  public void processRequestResponse(@Nonnull MEMessage aMessage) throws IOException {

    final ICommonsList<MEPayload> plList = aMessage.getAllPayloads();

    plList.forEach(mePayload -> {
      //// Extract from ASiC
      //final ICommonsList<AsicReadEntry> aAttachments = new CommonsArrayList<>();
      //
      //final Serializable aMsg = ToopMessageBuilder140.parseRequestOrResponse(mePayload.getData().getInputStream(), aAttachments::add);
//
      //// Response before Request because it is derived from Request!
      //if (aMsg instanceof TDETOOPResponseType) {
      //  // This is the way from DP back to DC; we're in DC incoming mode
      //  final ToopResponseWithAttachments140 aResponse = new ToopResponseWithAttachments140((TDETOOPResponseType) aMsg,
      //      aAttachments);
      //  m_aIncomingHandler.handleIncomingResponse(aResponse);
      //} else if (aMsg instanceof TDETOOPRequestType) {
      //  // This is the way from DC to DP; we're in DP incoming mode
      //  final ToopRequestWithAttachments140 aRequest = new ToopRequestWithAttachments140((TDETOOPRequestType) aMsg,
      //      aAttachments);
//
      //  IncomingEDMRequest
      //  m_aIncomingHandler.handleIncomingRequest();
      //} else
      //  ToopKafkaClient.send(EErrorLevel.ERROR, () -> "Unsupported Message: " + aMsg);
    });
    //else
    //  ToopKafkaClient.send(EErrorLevel.WARN, () -> "MEMessage contains no payload: " + aMessage);
  }


  public void shutdown(@Nonnull final ServletContext aServletContext) {
  }

  @Override
  public String toString() {
    return "MockDCDPMessageExchange: Connector to DC-DP direct message submitter";
  }
}
