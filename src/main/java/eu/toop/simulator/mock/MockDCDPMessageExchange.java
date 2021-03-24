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


import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.IsSPIImplementation;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.error.level.EErrorLevel;

import eu.toop.connector.api.me.IMessageExchangeSPI;
import eu.toop.connector.api.me.incoming.IIncomingEDMResponse;
import eu.toop.connector.api.me.incoming.IMEIncomingHandler;
import eu.toop.connector.api.me.incoming.IncomingEDMErrorResponse;
import eu.toop.connector.api.me.incoming.IncomingEDMResponse;
import eu.toop.connector.api.me.incoming.MEIncomingTransportMetadata;
import eu.toop.connector.api.me.model.MEMessage;
import eu.toop.connector.api.me.model.MEPayload;
import eu.toop.connector.api.me.outgoing.IMERoutingInformation;
import eu.toop.connector.api.me.outgoing.MEOutgoingException;
import eu.toop.connector.app.incoming.DC_DP_TriggerViaHttp;
import eu.toop.edm.EDMErrorResponse;
import eu.toop.edm.EDMRequest;
import eu.toop.edm.EDMResponse;
import eu.toop.edm.IEDMTopLevelObject;
import eu.toop.edm.xml.EDMPayloadDeterminator;
import eu.toop.kafkaclient.ToopKafkaClient;
import eu.toop.simulator.SimulationMode;
import eu.toop.simulator.SimulatorConfig;

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
              DC_DP_TriggerViaHttp.forwardMessage((IncomingEDMResponse) response, SimulatorConfig.getDcEndpoint());
            if (response instanceof IncomingEDMErrorResponse)
              DC_DP_TriggerViaHttp.forwardMessage((IncomingEDMErrorResponse) response, SimulatorConfig.getDcEndpoint());
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
        DC_DP_TriggerViaHttp.forwardMessage(new IncomingEDMResponse((EDMResponse) aTopLevel,"mock@toop",
            aAttachments,
            aMetadata), SimulatorConfig.getDcEndpoint());
      } else if (aTopLevel instanceof EDMErrorResponse) {
        // Error response
        DC_DP_TriggerViaHttp.forwardMessage(new IncomingEDMErrorResponse((EDMErrorResponse) aTopLevel,"mock@toop",
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
