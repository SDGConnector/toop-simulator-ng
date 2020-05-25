package eu.toop.simulator.web;

import com.helger.commons.io.stream.StreamHelper;
import eu.toop.connector.api.rest.TCIncomingMessage;
import eu.toop.connector.api.rest.TCIncomingMetadata;
import eu.toop.connector.api.rest.TCRestJAXB;
import eu.toop.edm.EDMResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet("/to-dc")
public class MockDCServlet extends HttpServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger(MockDCServlet.class);

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    byte []bytes = StreamHelper.getAllBytes(req.getInputStream());

    LOGGER.info("DC Servlet got message:");
    final String sIncomingMessage = new String(bytes, StandardCharsets.UTF_8);
    LOGGER.info(sIncomingMessage);

    final TCIncomingMessage tcIncomingMessage = TCRestJAXB.incomingMessage().read(sIncomingMessage);
    final TCIncomingMetadata metadata = tcIncomingMessage.getMetadata();

    LOGGER.info("DC Received Metadata: " + metadata);
    tcIncomingMessage.getPayload().forEach(tcPayload -> {
      LOGGER.info("DC Received Payload  Content ID: " + tcPayload.getContentID());
      LOGGER.info("DC Received Payload  Mime Type: " + tcPayload.getMimeType());
      final EDMResponse edmResponse = EDMResponse.reader().read(tcPayload.getValue());
      LOGGER.info("DC Received Payload:\n" + edmResponse.getWriter().getAsString());
    });
    resp.setStatus(HttpServletResponse.SC_OK);
  }
}
