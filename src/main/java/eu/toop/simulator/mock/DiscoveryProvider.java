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

import com.helger.commons.collection.impl.*;
import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.url.SimpleURL;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.HttpClientSettings;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.xsds.bdxr.smp1.DocumentIdentifierType;
import com.helger.xsds.bdxr.smp1.ParticipantIdentifierType;
import com.helger.xsds.bdxr.smp1.ProcessIdentifierType;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;
import eu.toop.connector.api.TCIdentifierFactory;
import eu.toop.connector.api.dd.IDDErrorHandler;
import eu.toop.connector.api.dd.IDDServiceGroupHrefProvider;
import eu.toop.connector.api.dd.IDDServiceMetadataProvider;
import eu.toop.connector.api.dsd.IDSDParticipantIDProvider;
import eu.toop.connector.api.simulator.CountryAwareServiceMetadataListType;
import eu.toop.connector.api.simulator.ObjectFactory;
import eu.toop.connector.api.simulator.TCSimulatorJAXB;
import eu.toop.simulator.ToopSimulatorResources;
import eu.toop.simulator.util.JAXBUtil;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class plays the role of both a directory and an SMP server. It reads its contents
 * from the file or classpath resource 'disovery-data.xml' and creates a map in the memory
 * to provide query results.
 *
 * @author yerlibilgin
 */
public class DiscoveryProvider implements IDDServiceGroupHrefProvider, IDDServiceMetadataProvider, IDSDParticipantIDProvider {

  /**
   * The Logger instance
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryProvider.class);

  static final DiscoveryProvider instance = new DiscoveryProvider();

  /**
   * The directory database
   * The key is the query params <code>getAllParticipantIDs</code> call
   */
  Map<DIRQuery, ICommonsSet<IParticipantIdentifier>> directoryMap;
  /**
   * The SMP database
   * The key is the query params <code>getEndpoints</code> call
   */
  Map<SMPQuery, ICommonsList<SMPEndpoint>> smpMap;

  public static DiscoveryProvider getInstance() {
    return instance;
  }

  private final CountryAwareServiceMetadataListType serviceMetadataListType;

  /*
   * Used in case no matches are found
   */
  private static final CommonsHashSet<IParticipantIdentifier> EMPTY_PID_SET = new CommonsHashSet<>();

  private CertificateFactory certificateFactory;

  {
    try {
      certificateFactory = CertificateFactory.getInstance("X509");
    } catch (CertificateException e) {
    }
  }

