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
package eu.toop.connector.standalone;

import javax.annotation.concurrent.Immutable;

import com.helger.photon.jetty.JettyStarter;

/**
 * Run as a standalone web application in Jetty on port 8090.<br>
 * http://localhost:8090/
 *
 * @author Philip Helger
 */
@Immutable
public final class RunInJettyTOOPSIM
{
  public static void main (final String [] args) throws Exception
  {
    final JettyStarter js = new JettyStarter (RunInJettyTOOPSIM.class).setPort (8090)
                                                                        .setStopPort (9090)
                                                                        .setSessionCookieName ("TOOP_TC_DC_SESSION")
                                                                        .setContainerIncludeJarPattern (".*/classes/.*");
    js.run ();
  }
}
