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
package eu.toop.simulator.web;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import com.helger.photon.api.IAPIRegistry;
import com.helger.photon.core.servlet.WebAppListener;

import eu.toop.connector.app.TCInit;
import eu.toop.connector.webapi.TCAPIInit;

/**
 * Global startup etc. listener.
 * 
 * @author yerlibilgin
 */
public class TSWebAppListener extends WebAppListener
{
  @Override
  protected void afterContextInitialized (final ServletContext aSC)
  {
    TCInit.initGlobally (aSC);
  }

  @Override
  protected void initAPI (@Nonnull final IAPIRegistry aAPIRegistry)
  {
    System.out.println("Init api");
    TCAPIInit.initAPI (aAPIRegistry);
  }

  @Override
  protected void beforeContextDestroyed (final ServletContext aSC)
  {
    TCInit.shutdownGlobally (aSC);
  }
}