  private DiscoveryProvider() {
    //parse the file or resource discovery-data.xml
    try (InputStream is = ToopSimulatorResources.getDiscoveryDataResURL().openStream()) {
      serviceMetadataListType = JAXBUtil.parseURL( ToopSimulatorResources.getDiscoveryDataResURL(), ObjectFactory.class);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    //build a database for beter performance


    //step 1. build a directory map ( CountryCode, Doctype -- > List<participant id> )
    buildDirectoryMap();

    //step 2. build an SMP map (participant,doctype,transportprofile,procid) --> r2d2ep)
    buildSMPMap();
  }

  private void buildDirectoryMap() {

    LOGGER.info("Building directory map");
    // ok now that we have parsed the XML document and we will be serving queries with country code and doc type,
    // then no need to hold the root type and long loop at each query.
    // instead, we can create a mapping as [countrycode, doctype] ---> IParticipantIdentifier at once.
    // so that the queries (getAllParticipantIDs) perform faster.

    directoryMap = new HashMap<>();

    serviceMetadataListType.getCountryAwareServiceMetadata().forEach(countryAwareServiceMetadataType -> {

      String countrycode = countryAwareServiceMetadataType.getCountryCode();

      countryAwareServiceMetadataType.getServiceMetadata().forEach(serviceMetadataType -> {
        DocumentIdentifierType documentIdentifier = serviceMetadataType.getServiceInformation().getDocumentIdentifier();
        IDocumentTypeIdentifier docID = TCIdentifierFactory.INSTANCE_TC.createDocumentTypeIdentifier(documentIdentifier.getScheme(), documentIdentifier.getValue());


        DIRQuery dirQuery = new DIRQuery(countrycode, docID);

        ICommonsSet<IParticipantIdentifier> identifierSet;
        if (directoryMap.containsKey(dirQuery)) {
          identifierSet = directoryMap.get(dirQuery);
        } else {
          identifierSet = new CommonsHashSet<>();
          directoryMap.put(dirQuery, identifierSet);
        }

        //now add a new participant identifier to this set.
        //TODO: vulnerable, do null check
        identifierSet.add(TCIdentifierFactory.INSTANCE.createParticipantIdentifier(serviceMetadataType.getServiceInformation().getParticipantIdentifier().getScheme(),
            serviceMetadataType.getServiceInformation().getParticipantIdentifier().getValue()));
      });
    });
  }


  /**
   * Build a map of SMPQuery --> List&lt;IR2D2Participant&gt;
   */
  private void buildSMPMap() {
    LOGGER.info("Building SMP Map");

    smpMap = new HashMap<>();

    serviceMetadataListType.getCountryAwareServiceMetadata().forEach(country -> {

      country.getServiceMetadata().stream().map(ServiceMetadataType::getServiceInformation).forEach(serviceInformation -> {

        ParticipantIdentifierType participantIdentifier = serviceInformation.getParticipantIdentifier();
        IParticipantIdentifier participantID = TCIdentifierFactory.INSTANCE_TC.createParticipantIdentifier(participantIdentifier.getScheme(),
            participantIdentifier.getValue());

        DocumentIdentifierType documentIdentifier = serviceInformation.getDocumentIdentifier();
        IDocumentTypeIdentifier documentTypeID = TCIdentifierFactory.INSTANCE_TC.createDocumentTypeIdentifier(documentIdentifier.getScheme(), documentIdentifier.getValue());

        serviceInformation.getProcessList().getProcess().forEach(processType -> {
          ProcessIdentifierType processIdentifier = processType.getProcessIdentifier();
          IProcessIdentifier procID = TCIdentifierFactory.INSTANCE_TC.createProcessIdentifier(processIdentifier.getScheme(), processIdentifier.getValue());
          processType.getServiceEndpointList().getEndpoint().forEach(endpointType -> {
            String transportProfile = endpointType.getTransportProfile();

            try {
              final X509Certificate x509Certificate[] = new X509Certificate[1];
              //check if we have an extension to set the certificate from file
              endpointType.getExtension().forEach(extensionType -> {
                try {
                  if (extensionType.getAny() != null) {
                    Node any = (Node) extensionType.getAny();

                    if (any.getLocalName().equals("CertFileName")) {
                      //this is a special case for simulator. One can want to put his retificate as
                      //a file name, so that we can parse it from the file.

                      InputStream stream;
                      String path = any.getTextContent();
                      File file = new File(path);
                      if (file.exists()) {
                        stream = new FileInputStream(file);
                      } else {
                        //try possibly from resource root dir
                        file = new File(ToopSimulatorResources.SIMULATOR_CONFIG_DIR + path);
                        if (file.exists()) {
                          stream = new FileInputStream(file);
                        } else {
                          //file doesn't exist, try resource
                          stream = DiscoveryProvider.this.getClass().getResourceAsStream("/" + path);
                          if (stream == null) {
                            throw new IllegalStateException("A file or a classpath resource with name " + path + " was not found");
                          }
                        }
                      }
                      LOGGER.debug("FULL CERT PATH PARSE: " + file.getAbsolutePath());
                      x509Certificate[0] = (X509Certificate) certificateFactory.generateCertificate(stream);
                    }

                  }
                } catch (Exception ex) {
                  throw new IllegalStateException(ex.getMessage(), ex);
                }
              });

              //do we have a certificate ?
              if (x509Certificate[0] == null) {
                //no we don't, so load it form the default smp cert element
                x509Certificate[0] = (X509Certificate) certificateFactory.generateCertificate(new NonBlockingByteArrayInputStream(endpointType.getCertificate()));
              }

              ICommonsList<SMPEndpoint> list;

              SMPQuery smpQuery = new SMPQuery(participantID, documentTypeID, procID, transportProfile);
              if (smpMap.containsKey(smpQuery)) {
                list = smpMap.get(smpQuery);
              } else {
                list = new CommonsArrayList<>();
                smpMap.put(smpQuery, list);
              }

              list.add(new SMPEndpoint(participantID, endpointType.getTransportProfile(), endpointType.getEndpointURI(),
                  x509Certificate[0]));

            } catch (Exception ex) {
              LOGGER.error(ex.getMessage(), ex);
            }
          });
        });
      });

    });
  }

  @Nonnull
  @Override
  public ICommonsSortedMap<String, String> getAllServiceGroupHrefs(@Nonnull IParticipantIdentifier aParticipantID) {

    ICommonsSortedMap<String, String> map = new CommonsTreeMap<>();

    smpMap.keySet().forEach(key -> {

      if (key.aRecipientID.equals(aParticipantID)){


        //temporary

        String asmr  = querySMP(aParticipantID);

        //for (final ServiceMetadataReferenceType aSMR : aSG.getServiceMetadataReferenceCollection ().getServiceMetadataReference ())
        //{
        //  // Decoded href is important for unification
        //  final String sHref = CIdentifier.createPercentDecoded (aSMR.getHref ());
        //  if (ret.put (sHref, aSMR.getHref ()) != null)
        //    LOGGER.warn ("[API] The ServiceGroup list contains the duplicate URL '" + sHref + "'");
        //}
      }
    });
    return map;
  }

  private String querySMP(IParticipantIdentifier aParticipantID) {

    final SimpleURL aBaseURL = new SimpleURL("http://smp.helger.com/" + aParticipantID.getValue());

    final HttpClientSettings aHttpClientSettings = new HttpClientSettings();
    try (final HttpClientManager httpClient = HttpClientManager.create(aHttpClientSettings)) {
      final HttpGet aGet = new HttpGet(aBaseURL.getAsURI());

      try (final CloseableHttpResponse response = httpClient.execute(aGet)) {
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
          throw new IllegalStateException("Request failed " + response.getStatusLine().getStatusCode());
        }

        try (final NonBlockingByteArrayOutputStream stream = new NonBlockingByteArrayOutputStream()) {
          response.getEntity().writeTo(stream);
          final byte[] s_bytes = stream.toByteArray();
          if (LOGGER.isDebugEnabled()) {
            final String s_result = new String(s_bytes, StandardCharsets.UTF_8);
            LOGGER.debug("DSD result:\n" + s_result);
          }
          return new String(s_bytes);
        }
      }
    } catch (final RuntimeException ex) {
      throw ex;
    } catch (final Exception ex) {
      LOGGER.error(ex.getMessage(), ex);
      throw new IllegalStateException(ex);
    }
  }

