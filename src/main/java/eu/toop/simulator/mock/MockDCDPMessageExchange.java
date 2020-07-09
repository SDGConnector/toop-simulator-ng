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
    LOGGER.warn("Ignoring IncomingHandler, using MPTrigger directly.");
  }

  @Override
  public void sendOutgoing(@Nonnull IMERoutingInformation imeRoutingInformation, @Nonnull MEMessage meMessage) throws MEOutgoingException {
    try {
      final MEPayload aHead = meMessage.payloads().getFirst();
      final InputStream inputStream = aHead.getData().getInputStream();
      final IEDMTopLevelObject aTopLevel = EDMPayloadDeterminator.parseAndFind(inputStream);
      final MEIncomingTransportMetadata aMetadata = new MEIncomingTransportMetadata(
          imeRoutingInformation.getSenderID(), imeRoutingInformation.getReceiverID(),
          imeRoutingInformation.getDocumentTypeID(), imeRoutingInformation.getProcessID());

      if (aTopLevel instanceof EDMRequest) {
        if (SimulatorConfig.getMode() == SimulationMode.DP) {
          if (SimulatorConfig.isDpResponseAuto()) {
            LOGGER.debug("Automatic response will be created and sent");
            //create response and send back
            final IIncomingEDMResponse response = MockDP.eloniaCreateResponse((EDMRequest) aTopLevel, aMetadata);
            if (response instanceof IncomingEDMResponse)
              MPTrigger.forwardMessage((IncomingEDMResponse) response, SimulatorConfig.getDcEndpoint());
            if (response instanceof IncomingEDMErrorResponse)
              MPTrigger.forwardMessage((IncomingEDMErrorResponse) response, SimulatorConfig.getDcEndpoint());
          } else {
            LOGGER.debug("Automatic response is disabled. Having a rest");
          }
        } else {
          //send it to the configured /to-dp
          MockDP.deliverRequestToDP((EDMRequest) aTopLevel, aMetadata);
        }
      } else if (aTopLevel instanceof EDMResponse) {
        // Response, send to freedonia
        final ICommonsList<MEPayload> aAttachments = new CommonsArrayList<>();
        for (final MEPayload aItem : meMessage.payloads())
          if (aItem != aHead)
            aAttachments.add(aItem);
        MPTrigger.forwardMessage(new IncomingEDMResponse((EDMResponse) aTopLevel,"mock@toop",
            aAttachments,
            aMetadata), SimulatorConfig.getDcEndpoint());
      } else if (aTopLevel instanceof EDMErrorResponse) {
        // Error response
        MPTrigger.forwardMessage(new IncomingEDMErrorResponse((EDMErrorResponse) aTopLevel,"mock@toop",
            aMetadata), SimulatorConfig.getDcEndpoint());
      } else {
        // Unknown
        ToopKafkaClient.send(EErrorLevel.ERROR, () -> "Unsupported Message: " + aTopLevel);
      }
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
      throw new MEOutgoingException(ex.getMessage(), ex);
    }
  }

  public void shutdown(@Nonnull final ServletContext aServletContext) {
  }

  @Override
  public String toString() {
    return "MockDCDPMessageExchange: Connector to DC-DP direct message submitter";
  }
}
