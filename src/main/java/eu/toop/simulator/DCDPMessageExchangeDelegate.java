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
package eu.toop.simulator;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;

import eu.toop.connector.api.me.IMessageExchangeSPI;
import eu.toop.connector.api.me.incoming.IMEIncomingHandler;
import eu.toop.connector.api.me.model.MEMessage;
import eu.toop.connector.api.me.outgoing.IMERoutingInformation;
import eu.toop.connector.api.me.outgoing.MEOutgoingException;
import eu.toop.connector.mem.external.spi.ExternalMessageExchangeSPI;
import eu.toop.simulator.mock.MockDCDPMessageExchange;

/**
 * <p>
 * This class delegates to an underlying IMessageExchangeSPI implementation
 * that is configured with respect to the <code>toop-simulator.mockGateway</code> configuration.<br>
 * If the <code>toop-simulator.mockGateway==true</code> then the gateway communication simulated
 * and an instance of {@link eu.toop.simulator.mock.MockDCDPMessageExchange} is used,
 * <br>otherwise (<code>toop-simulator.mockGateway==true</code>) then the message exchange communication
 * is not simulated and an instance of {@link eu.toop.connector.mem.external.spi.ExternalMessageExchangeSPI} is used.
 * </p>
 * <p>In the latter case, the communication with a gateway is configured with the key
 * <code>toop.mem.as4.endpoint</code> and all
 * <code>the toop.mem.as4.*</code> configurations (see toop-connector.properties) become significant.</p>
 * <p>Another important point for the latter case is that; the discovery data will have to be realistic as well.
 * You should not use fake certificates, and the CName property of the x509 certificate has to be
 * compatible with the gateway party id. Please see more on toop wiki</p>
 *
 * @author yerlibilgin
 */
public class DCDPMessageExchangeDelegate implements IMessageExchangeSPI {

  private static final Logger LOGGER = LoggerFactory.getLogger(DCDPMessageExchangeDelegate.class);
  private final IMessageExchangeSPI underlyingSPI;

  /**
   * Create a new DCDPMessageExchangeDelegate. Check the configuration and decide to
   * mock or use the default SPI for the IMessageExchangeSPI.
   */
  public DCDPMessageExchangeDelegate() {
    if (SimulatorConfig.isMockGateway()) {
      LOGGER.info("Using " + MockDCDPMessageExchange.class.getName());
      underlyingSPI = new MockDCDPMessageExchange();
    } else {
      LOGGER.info("Using " + ExternalMessageExchangeSPI.class.getName());
      underlyingSPI = new ExternalMessageExchangeSPI();
    }
  }

  /**
   * Since the id mem-default is already taken, we cannot provide it for the second time.
   * So we provide the simulator value {@value eu.toop.simulator.mock.MockDCDPMessageExchange#ID}
   * @return the id of this message exchange SPI implementation
   */
  @Override
  @Nonempty
  @Nonnull
  public String getID() {
    return MockDCDPMessageExchange.ID;
  }

  @Override
  public void registerIncomingHandler(@Nonnull ServletContext aServletContext, @Nonnull IMEIncomingHandler aIncomingHandler) {
    underlyingSPI.registerIncomingHandler(aServletContext, aIncomingHandler);
  }

  @Override
  public void sendOutgoing(@Nonnull IMERoutingInformation aRoutingInfo, @Nonnull MEMessage aMessage) throws MEOutgoingException {
    underlyingSPI.sendOutgoing(aRoutingInfo, aMessage);
  }

  @Override
  public void shutdown(@Nonnull ServletContext servletContext) {
    underlyingSPI.shutdown(servletContext);
  }
}
