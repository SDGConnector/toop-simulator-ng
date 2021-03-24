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
package eu.toop.simulator.web;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.mime.CMimeType;

import eu.toop.connector.api.me.IMessageExchangeSPI;
import eu.toop.connector.api.me.MessageExchangeManager;
import eu.toop.connector.app.CTC;

/**
 * Servlet for handling the initial calls without any path. This servlet
 * redirects to "/index.html".
 *
 * @author Philip Helger
 */
@WebServlet("")
public class TSRootServlet extends HttpServlet {
  @Override
  protected void doGet(@Nonnull final HttpServletRequest req, @Nonnull final HttpServletResponse resp) throws ServletException, IOException {
    final String sContextPath = req.getServletContext().getContextPath();
    final String sCSS = "* { font-family: sans-serif; }" +
      " a:link, a:visited, a:hover, a:active { color: #2255ff; }" +
      " code { font-family:monospace; color:#e83e8c; }";

    final StringBuilder aSB = new StringBuilder();
    aSB.append("<html><head><title>TOOP Connector NG</title><style>").append(sCSS).append("</style></head><body>");
    aSB.append("<h1>TOOP Connector NG</h1>");
    aSB.append("<div>Version: ").append(CTC.getVersionNumber()).append("</div>");
    aSB.append("<div>Build timestamp: ").append(CTC.getBuildTimestamp()).append("</div>");
    aSB.append("<div>Current time: ").append(PDTFactory.getCurrentZonedDateTimeUTC().toString()).append("</div>");
    aSB.append("<div><a href='tc-status'>Check /tc-status</a></div>");

    {
      aSB.append("<h2>Registered Message Exchange implementations</h2>");
      for (final Map.Entry<String, IMessageExchangeSPI> aEntry : CollectionHelper.getSortedByKey(MessageExchangeManager.getAll())
        .entrySet()) {
        aSB.append("<div>ID <code>").append(aEntry.getKey()).append("</code> mapped to ").append(aEntry.getValue()).append("</div>");
      }
    }

    // if (GlobalDebug.isDebugMode ())
    {
      aSB.append("<h2>servlet information</h2>");
      for (final Map.Entry<String, ? extends ServletRegistration> aEntry : CollectionHelper.getSortedByKey(req.getServletContext()
        .getServletRegistrations())
        .entrySet()) {
        aSB.append("<div>Servlet <code>")
          .append(aEntry.getKey())
          .append("</code> mapped to ")
          .append(aEntry.getValue().getMappings())
          .append("</div>");
      }
    }

    // APIs
    {
      aSB.append("<h2>API information</h2>");

      aSB.append("<h3>DSD</h3>");
      aSB.append("<div>GET /api/dsd/dp/by-country - <a href='" +
        sContextPath +
        "/api/dsd/dp/REGISTERED_ORGANIZATION_TYPE/by-country/SV' target='_blank'>test me</a></div>");
      // New in 2.1.0
      aSB.append("<div>GET /api/dsd/dp/by-dp-type - <a href='" +
        sContextPath +
        "/api/dsd/dp/REGISTERED_ORGANIZATION_TYPE/by-dp-type/RO' target='_blank'>test me</a></div>");

      aSB.append("<h3>SMP</h3>");
      aSB.append("<div>GET /api/smp/doctypes - <a href='" +
        sContextPath +
        "/api/smp/doctypes/iso6523-actorid-upis%3A%3A9915%3Atooptest' target='_blank'>test me</a></div>");
      aSB.append("<div>GET /api/smp/endpoints - <a href='" +
        sContextPath +
        "/api/smp/endpoints/iso6523-actorid-upis%3A%3A9915%3Atooptest/toop-doctypeid-qns%3A%3ARegisteredOrganization%3A%3AREGISTERED_ORGANIZATION_TYPE%3A%3ACONCEPT%23%23CCCEV%3A%3Atoop-edm%3Av2.0' target='_blank'>test me</a></div>");

      aSB.append("<h3>Validation</h3>");
      aSB.append("<div>POST /api/validate/request</div>");
      aSB.append("<div>POST /api/validate/response</div>");
      aSB.append("<div>POST /api/validate/error</div>");

      aSB.append("<h3>AS4</h3>");
      aSB.append("<div>POST /api/send</div>");

      aSB.append("<h3>Utilities</h3>");
      aSB.append("<div>POST /api/user/submit/request</div>");
      aSB.append("<div>POST /api/user/submit/response</div>");
      aSB.append("<div>POST /api/user/submit/error</div>");
    }

    aSB.append("</body></html>");

    resp.addHeader(CHttpHeader.CONTENT_TYPE, CMimeType.TEXT_HTML.getAsString());
    resp.getWriter().write(aSB.toString());
    resp.getWriter().flush();
  }
}
