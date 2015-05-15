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

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@ServerEndpoint(value = "/websocket/{destination}")
public class WebsocketBridge {
	private final Logger log = Logger.getLogger(getClass().getName());
   @Resource
   ManagedExecutorService managedExecutorService;

	@OnOpen
	public void open(final Session session, @PathParam("destination") final String destination) {
		log.info("wsSession openend and bound to destination: " + destination);
		session.getUserProperties().put("destination", destination);
      StatusMonitor monitor = new StatusMonitor(session, destination);
      managedExecutorService.submit(monitor);
	}

	@OnMessage
	public void onMessage(String message, final Session session) {
		String destination = (String) session.getUserProperties().get("destination");
		try {
			for (Session s : session.getOpenSessions()) {
				if (s.isOpen()
						&& destination.equals(s.getUserProperties().get("destination"))) {
					s.getBasicRemote().sendText(message);
				}
			}
		} catch (IOException e) {
			log.warn("onMessage failed", e);
		}
	}

}
