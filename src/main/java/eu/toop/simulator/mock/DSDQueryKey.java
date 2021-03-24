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

import java.util.Objects;

/**
 * This class is used as a KEY for the DSD queries.
 */
public class DSDQueryKey {
  private String datasetType;
  private String countryCode;

  /**
   * Instantiates a new Dsd query key.
   */
  public DSDQueryKey() {
  }

  /**
   * Instantiates a new Dsd query key.
   *
   * @param datasetType the dataset type
   * @param countryCode the country code
   */
  public DSDQueryKey(String datasetType, String countryCode) {
    this.datasetType = datasetType;
    this.countryCode = countryCode;
  }

  /**
   * Gets dataset type.
   *
   * @return the dataset type
   */
  public String getDatasetType() {
    return datasetType;
  }

  /**
   * Sets dataset type.
   *
   * @param datasetType the dataset type
   */
  public void setDatasetType(String datasetType) {
    this.datasetType = datasetType;
  }

  /**
   * Gets country code.
   *
   * @return the country code
   */
  public String getCountryCode() {
    return countryCode;
  }

  /**
   * Sets country code.
   *
   * @param countryCode the country code
   */
  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DSDQueryKey that = (DSDQueryKey) o;
    return Objects.equals(datasetType, that.datasetType) &&
        Objects.equals(countryCode, that.countryCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(datasetType, countryCode);
  }
}
