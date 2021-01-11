/**
 * Copyright (C) 2018-2021 toop.eu. All rights reserved.
 *
 * This project is dual licensed under Apache License, Version 2.0
 * and the EUPL 1.2.
 *
 *  = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.Test;

import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.commons.error.level.EErrorLevel;

import eu.toop.connector.api.dsd.DSDDatasetResponse;
import eu.toop.connector.api.error.ITCErrorHandler;
import eu.toop.edm.error.IToopErrorCode;

public class DiscoveryTest {

  @Test
  public void dsdTestByCountry(){
    System.out.println("true");

    DiscoveryProvider discoveryProvider = DiscoveryProvider.getInstance();

    final ICommonsSet<DSDDatasetResponse> responses = discoveryProvider.getAllDatasetResponsesByCountry("", "REGISTERED_ORGANIZATION_TYPE", "SV", new ITCErrorHandler() {
      @Override
      public void onMessage(@Nonnull EErrorLevel eErrorLevel, @Nonnull String sMsg, @Nullable Throwable t, @Nonnull IToopErrorCode eCode) {
        System.err.println(sMsg);
      }
    });

    responses.forEach(resp->{
      System.out.println(resp.getAsJson().getAsJsonString());
    });
  }

  @Test
  public void dsdTestByDPType(){
    System.out.println("true");

    DiscoveryProvider discoveryProvider = DiscoveryProvider.getInstance();

    final ICommonsSet<DSDDatasetResponse> responses = discoveryProvider.getAllDatasetResponsesByDPType("", "REGISTERED_ORGANIZATION_TYPE", "GLN", new ITCErrorHandler() {
      @Override
      public void onMessage(@Nonnull EErrorLevel eErrorLevel, @Nonnull String sMsg, @Nullable Throwable t, @Nonnull IToopErrorCode eCode) {
        System.err.println(sMsg);
      }
    });

    responses.forEach(resp->{
      System.out.println(resp.getAsJson().getAsJsonString());
    });
  }
}
