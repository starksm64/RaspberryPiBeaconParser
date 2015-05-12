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

import com.google.gson.JsonObject;
import org.jboss.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@Path("/")
public class ReplayService implements ReplayCompleteCallback {
   private static final Logger log = Logger.getLogger(ReplayService.class);
   private static ConcurrentHashMap<String, ReplayManager> managerTasks = new ConcurrentHashMap<>();

   @Resource
   ManagedExecutorService managedExecutorService;
   /*
   @Inject
   Instance<ReplayManager> replayManagers;
   */

   @GET
   @Path("replaystream/{dataSet}")
   @Produces({"text/html"})
   public StreamingOutput replayDataStream(@DefaultValue("SevenScannersRun-2015-05-11.json.gz") @PathParam("dataSet") String dataSet) {
      return new StreamingOutput() {
         public void write(OutputStream outputStream) throws IOException, WebApplicationException {
            JsonEventStream eventStream = new JsonEventStream();
            try {
               int count = 0;
               long begin = System.currentTimeMillis();
               StringBuilder response = new StringBuilder("<html><title>ReplayService.replayData, "+new Date()+"</title><body><h1>dataSet=");
               response.append(dataSet);
               response.append("; Begin, start=%s\n");
               response.append(new Date(begin));
               response.append("</h1>");
               response.append("<pre>");
               outputStream.write(response.toString().getBytes());
               outputStream.flush();
               log.info(response);
               eventStream.init(dataSet);
               MsgPublisher publisher = new MsgPublisher();
               publisher.init();
               while (eventStream.hasNext()) {
                  JsonObject event = eventStream.next();
                  publisher.publishEvent(event);
                  count ++;
                  if((count % 1000) == 0) {
                     String msg = String.format("count=%s, time=%s\n", count, new Date());
                     outputStream.write(msg.getBytes());
                     outputStream.flush();
                     log.info(msg);
                     publisher.commit();
                  }
               }
               publisher.close();
               eventStream.close();
               long end = System.currentTimeMillis();
               String msg = String.format("Done, elapsed = %d seconds\n", (end - begin));
               outputStream.write(msg.getBytes());
               outputStream.flush();
               log.info(msg);
            } catch (Exception e) {
               throw new WebApplicationException(e);
            }
         }
      };
   }

   @GET
   @Path("replay/{dataSet}")
   @Produces({"text/html"})
   public String replayData(@DefaultValue("SevenScannersRun-2015-05-11.json.gz") @PathParam("dataSet") PathSegment dataSet) {

      log.infof("Begin replayData");
      String dataSetName = dataSet.getPath();
      float speedup = 1;
      String speedupParam = dataSet.getMatrixParameters().getFirst("speedup");
      if(speedupParam != null)
         speedup = Float.parseFloat(speedupParam);

      ReplayManager replayManager = null;
      try {
         log.infof("Creating raw ReplayManager\n");
         replayManager = new ReplayManager();
         log.infof("Created raw ReplayManager\n");
         replayManager.setDataSet(dataSetName);
         replayManager.setSpeedup(speedup);
         replayManager.setCompleteCallback(this);
         String queue = dataSet.getMatrixParameters().getFirst("queue");
         if (queue != null)
            replayManager.setQueueName(queue);
         String limitCountParam = dataSet.getMatrixParameters().getFirst("limitCount");
         if (limitCountParam != null)
            replayManager.setLimitCount(Integer.parseInt(limitCountParam));
         replayManager.connect();

         Future future = managedExecutorService.submit(replayManager);
         replayManager.setFuture(future);
         log.infof("Submitted replay manager task: %s", replayManager);
         managerTasks.put(replayManager.getID(), replayManager);
      } catch (Throwable t) {
         log.error("Failed to initialize replay manager", t);
      }

      StringBuilder response = new StringBuilder("<html><title>ReplayService.replayData, "+new Date()+"</title><body><h1>dataSet=");
      response.append(dataSet.getPath());
      response.append("</h1>");
      response.append("<h2>Path Parameters</h2><ul>");
      for (String key : dataSet.getMatrixParameters().keySet()) {
         response.append("<li>");
         response.append(key);
         response.append("=");
         response.append(dataSet.getMatrixParameters().getFirst(key));
         response.append("</li>");
      }
      response.append("</ul>");
      response.append("<pre>");
      response.append(replayManager);
      response.append("</pre>");
      response.append("</body></html>");
      return response.toString();
   }

