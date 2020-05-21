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

import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.peppolid.IParticipantIdentifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.stream.Collectors;

public class DiscoveryTest {
  @Ignore
  @Test
  public void dsdTest(){
    final DiscoveryProvider instance = DiscoveryProvider.getInstance();

    final DiscoveryProvider.SMPQuery smpQuery = instance.smpMap.keySet().stream().collect(Collectors.toList()).get(0);
    final ICommonsSet<IParticipantIdentifier> allParticipantIDs = instance.getAllParticipantIDs("", "REGISTERED_ORGANIZATION", "SV", null, null);

    allParticipantIDs.forEach(ide -> {
      System.out.println(ide.getScheme() + "::" + ide.getValue());
    });
  }
}
