package org.jboss.summit2015.beacon.scanner;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base hcidump parser class
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public abstract class AbstractParser {

   /** Flag indicating that finite length test data is being used and the client should be disconnected when all data is written */
   private volatile boolean usingTestData;
   private volatile boolean running;
   private String scannerID;

   public String getScannerID() {
      return scannerID;
   }

   public void setScannerID(String scannerID) {
      this.scannerID = scannerID;
   }
   /**
    * Utility method for opening the input stream representing the hcidump raw output.
    * @param testDataPath - a file or resource path to use if not null. If null, System.in will be used.
    * @return the InputStream for the hcidump raw output
    * @throws IOException
    */
   public InputStream getInputStream(String testDataPath) throws IOException {
      InputStream is = System.in;
      if(testDataPath != null) {
         File testData = new File(testDataPath);
         if(testData.exists()) {
            // The testDataPath points to an existing file, use it
            is = new FileInputStream(testDataPath);
         } else {
            // Search the classpath for a resource
            is = getClass().getResourceAsStream(testDataPath);
         }
         usingTestData = true;
      }
      return is;
   }

   public boolean isRunning() {
      return running;
   }
   public void setRunning(boolean running) {
      this.running = running;
   }

   /**
    * Is the hcidump stream a finite legnth playback of test data
    * @return true if the stream is test data
    */
   public boolean isUsingTestData() {
      return usingTestData;
   }

   /**
    * Process the input stream of hcidump data
    * @param is - an InputStream of hcidump command output
    * @throws Exception - thrown on any read/processing error
    */
   abstract public void processHCIStream(final InputStream is) throws Exception;

   /**
    * Cleanup resource used
    */
   abstract public void cleanup();
}
