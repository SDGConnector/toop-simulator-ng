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
import com.helger.commons.collection.impl.*;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.string.StringHelper;
import com.helger.pd.searchapi.PDSearchAPIReader;
import com.helger.pd.searchapi.v1.EntityType;
import com.helger.pd.searchapi.v1.IDType;
import com.helger.pd.searchapi.v1.MatchType;
import com.helger.pd.searchapi.v1.ResultListType;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.xsds.bdxr.smp1.DocumentIdentifierType;
import com.helger.xsds.bdxr.smp1.ParticipantIdentifierType;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;
import eu.toop.connector.api.TCConfig;
import eu.toop.connector.api.dd.IDDErrorHandler;
import eu.toop.connector.api.dd.IDDServiceGroupHrefProvider;
import eu.toop.connector.api.dd.IDDServiceMetadataProvider;
import eu.toop.connector.api.dsd.DSDDatasetResponse;
import eu.toop.connector.api.dsd.IDSDDatasetResponseProvider;
import eu.toop.dsd.client.BregDCatHelper;
import eu.toop.dsd.client.types.DoctypeParts;
import eu.toop.edm.jaxb.cv.agent.PublicOrganizationType;
import eu.toop.edm.jaxb.dcatap.DCatAPDatasetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

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
  public ICommonsSortedMap<String, String> getAllServiceGroupHrefs(@Nonnull IParticipantIdentifier aParticipantID) {
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
      LOGGER.debug("Not Found match " + key);
      return null; //TODO: return null or throw?
    }
  }

  /**
   * TODO: Most of the code is copy here. Need to write a smarter mock. Lotsa refactors needed.
   *
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
                                                                @Nonnull final IDDErrorHandler aErrorHandler) {
    final ICommonsSet<DSDDatasetResponse> ret = new CommonsHashSet<>();

    final ResultListType resultList = PDSearchAPIReader.resultListV1().read(resultListBytes);

    List<MatchType> directoryList = resultList.getMatch();
    filterDirectoryResult(sDatasetType, sCountryCode, directoryList);
    final List<Document> documents = BregDCatHelper.convertMatchTypesToDCATDocuments(sDatasetType, directoryList);
    final List<Element> collect = documents.stream().map(doc -> doc.getDocumentElement()).collect(Collectors.toList());
    final List<DCatAPDatasetType> dCatAPDatasetTypes = BregDCatHelper.convertElementsToDCatList(collect);

    dCatAPDatasetTypes.forEach(d -> {
      d.getDistribution().forEach(dist -> {
        final DSDDatasetResponse resp = new DSDDatasetResponse();
        // Access Service Conforms To
        if (dist.getAccessService().hasConformsToEntries())
          resp.setAccessServiceConforms(dist.getAccessService().getConformsToAtIndex(0).getValue());

        // DP Identifier
        final eu.toop.edm.jaxb.cv.cbc.IDType aDPID = ((PublicOrganizationType) d.getPublisherAtIndex(0)).getIdAtIndex(0);
        resp.setDPIdentifier(TCConfig.getIdentifierFactory().createParticipantIdentifier(aDPID.getSchemeName(), aDPID.getValue()));

        // Access Service Identifier, used as Document Type ID
        final ICommonsList<String> aDTParts = StringHelper.getExploded("::", dist.getAccessService().getIdentifier(), 2);
        if (aDTParts.size() == 2)
          resp.setDocumentTypeIdentifier(TCConfig.getIdentifierFactory()
              .createDocumentTypeIdentifier(aDTParts.get(0), aDTParts.get(1)));

        resp.setDatasetIdentifier(d.getIdentifierAtIndex(0));
        if (dist.hasConformsToEntries())
          resp.setDistributionConforms(dist.getConformsToAtIndex(0).getValue());

        resp.setDistributionFormat(dist.getFormat().getContentAtIndex(0).toString());
        ret.add(resp);
      });
    });

    LOGGER.debug("List size " + ret.size());
    return ret;
  }


  /**
   * TODO: this is copied from matchtypewriter. Need to refactor in the
   * next release
   * <p>
   * TODO: not a good code. Modifies the underlying lists as well.
   * <p>
   * This is a tentative approach. We filter out match types as following:<br>
   * <pre>
   *   for each matchtype
   *     for each doctype of that matchtype
   *       remote the doctype if it does not contain datasetType
   *     if all doctypes were removed
   *        then remove the matchtype
   * </pre>
   *
   * @param s_datasetType Dataset type
   * @param sCountryCode  country code
   * @param matchTypes    Match types
   */
  public static void filterDirectoryResult(String s_datasetType, String sCountryCode, List<MatchType> matchTypes) {
    //filter
    final Iterator<MatchType> iterator = matchTypes.iterator();

    while (iterator.hasNext()) {
      MatchType matchType = iterator.next();
      final Iterator<IDType> iterator1 = matchType.getDocTypeID().iterator();
      while (iterator1.hasNext()) {
        IDType idType = iterator1.next();
        String concatenated = BregDCatHelper.flattenIdType(idType);

        DoctypeParts parts = DoctypeParts.parse(concatenated);

        // TODO: This is temporary, for now we are removing _ (underscore) and performing a case insensitive "contains" search

        //first check for the EXACT match

        //  ignore cases and underscores (CRIMINAL_RECORD = criminalRecord)
        if (!concatenated.replaceAll("_", "").toLowerCase()
            .contains(s_datasetType.replaceAll("_", "").toLowerCase())) {
          iterator1.remove();
        }
      }

      // if all doctypes have been removed then, eliminate this business card
      if (matchType.getDocTypeID().size() == 0) {
        iterator.remove();
        continue;
      }

      if (sCountryCode != null) {
        final List<EntityType> entity = matchType.getEntity();
        final Iterator<EntityType> iterator2 = entity.iterator();
        while (iterator2.hasNext()) {
          EntityType entityType = iterator2.next();
          if (!entityType.getCountryCode().equals(sCountryCode)) {
            iterator2.remove();
          }
        }

        if (matchType.getEntity().isEmpty()) {
          iterator.remove();
        }
      }
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
