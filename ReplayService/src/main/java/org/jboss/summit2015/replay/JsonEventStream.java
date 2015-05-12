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
import com.google.gson.JsonStreamParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class JsonEventStream implements Iterator<JsonObject> {
   private JsonStreamParser jsp;
   private InputStreamReader reader;


   @Override
   public boolean hasNext() {
      return jsp.hasNext();
   }

   @Override
   public JsonObject next() {
      return jsp.next().getAsJsonObject();
   }

   public void init(String dataSet) throws IOException {
      // Get the dataSet input stream directly from git
      URL gitURL = new URL("https://github.com/starksm64/RaspberryPiBeaconParser/blob/master/data/"+dataSet+"?raw=true");
      InputStream is = gitURL.openStream();
      GZIPInputStream gzip = new GZIPInputStream(is);
      reader = new InputStreamReader(gzip);
      jsp = new JsonStreamParser(reader);
   }

   public void close() {
      try {
         if (reader != null)
            reader.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
