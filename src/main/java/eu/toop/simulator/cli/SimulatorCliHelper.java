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

import org.jline.builtins.Completers;
import org.jline.reader.*;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * A class that helps autocomplete and suggestions for the command line interface
 */
public class SimulatorCliHelper {
  public static final String CMD_HELP = "help";
  public static final String CMD_SEND_DC_REQUEST = "send-dc-request";
  public static final String CMD_QUIT = "quit";
  /**
   * Read lines from the console, with input editing.
   */
  private final LineReader reader;
  /**
   * Formats date for displaying the prompt
   */
  private SimpleDateFormat sdf = new SimpleDateFormat("YYYY-mm-dd HH:MM:ss");


  private String prompt;

  public SimulatorCliHelper(){
    this("toop-simulator> ",
        ".tshistory",
        Arrays.asList(CMD_HELP, CMD_SEND_DC_REQUEST,  CMD_QUIT));
  }
  /**
   * Default constructor
   */
  public SimulatorCliHelper(String prompt, String historyFile, List<String> stringList) {
    this.prompt = prompt;

    TerminalBuilder builder = TerminalBuilder.builder();

    Completer second = new SecondCompleter();

    ArgumentCompleter completer = new ArgumentCompleter(
        new StringsCompleter(stringList),
        second
    );

    completer.setStrict(false);

    Parser parser = null;

    Terminal terminal;
    try {
      terminal = builder.build();
    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }

    DefaultHistory defaultHistory = new DefaultHistory();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        defaultHistory.save();
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }));

    reader = LineReaderBuilder.builder()
        .terminal(terminal).variable(LineReader.HISTORY_FILE, new File(historyFile))
        .completer(completer)
        .parser(parser).history(defaultHistory)
        .build();

  }

  /**
   * Read one line from the command line
   *
   * @return boolean
   */
  public boolean readLine() {
    boolean b = reader.readLine(prompt, sdf.format(Calendar.getInstance().getTime()), (MaskingCallback) null, null) != null;
    return b;
  }

  /**
   * get the list of the words (tokens) parsed from the cli
   *
   * @return words
   */
  public List<String> getWords() {
    return reader.getParsedLine().words();
  }


  /**
   * Second level auto completer helper (running after the primary command)
   */
  private static class SecondCompleter implements Completer {
    /**
     * The Send completer.
     */
    SendCompleter sendCompleter = new SendCompleter();
    /**
     * The File name completer.
     */
    Completers.FileNameCompleter fileNameCompleter = new Completers.FileNameCompleter();

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
      //check the command
      String command = line.words().get(0);
      switch (command) {
        case CMD_HELP:
        case CMD_QUIT:
          break;

        case CMD_SEND_DC_REQUEST:
          sendCompleter.complete(reader, line, candidates);
          break;
      }
    }
  }

  private static class SendCompleter implements Completer {
    private List<Candidate> sendNewMessageCandidates = new ArrayList<>();
    /**
     * The File name completer.
     */
    Completers.FileNameCompleter fileNameCompleter = new Completers.FileNameCompleter();

    /**
     * Instantiates a new Send completer.
     */
    public SendCompleter() {
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
      if (line.wordIndex() == 0) {
        return;
      }

      if (line.wordIndex() == 1) {
        candidates.add(new Candidate("-f", "-f", "", "send existing file", null, null, false));
      } else if (line.wordIndex() == 2) {
        String arg = line.words().get(1);

        switch (arg) {
          case "-f":
            fileNameCompleter.complete(reader, line, candidates);
            break;
        }
      } else {
        if (line.words().get(1).equals("-f")) {
          fileNameCompleter.complete(reader, line, candidates);
          return;
        }

        if (line.wordIndex() % 2 == 0 && line.words().get(1).equals("-new")) {
          candidates.addAll(sendNewMessageCandidates);
        }
      }

    }
  }
}
