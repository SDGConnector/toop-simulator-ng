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
  public List <String> getOption(final String key)
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
          //if it is a NUMBER, then it is a value of the current option
          // skip dash
          String val = current.substring (1);

          if(val.matches("\\d+")){
            currentList.add (current);
          } else {
            currentList = new ArrayList<>();
            options.put(val, currentList);
          }
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
    return getOption("");
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
