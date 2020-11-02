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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.impl.CommonsTreeMap;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.commons.collection.impl.ICommonsSortedMap;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.xsds.bdxr.smp1.DocumentIdentifierType;
import com.helger.xsds.bdxr.smp1.EndpointType;
import com.helger.xsds.bdxr.smp1.ParticipantIdentifierType;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;

import eu.toop.connector.api.dd.IDDServiceGroupHrefProvider;
import eu.toop.connector.api.dd.IDDServiceMetadataProvider;
import eu.toop.connector.api.dsd.DSDDatasetHelper;
import eu.toop.connector.api.dsd.DSDDatasetResponse;
import eu.toop.connector.api.dsd.IDSDDatasetResponseProvider;
import eu.toop.connector.api.error.ITCErrorHandler;
import eu.toop.dsd.api.DsdDataConverter;
import eu.toop.edm.error.EToopErrorCode;
import eu.toop.edm.jaxb.dcatap.DCatAPDatasetType;

/**
 * This class plays the role of both a directory and an SMP server. It reads its contents
 * from the file or classpath resource 'disovery-data.xml' and creates a map in the memory
 * to provide query results.
 *
 * @author yerlibilgin
 */
public class DiscoveryProvider implements IDDServiceGroupHrefProvider, IDDServiceMetadataProvider, IDSDDatasetResponseProvider {

  /**
   * The Logger instance
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryProvider.class);

  /**
   * The Instance.
   */
  static final DiscoveryProvider instance = new DiscoveryProvider();
  private byte[] resultListBytes;

  /**
   * Gets instance.
   *
   * @return the instance
   */
  public static DiscoveryProvider getInstance() {
    return instance;
  }


  private LinkedHashMap<ParticipantIdentifierType, LinkedHashMap<String, String>> hrefsMap = new LinkedHashMap<>();
  private LinkedHashMap<SMPServiceMetadataKey, ServiceMetadataType> serviceMetadataMap = new LinkedHashMap<>();

  private static final CommonsTreeMap EMPTY_MAP = new CommonsTreeMap<>();

  private DiscoveryProvider() {
    InputStream stream = this.getClass().getResourceAsStream("/discovery/directory.xml");
    resultListBytes = StreamHelper.getAllBytes(stream);

    Yaml yaml = new Yaml();
    stream = this.getClass().getResourceAsStream("/discovery/endpointhrefs.yml");
    ValueEnforcer.notNull(stream, "resource: /discovery/endpointhrefs.yml");
    hrefsMap = yaml.load(stream);

    stream = this.getClass().getResourceAsStream("/discovery/serviceMetadataTypes.yml");
    ValueEnforcer.notNull(stream, "resource: /discovery/serviceMetadataTypes.yml");
    serviceMetadataMap = yaml.load(stream);
  }

  @Nonnull
  @Override
  public ICommonsSortedMap<String, String> getAllServiceGroupHrefs(@Nonnull IParticipantIdentifier aParticipantID, @Nonnull ITCErrorHandler aErrorHandler) {
    ParticipantIdentifierType pId = createParticipantId(aParticipantID);

    if (hrefsMap.containsKey(pId)) {
      final LinkedHashMap<String, String> hrefsMapForPid = hrefsMap.get(pId);

      CommonsTreeMap<String, String> ret = new CommonsTreeMap<>();
      hrefsMapForPid.forEach((k, v) -> {
        ret.put(k, v);
      });


      LOGGER.error("Service Group Hrefs size " + ret.size());
      return ret;
    }

    LOGGER.error("Service Group Hrefs Empty");
    return EMPTY_MAP;
  }

  @Nullable
  @Override
  public ServiceMetadataType getServiceMetadata(@Nonnull IParticipantIdentifier aParticipantID, @Nonnull IDocumentTypeIdentifier aDocTypeID, @Nonnull IProcessIdentifier aProcessID, @Nonnull String sTransportProfile) {
    SMPServiceMetadataKey key = new SMPServiceMetadataKey(createParticipantId(aParticipantID), createDocTypeId(aDocTypeID));

    if (serviceMetadataMap.containsKey(key)) {
      LOGGER.debug("Found match " + key);
      return serviceMetadataMap.get(key);
    } else {
      LOGGER.error("No service metadata found for participant: " + aParticipantID.getScheme() + "::" + aParticipantID.getValue() +
          "    and doctypeid: " + aDocTypeID.getScheme() + "::" + aDocTypeID.getValue(), EToopErrorCode.GEN);
      return null;
    }
  }

  /**
   * @param sLogPrefix    The logging prefix to be used. May not be <code>null</code>.
   * @param sDatasetType  Dataset Type to query. May not be <code>null</code>.
   * @param sCountryCode  Country code to use. Must be a 2-digit string. May be
   *                      <code>null</code>.
   * @param aErrorHandler The error handler to be used. May not be <code>null</code>.
   * @return
   */
  @Nonnull
  public ICommonsSet<DSDDatasetResponse> getAllDatasetResponses(@Nonnull final String sLogPrefix,
                                                                @Nonnull final String sDatasetType,
                                                                @Nullable final String sCountryCode,
                                                                @Nonnull final ITCErrorHandler aErrorHandler) {

    String resultListXml = new String(resultListBytes, StandardCharsets.UTF_8);
    try {
      final String dsdXml = DsdDataConverter.convertDIRToDSDWithCountryCode(resultListXml, sDatasetType, sCountryCode);
      final List<DCatAPDatasetType> datasetTypes = DsdDataConverter.parseDataset(dsdXml);
      final ICommonsSet<DSDDatasetResponse> set = DSDDatasetHelper.buildDSDResponseSet(datasetTypes);
      LOGGER.debug("Size of dsd dataset response set " + set.size());
      return set;
    } catch (TransformerException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }

  }


  private ParticipantIdentifierType createParticipantId(IParticipantIdentifier aParticipantID) {
    ParticipantIdentifierType pId = new ParticipantIdentifierType();
    pId.setScheme(aParticipantID.getScheme());
    pId.setValue(aParticipantID.getValue());
    return pId;
  }


  private DocumentIdentifierType createDocTypeId(IDocumentTypeIdentifier aDoctypeId) {
    DocumentIdentifierType docId = new DocumentIdentifierType();
    docId.setScheme(aDoctypeId.getScheme());
    docId.setValue(aDoctypeId.getValue());
    return docId;
  }
}
