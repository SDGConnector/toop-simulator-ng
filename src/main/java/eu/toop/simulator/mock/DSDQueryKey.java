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
