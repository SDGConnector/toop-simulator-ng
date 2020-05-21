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
package eu.toop.simulator.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.helger.commons.ValueEnforcer;

/**
 * This class parses an argument list with an optional main command
 * <p>
 * example:
 * <code>x509 -in cert.pem -inform PEM -out cert.der -outform DER</code> is
 * parsed as follows
 * <p>
 * MainCommand: x509<br>
 * Options: <br>
 * <ul>
 * <li>option: -in, arg: cert.pem</li>
 * <li>option: -inform, arg: PEM</li>
 * <li>option: -out, arg: cert.der</li>
 * <li>option: -outform, arg: DER</li>
 * </ul>
 *
 * @author Muhammet YILDIZ
 */
public class CliCommand
{
  /**
   * The main command, the first word in the list
   */
  private String mainCommand;

  /**
   * The map that associates an option with its parameters
   */
  private Map <String, List <String>> options;

  /**
   * Private constructor
   */
  private CliCommand ()
  {}

  /**
   * @return the actual command
   */
  public String getMainCommand ()
  {
    return mainCommand;
  }

  /**
   * Get the entire parameter list that is associated with the given option
   *
   * @param key
   *        the option
   * @return the parameter list or null (if the map is empty or key doesn't hit)
   */
  public List <String> getArguments (final String key)
  {
    if (options == null || options.size () == 0)
      return null;

    return options.get (key);
  }

  /**
   * Traverse the whole list and group with respect to options that start with a
   * dash.<br>
   * For example, the following words <br>
   * <code>sampleCommand optionwithoutdash1 optionwithoutdash2 -f file1 -q -t option1 option2 -c option3</code><br>
   * is parsed as a linked hash map as follows <br>
   *
   * <pre>
   *   mainCommand: sampleCommand,
   *   option map:
   *      "" -&gt; (optionwithoutdash1, optionwithoutdash2)
   *      f -&gt; (file1)
   *      q -&gt; ()
   *      t -&gt; (option1, option2)
   *      c -&gt; (option3)
   * </pre>
   *
   * @param words
   *        word list
   * @return A new command
   */
  public static CliCommand parse (final List <String> words)
  {
    return parse (words, false);
  }

  public static CliCommand parse (final List <String> words, final boolean hasMainCommand)
  {
    ValueEnforcer.notEmpty (words, "The word list cannot be null or empty");

    final CliCommand command = new CliCommand ();
    if (hasMainCommand)
      command.mainCommand = words.get (0);

    final int startIndex = hasMainCommand ? 1 : 0;

    if (words.size () > startIndex)
    {
      final Map <String, List <String>> options = new LinkedHashMap <> ();

      final int listSize = words.size ();
      String currentKey = ""; // empty key
      ArrayList <String> currentList = new ArrayList <> ();
      options.put (currentKey, currentList);

      for (int i = startIndex; i < listSize; ++i)
      {
        final String current = words.get (i);
        if (current.startsWith ("-"))
        {
          // skip dash
          currentKey = current.substring (1);
          currentList = new ArrayList <> ();
          options.put (currentKey, currentList);
        }
        else
        {
          // populate the current option
          currentList.add (current);
        }
      }
      command.options = options;
    }
    return command;
  }

  /**
   * @return the entire options map
   */
  public Map <String, List <String>> getOptions ()
  {
    return options;
  }

  /**
   * @return the parameters that don't have a leading option
   */
  public List <String> getEmptyParameters ()
  {
    return getArguments ("");
  }

  /**
   * @param key
   *        key to check
   * @return true if the options map contains the given <code>key</code>
   */
  public boolean hasOption (final String key)
  {
    if (options == null || options.isEmpty ())
      return false;

    return options.containsKey (key);
  }
}
