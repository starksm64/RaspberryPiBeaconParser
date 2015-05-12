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

import java.text.DateFormat;
import java.util.Date;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class EventInTime {
   static DateFormat dtf = DateFormat.getTimeInstance(DateFormat.LONG);

   private long eventEpoch;
   private long eventTime;
   private int id;
   private JsonObject json;

   public EventInTime(long eventEpoch, long eventTime, int id) {
      this(eventEpoch, eventTime, id, null);
   }
   public EventInTime(long eventEpoch, long eventTime, int id, JsonObject json) {
      this.eventTime = eventTime;
      this.eventEpoch = eventEpoch;
      this.id = id;
      this.json = json;
   }

   public int getID() {
      return id;
   }
   public JsonObject getJson() {return json;}
   public long getElapsedFromEpoch() {
      return eventTime - eventEpoch;
   }
   public long getReplayElapsedFromEpoch(long epoch) {
      return getReplayElapsedFromEpoch(epoch, 1);
   }
   public long getReplayElapsedFromEpoch(long epoch, float speedup) {
      long replayTime = getEventReplayTime(epoch, speedup);
      long elapsed = replayTime - epoch;
      return elapsed;
   }
   public long getEventReplayTime(long epoch) {
      return getEventReplayTime(epoch, 1);
   }
   public long getEventReplayTime(long epoch, float speedup) {
      long elapsed = eventTime - eventEpoch;
      long scaledElapsed = (long) (elapsed / speedup);
      long replayTime = epoch + scaledElapsed;
      return replayTime;
   }

   public String toString() {
      return id + "@" + dtf.format(new Date(eventTime));
   }
}
