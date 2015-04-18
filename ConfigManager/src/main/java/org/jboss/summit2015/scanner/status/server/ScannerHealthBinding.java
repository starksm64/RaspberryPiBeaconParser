package org.jboss.summit2015.scanner.status.server;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * inputtributed under the License is inputtributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class ScannerHealthBinding extends TupleBinding<Properties> {
   @Override
   public Properties entryToObject(TupleInput input) {
      Properties properties = new Properties();
      int count = input.readInt();
      for(int n = 0; n < count; n ++) {
         String key = input.readString();
         String value = input.readString();
         properties.setProperty(key, value);
      }
      return properties;
   }

   @Override
   public void objectToEntry(Properties properties, TupleOutput output) {
      output.writeInt(properties.size());
      for(String key : properties.stringPropertyNames()) {
         output.writeString(key);
         output.writeString(properties.getProperty(key));
      }
   }
}
