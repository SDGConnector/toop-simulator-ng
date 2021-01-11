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
package eu.toop.simulator.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * A class that helps autocomplete and suggestions for the command line interface
 */
public class SimulatorCliHelper {
  public static final String CMD_HELP = "help";
  public static final String CMD_SEND_DC_REQUEST = "send-dc-request";
  public static final String CMD_SEND_DP_RESPONSE = "send-dp-response";
  public static final String CMD_QUIT = "quit";
  /**
   * Read lines from the console, with input editing.
   */
  private final BufferedReader reader;
  /**
   * Formats date for displaying the prompt
   */
  private SimpleDateFormat sdf = new SimpleDateFormat("YYYY-mm-dd HH:MM:ss");


  private String prompt;
  /**
   * the last line read each time
   */
  private String lastLine;

  public SimulatorCliHelper() {
    reader = new BufferedReader(new InputStreamReader(System.in));
    prompt = "toop-simulator> ";
  }

  /**
   * Read one line from the command line
   *
   * @return boolean
   */
  public boolean readLine() {
    System.out.print(prompt);
    try {
      lastLine = reader.readLine();//prompt, sdf.format(Calendar.getInstance().getTime()), (MaskingCallback) null, null) != null;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    return true;
  }

  /**
   * get the list of the words (tokens) parsed from the cli
   *
   * @return words
   */
  public List<String> getWords() {
    //TODO: this does not consider parameters in quotes "
    if (lastLine == null){
      throw new NullPointerException("Command line empty. Probably no terminal");
    }

    return Arrays.asList(lastLine.split("\\s"));
  }
}