  @Nullable
  @Override
  public ServiceMetadataType getServiceMetadata(@Nonnull IParticipantIdentifier aParticipantID, @Nonnull IDocumentTypeIdentifier aDocTypeID) {
    return serviceMetadataListType.getCountryAwareServiceMetadata().get(0).getServiceMetadata().get(0);
  }

  @Nonnull
  @Override
  public ICommonsSet<IParticipantIdentifier> getAllParticipantIDs(@Nonnull String sLogPrefix, @Nonnull String sDatasetType, @Nullable String sCountryCode, @Nonnull IDocumentTypeIdentifier aDocumentTypeID, @Nonnull IDDErrorHandler aErrorHandler) {


    LOGGER.info(sLogPrefix + "Query directory for [countryCode: " + sCountryCode +
        ", doctype: " + aDocumentTypeID.getURIEncoded() + "]");

    DIRQuery dirQuery = new DIRQuery(sCountryCode, aDocumentTypeID);

    if (directoryMap.containsKey(dirQuery)) {
      return directoryMap.get(dirQuery);
    }

    return EMPTY_PID_SET;

  }

  /**
   * A placeholder for a simple smp query, to play the KEY role in the SMP map.
   */
  class SMPQuery {
    private IParticipantIdentifier aRecipientID;
    private IDocumentTypeIdentifier aDocumentTypeID;
    private IProcessIdentifier aProcessID;
    private String sTransportProfileID;

    public SMPQuery(IParticipantIdentifier aRecipientID, IDocumentTypeIdentifier aDocumentTypeID, IProcessIdentifier aProcessID, String sTransportProfileID) {
      this.aRecipientID = aRecipientID;
      this.aDocumentTypeID = aDocumentTypeID;
      this.aProcessID = aProcessID;
      this.sTransportProfileID = sTransportProfileID;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SMPQuery smpQuery = (SMPQuery) o;
      return aRecipientID.equals(smpQuery.aRecipientID) &&
          aDocumentTypeID.equals(smpQuery.aDocumentTypeID) &&
          aProcessID.equals(smpQuery.aProcessID) &&
          sTransportProfileID.equals(smpQuery.sTransportProfileID);
    }

    @Override
    public int hashCode() {
      return Objects.hash(aRecipientID, aDocumentTypeID, aProcessID, sTransportProfileID);
    }
  }

  /**
   * A placeholder for a simple dir query, to play the KEY role in the directory map.
   */
  private class DIRQuery {
    private String sCountryCode;
    private IDocumentTypeIdentifier aDocumentTypeID;

    public DIRQuery(String sCountryCode, IDocumentTypeIdentifier aDocumentTypeID) {
      this.sCountryCode = sCountryCode;
      this.aDocumentTypeID = aDocumentTypeID;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DIRQuery dirQuery = (DIRQuery) o;
      return sCountryCode.equals(dirQuery.sCountryCode) &&
          aDocumentTypeID.equals(dirQuery.aDocumentTypeID);
    }

    @Override
    public int hashCode() {
      return Objects.hash(sCountryCode, aDocumentTypeID);
    }
  }

}