   @GET
   @Path("replayandwait/{dataSet}")
   @Produces({"text/html"})
   public StreamingOutput replayDataAndWait(@DefaultValue("SevenScannersRun-2015-05-11.json.gz") @PathParam("dataSet") PathSegment dataSet) {

      log.infof("Begin replayandwait");
      String dataSetName = dataSet.getPath();
      float speedup = 1;
      String speedupParam = dataSet.getMatrixParameters().getFirst("speedup");
      if(speedupParam != null)
         speedup = Float.parseFloat(speedupParam);

      ReplayManager replayManager = null;
      StreamingOutput result = null;
      try {
         log.infof("Creating raw ReplayManager\n");
         replayManager = new ReplayManager();
         log.infof("Created raw ReplayManager\n");
         replayManager.setDataSet(dataSetName);
         replayManager.setSpeedup(speedup);
         replayManager.setCompleteCallback(this);
         result = replayManager.getStreamingOutput();
         String queue = dataSet.getMatrixParameters().getFirst("queue");
         if (queue != null)
            replayManager.setQueueName(queue);
         String limitCountParam = dataSet.getMatrixParameters().getFirst("limitCount");
         if (limitCountParam != null)
            replayManager.setLimitCount(Integer.parseInt(limitCountParam));
         replayManager.connect();

         Future future = managedExecutorService.submit(replayManager);
         replayManager.setFuture(future);
         log.infof("Submitted replay manager task: %s", replayManager);
         managerTasks.put(replayManager.getID(), replayManager);
      } catch (Throwable t) {
         log.error("Failed to initialize replay manager", t);
         throw new WebApplicationException(t);
      }

      return result;
   }

   @GET
   @Path("cancel/{id}")
   @Produces({"text/html"})
   public Response cancel(@PathParam("id") String id) {
      log.infof("Cancel %s", id);
      ReplayManager replayManager = managerTasks.remove(id);
      if(replayManager == null)
         return Response.status(Status.NOT_FOUND).entity("<html><body><h1>NOT FOUND</h1></body></html>").build();

      // Cancel the replay
      replayManager.cancel();
      StringBuilder html = new StringBuilder("<html><body><h1>ReplayManager.cancel</h1><pre>");
      html.append(replayManager.toString());
      html.append("</pre></body></html>");
      return Response.ok(html.toString()).build();
   }

   @GET
   @Path("status/{id}")
   @Produces({"text/html"})
   public Response status(@PathParam("id") String id) {
      log.infof("Status %s", id);
      ReplayManager replayManager = managerTasks.get(id);
      if(replayManager == null)
         return Response.status(Status.NOT_FOUND).entity("<html><body><h1>NOT FOUND</h1></body></html>").build();

      // The status is displayed in the ReplayManager toString value
      Date now = new Date();
      StringBuilder html = new StringBuilder("<html><body><h1>ReplayManager.status, "+now+"</h1><pre>");
      html.append(replayManager.toString());
      html.append("</pre></body></html>");
      return Response.ok(html.toString()).build();
   }

   @GET
   @Path("statusall")
   @Produces({"text/html"})
   public Response statusall() {
      // The status is displayed in the ReplayManager toString value
      StringBuilder html = new StringBuilder("<html><body><h1>ReplayManager.status</h1><ul>");
      log.infof("Status all");

      for(ReplayManager replayManager : managerTasks.values()) {
         html.append("<li><pre>");
         html.append(replayManager.toString());
         html.append("</pre></li>");
      }
      html.append("</ul></body></html>");

      return Response.ok(html.toString()).build();
   }

   @GET
   @Path("test/{dataSet}")
   @Produces({"text/html"})
   public String testPathParam(@DefaultValue("SevenScannersRun-2015-05-11.json.gz") @PathParam("dataSet") PathSegment dataSet) {
      StringBuilder response = new StringBuilder("<html><title>ReplayService.replayData</title><body><h1>dataSet=");
      response.append(dataSet.getPath());
      response.append("</h1>");
      response.append("<h2>Path Parameters</h2><ul>");
      for (String key : dataSet.getMatrixParameters().keySet()) {
         response.append("<li>");
         response.append(key);
         response.append("=");
         response.append(dataSet.getMatrixParameters().getFirst(key));
         response.append("</li>");
      }
      response.append("</ul>");
      return response.toString();
   }


   @Override
   public void complete(ReplayManager manager) {
      log.infof("complete: %s", manager);
      String id = manager.getID();
      managerTasks.remove(id);
   }
}
