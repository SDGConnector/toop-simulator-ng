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

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.yaml.snakeyaml.Yaml;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.impl.CommonsTreeMap;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.commons.collection.impl.ICommonsSortedMap;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.pd.searchapi.PDSearchAPIReader;
import com.helger.pd.searchapi.v1.MatchType;
import com.helger.pd.searchapi.v1.ResultListType;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.xsds.bdxr.smp1.DocumentIdentifierType;
import com.helger.xsds.bdxr.smp1.ParticipantIdentifierType;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;

import eu.toop.connector.api.dd.IDDServiceGroupHrefProvider;
import eu.toop.connector.api.dd.IDDServiceMetadataProvider;
import eu.toop.connector.api.dsd.DSDDatasetHelper;
import eu.toop.connector.api.dsd.DSDDatasetResponse;
import eu.toop.connector.api.dsd.IDSDDatasetResponseProvider;
import eu.toop.connector.api.error.ITCErrorHandler;
import eu.toop.dsd.api.DSDTypesManipulator;
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
  public ServiceMetadataType getServiceMetadata(@Nonnull IParticipantIdentifier aParticipantID, @Nonnull IDocumentTypeIdentifier aDocTypeID) {
    SMPServiceMetadataKey key = new SMPServiceMetadataKey(createParticipantId(aParticipantID), createDocTypeId(aDocTypeID));

    if (serviceMetadataMap.containsKey(key)) {
      LOGGER.debug("Found match " + key);
      return serviceMetadataMap.get(key);
    } else {
      LOGGER.error ("No service metadata found for participant: " + aParticipantID.getScheme() + "::" + aParticipantID.getValue() +
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
    final ResultListType resultList = PDSearchAPIReader.resultListV1().read(resultListBytes);

    List<MatchType> directoryList = resultList.getMatch();
    DSDTypesManipulator.filterDirectoryResults(sDatasetType, sCountryCode, directoryList);
    final List<Document> documents = DSDTypesManipulator.convertMatchTypesToDCATDocuments(sDatasetType, directoryList);
    final List<Element> collect = documents.stream().map(doc -> doc.getDocumentElement()).collect(Collectors.toList());
    final List<DCatAPDatasetType> dCatAPDatasetTypes = DSDTypesManipulator.convertElementsToDCatList(collect);

    final ICommonsSet<DSDDatasetResponse> set = DSDDatasetHelper.buildDSDResponseSet(dCatAPDatasetTypes);
    LOGGER.debug("Size of dsd dataset response set " + set.size());
    return set;
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
