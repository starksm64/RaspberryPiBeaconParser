package org.jboss.summit2015.replay;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@WebServlet(description = "JMS Publisher Servlet", urlPatterns = {"/MessageServlet"},
   initParams = {@WebInitParam(name = "id", value = "1")})
public class MessageServlet extends HttpServlet {
   private static final Logger log = Logger.getLogger(MessageServlet.class);

   @Inject
   MsgPublisher publisher;

   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      log.infof("init, id=%s", config.getInitParameter("id"));
   }

   /**
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
    */
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      PrintWriter out = response.getWriter();
      Date date = new Date();
      try {
         publisher.publishTestMessage();
      } catch (JMSException e) {
         throw new ServletException(e);
      }
      out.println("<html><body><h2>JMS Test Message Posted</h2><br/><h3>Date=" + date + "</h3></body></html>");
   }

   /**
    * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // TODO Auto-generated method stub
   }
}
