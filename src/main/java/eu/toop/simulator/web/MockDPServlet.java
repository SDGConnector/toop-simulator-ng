package eu.toop.simulator.web;

import com.helger.commons.io.stream.StreamHelper;
import com.helger.peppolid.simple.doctype.SimpleDocumentTypeIdentifier;
import com.helger.peppolid.simple.participant.SimpleParticipantIdentifier;
import com.helger.peppolid.simple.process.SimpleProcessIdentifier;
import eu.toop.connector.api.me.incoming.IIncomingEDMResponse;
import eu.toop.connector.api.me.incoming.IncomingEDMResponse;
import eu.toop.connector.api.me.incoming.MEIncomingTransportMetadata;
import eu.toop.connector.api.me.outgoing.MEOutgoingException;
import eu.toop.connector.api.rest.TCIdentifierType;
import eu.toop.connector.api.rest.TCIncomingMessage;
import eu.toop.connector.api.rest.TCIncomingMetadata;
import eu.toop.connector.api.rest.TCRestJAXB;
import eu.toop.connector.app.incoming.MPTrigger;
import eu.toop.edm.EDMRequest;
import eu.toop.simulator.SimulatorConfig;
import eu.toop.simulator.mock.MockDP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet("/to-dp")
public class MockDPServlet extends HttpServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger(MockDPServlet.class);

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    byte[] bytes = StreamHelper.getAllBytes(req.getInputStream());

    LOGGER.info("DP Servlet got message:");
    final String sIncomingMessage = new String(bytes, StandardCharsets.UTF_8);
    LOGGER.info(sIncomingMessage);

    new Thread(() -> {
      final TCIncomingMessage tcIncomingMessage = TCRestJAXB.incomingMessage().read(sIncomingMessage);
      final TCIncomingMetadata metadata = tcIncomingMessage.getMetadata();

      LOGGER.info("DP Received Metadata: " + metadata);
      tcIncomingMessage.getPayload().forEach(tcPayload -> {
        LOGGER.info("DP Received Payload  Content ID: " + tcPayload.getContentID());
        LOGGER.info("DP Received Payload  Mime Type: " + tcPayload.getMimeType());

        final EDMRequest edmRequest = EDMRequest.reader().read(tcPayload.getValue());
        LOGGER.info("DP Received Payload:\n" + edmRequest.getWriter().getAsString());

        final TCIdentifierType receiverID = metadata.getReceiverID();
        final TCIdentifierType senderID = metadata.getSenderID();
        final TCIdentifierType docTypeID = metadata.getDocTypeID();
        final TCIdentifierType processID = metadata.getProcessID();
        MEIncomingTransportMetadata meIncomingTransportMetadata = new MEIncomingTransportMetadata(
            new SimpleParticipantIdentifier(senderID.getScheme(), senderID.getValue()),
            new SimpleParticipantIdentifier(receiverID.getScheme(), receiverID.getValue()),
            new SimpleDocumentTypeIdentifier(docTypeID.getScheme(), docTypeID.getValue()),
            new SimpleProcessIdentifier(processID.getScheme(), processID.getValue())
        );

        final IIncomingEDMResponse response = MockDP.eloniaCreateResponse(edmRequest, meIncomingTransportMetadata);
        final String sDestURL = "http://localhost:" + SimulatorConfig.connectorPort + "/api/user/submit/response";
        LOGGER.info("MOCKDPServlet sending back response to " + sDestURL);
        MockDP.buildAndSendResponse(response);
      });
    }).start();
    resp.setStatus(HttpServletResponse.SC_OK);
  }
}
