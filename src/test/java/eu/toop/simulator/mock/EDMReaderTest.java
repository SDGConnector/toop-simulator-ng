package eu.toop.simulator.mock;

import eu.toop.edm.IEDMTopLevelObject;
import eu.toop.edm.xml.EDMPayloadDeterminator;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class EDMReaderTest {

  @Test
  public void readQueryRequest() throws IOException {
    try (final InputStream inputStream = this.getClass().getResourceAsStream("/sample_query_request.xml")) {
      final IEDMTopLevelObject aTopLevel = EDMPayloadDeterminator.parseAndFind(inputStream);
    }
  }
}
