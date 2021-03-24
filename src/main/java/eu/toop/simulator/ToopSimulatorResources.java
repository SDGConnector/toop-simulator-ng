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

import java.io.File;
import java.net.URL;

/**
 * This class manages and serves the paths for resources that are being used by the simulator.
 *
 * @author yerlibilgin
 */
public class ToopSimulatorResources {

  private static final String DEFAULT_CONFIG_RESOURCE = "/toop-simulator.conf";
  private static final String DEFAULT_DISCOVERY_DATA_RESOURCE = "/discovery-data.xml";

  /**
   * Returns the <code>toop-simulator.conf</code> resource URL
   * @return the <code>toop-simulator.conf</code> resource URL
   */
  public static URL getSimulatorConfResource() {
    return ToopSimulatorResources.class.getResource(DEFAULT_CONFIG_RESOURCE);
  }

  /**
   * Returns the <code>discovery-data.xml</code> resource URL
   * @return the <code>discovery-data.xml</code> resource URL
   */
  public static URL getDiscoveryDataResURL() {
    return ToopSimulatorResources.class.getResource(DEFAULT_DISCOVERY_DATA_RESOURCE);
  }

  /**
   * Copy the toop-simulator.conf and discovery-data.xml from classpath
   * to the current directory, so that the user can edit them without
   * dealing with the jar file. <br>
   * Don't touch if they exist
   */
  public static void transferResourcesToFileSystem() {
    new File("datasets/gbm").mkdirs();
//    new File("datasets/document").mkdirs();
    new File("datasets/document/attachments").mkdirs();
    Util.transferResourceToDirectory("datasets/document/example.yaml", "datasets/document");
    Util.transferResourceToDirectory("datasets/document/example_LP.yaml", "datasets/document");
    Util.transferResourceToDirectory("datasets/document/example_LP_JPEG.yaml", "datasets/document");
    Util.transferResourceToDirectory("datasets/document/a852053e-ba86-4690-86cf-7190a2f5d915.yaml", "datasets/document");
    Util.transferResourceToDirectory("datasets/document/516159d1-6d92-4806-b9ee-0d3b7d8996b9.yaml", "datasets/document");
    Util.transferResourceToDirectory("datasets/document/622de68c-11a8-4006-adbf-4520f40693d0.yaml", "datasets/document");
    Util.transferResourceToDirectory("datasets/document/26bd0e98-108a-4d39-8553-d4f5693fcd41.yaml", "datasets/document");
    Util.transferResourceToDirectory("datasets/document/03e287f7-7500-446b-b360-1db78ffb2800.yaml", "datasets/document");
    Util.transferResourceToDirectory("datasets/document/03a74657-c4c9-4a26-92ac-abf37e36c6ae.yaml", "datasets/document");
    Util.transferResourceToDirectory("datasets/gbm/example_LP.yaml", "datasets/gbm");
    Util.transferResourceToDirectory("datasets/gbm/example_LP_LR.yaml", "datasets/gbm");
    Util.transferResourceToDirectory("datasets/gbm/example_NP.yaml", "datasets/gbm");
    Util.transferResourceToDirectory("datasets/edm-conceptRequest-lp.xml", "datasets");
    Util.transferResourceToDirectory("datasets/edm-conceptResponse-lp.xml", "datasets");

    Util.transferResourceToDirectory("datasets/document/attachments/a852053e-ba86-4690-86cf-7190a2f5d915.pdf", "datasets/document/attachments");
    Util.transferResourceToDirectory("datasets/document/attachments/516159d1-6d92-4806-b9ee-0d3b7d8996b9.pdf", "datasets/document/attachments");
    Util.transferResourceToDirectory("datasets/document/attachments/622de68c-11a8-4006-adbf-4520f40693d0.pdf", "datasets/document/attachments");
    Util.transferResourceToDirectory("datasets/document/attachments/26bd0e98-108a-4d39-8553-d4f5693fcd41.pdf", "datasets/document/attachments");
    Util.transferResourceToDirectory("datasets/document/attachments/03e287f7-7500-446b-b360-1db78ffb2800.pdf", "datasets/document/attachments");
    Util.transferResourceToDirectory("datasets/document/attachments/03a74657-c4c9-4a26-92ac-abf37e36c6ae.pdf", "datasets/document/attachments");
    Util.transferResourceToDirectory("datasets/document/attachments/dummy.pdf", "datasets/document/attachments");
    Util.transferResourceToDirectory("datasets/document/attachments/dummy.jpg", "datasets/document/attachments");
  }
}
