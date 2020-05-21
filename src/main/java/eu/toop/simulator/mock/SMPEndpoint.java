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

import com.helger.peppolid.IParticipantIdentifier;

import java.security.cert.X509Certificate;

/**
 * This class is a Java representation of the {@link com.helger.xsds.bdxr.smp1.EndpointType} classes.
 *
 * @author yerlibilgin
 */
public class SMPEndpoint {
  private final IParticipantIdentifier participantID;
  private final String transportProfile;
  private final String endpointURI;
  private final X509Certificate x509Certificate;

  public SMPEndpoint(IParticipantIdentifier participantID, String transportProfile, String endpointURI, X509Certificate x509Certificate) {

    this.participantID = participantID;
    this.transportProfile = transportProfile;
    this.endpointURI = endpointURI;
    this.x509Certificate = x509Certificate;
  }

  public IParticipantIdentifier getParticipantID() {
    return participantID;
  }

  public String getTransportProfile() {
    return transportProfile;
  }

  public String getEndpointURI() {
    return endpointURI;
  }

  public X509Certificate getX509Certificate() {
    return x509Certificate;
  }
}
