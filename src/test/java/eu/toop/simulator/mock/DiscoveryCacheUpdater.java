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

import java.io.File;
import java.io.PrintWriter;
import java.util.LinkedHashMap;

import com.helger.peppolid.IDocumentTypeIdentifier;
import org.yaml.snakeyaml.Yaml;

import com.helger.commons.collection.impl.ICommonsSortedMap;
import com.helger.pd.searchapi.PDSearchAPIReader;
import com.helger.pd.searchapi.PDSearchAPIWriter;
import com.helger.pd.searchapi.v1.IDType;
import com.helger.pd.searchapi.v1.ResultListType;
import com.helger.peppol.smp.ESMPTransportProfile;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.xsds.bdxr.smp1.DocumentIdentifierType;
import com.helger.xsds.bdxr.smp1.ParticipantIdentifierType;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;

import eu.toop.connector.api.TCConfig;
import eu.toop.connector.api.error.LoggingTCErrorHandler;
import eu.toop.connector.app.smp.DDServiceGroupHrefProviderSMP;
import eu.toop.connector.app.smp.DDServiceMetadataProviderSMP;
import eu.toop.dsd.api.ToopDirClient;

/**
 * This class contains the logic for updating the simulator cache
 * for offline SMP and DSD mocking
 */
public class DiscoveryCacheUpdater {

  private final DDServiceGroupHrefProviderSMP hrefProviderSMP = new DDServiceGroupHrefProviderSMP();

  private final DDServiceMetadataProviderSMP serviceMetadataProviderSMP = new DDServiceMetadataProviderSMP();

  /**
   * Update the DSD cache from the TOOP Directory
   *
   * @return
   */
  public void updateDSDCache() throws Exception {

    String baseDir = "http://directory.acc.exchange.toop.eu";
    final String sSV = ToopDirClient.callSearchApiWithCountryCode(baseDir, "SV");
    final String sGQ = ToopDirClient.callSearchApiWithCountryCode(baseDir, "GQ");
    final String sPF = ToopDirClient.callSearchApiWithCountryCode(baseDir, "PF");
    final String sGF = ToopDirClient.callSearchApiWithCountryCode(baseDir, "GF");
    final ResultListType results = PDSearchAPIReader.resultListV1().read(sSV);
    results.getMatch().addAll(PDSearchAPIReader.resultListV1().read(sGQ).getMatch());
    results.getMatch().addAll(PDSearchAPIReader.resultListV1().read(sPF).getMatch());
    results.getMatch().addAll(PDSearchAPIReader.resultListV1().read(sGF).getMatch());
    System.out.println(results);
    PDSearchAPIWriter.resultListV1().setFormattedOutput(true).write(results, new File("src/main/resources/discovery/directory.xml"));
  }

  /**
   * Update the SMP cache from the configured SMP server.
   */
  public void updateSMPCache() throws Exception {
    //first read directory
    ResultListType results = PDSearchAPIReader.resultListV1().read(new File("src/main/resources/discovery/directory.xml"));


    LinkedHashMap<ParticipantIdentifierType, LinkedHashMap<String, String>> hrefsMap = new LinkedHashMap<>();
    LinkedHashMap<SMPServiceMetadataKey, ServiceMetadataType> serviceMetadataMap = new LinkedHashMap<>();

    results.getMatch().forEach(matchType -> {
      ParticipantIdentifierType pId = createParticipantId(matchType.getParticipantID());
      //get hrefs
      IParticipantIdentifier pIdToQuery = TCConfig.getIdentifierFactory().createParticipantIdentifier(pId.getScheme(), pId.getValue());
      final ICommonsSortedMap<String, String> hrefs = hrefProviderSMP.getAllServiceGroupHrefs(pIdToQuery, LoggingTCErrorHandler.INSTANCE);
      LinkedHashMap<String, String> javaMap = new LinkedHashMap<>(hrefs);
      hrefsMap.put(pId, javaMap);

      //get endpoints

      matchType.getDocTypeID().forEach(dId -> {
        DocumentIdentifierType docType = createDocTypeId(dId);
        final IDocumentTypeIdentifier docTypeID = TCConfig.getIdentifierFactory().createDocumentTypeIdentifier(docType.getScheme(), docType.getValue());
        final ServiceMetadataType serviceMetadata = serviceMetadataProviderSMP.getServiceMetadata(pIdToQuery,
            TCConfig.getIdentifierFactory().createDocumentTypeIdentifier(docType.getScheme(), docType.getValue()),
            TCConfig.getIdentifierFactory().createProcessIdentifier ("dummy-procid", "procid-dummy"),
            ESMPTransportProfile.TRANSPORT_PROFILE_BDXR_AS4.getID ());

        serviceMetadataMap.put(new SMPServiceMetadataKey(pId, docType),
            serviceMetadata);
      });
    });


    PrintWriter hrefsWriter = new PrintWriter(new File("src/main/resources/discovery/endpointhrefs.yml"));
    PrintWriter smdWriter = new PrintWriter(new File("src/main/resources/discovery/serviceMetadataTypes.yml"));

    Yaml yaml = new Yaml();

    String dump = yaml.dump(hrefsMap);
    hrefsWriter.println(dump);
    hrefsWriter.close();

    dump = yaml.dump(serviceMetadataMap);
    smdWriter.println(dump);
    smdWriter.close();
  }

  private static ParticipantIdentifierType createParticipantId(IDType idType) {
    ParticipantIdentifierType pId = new ParticipantIdentifierType();
    pId.setScheme(idType.getScheme());
    pId.setValue(idType.getValue());
    return pId;
  }


  private static DocumentIdentifierType createDocTypeId(IDType idType) {
    DocumentIdentifierType docId = new DocumentIdentifierType();
    docId.setScheme(idType.getScheme());
    docId.setValue(idType.getValue());
    return docId;
  }

  /**
   * Entry point
   *
   * @param args
   */
  public static void main(String[] args) throws Exception {
    final DiscoveryCacheUpdater updater = new DiscoveryCacheUpdater();
    updater.updateDSDCache();
    updater.updateSMPCache();
  }
}
