package org.jboss.summit2015.scanner.status.server;/*
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

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import org.jboss.summit2015.beacon.Beacon;

import java.util.Iterator;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BeaconsIterable implements Iterable<Beacon> {
   private TheInterator iterator;

   static class TheInterator implements Iterator<Beacon> {
      private Cursor cursor;
      // DatabaseEntry objects used for reading records
      DatabaseEntry foundKey = new DatabaseEntry();
      DatabaseEntry foundData = new DatabaseEntry();
      TupleBinding<Beacon> beaconBinding = new BeaconBinding();
      Beacon nextBeacon;

      TheInterator(Cursor cursor) {
         this.cursor = cursor;
      }
      @Override
      public boolean hasNext() {
         if(cursor == null)
            return false;

         boolean hasNext = false;
         try { // always want to make sure the cursor gets closed
            if (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
               nextBeacon = beaconBinding.entryToObject(foundData);
               hasNext = true;
            }
         } catch (Exception e) {
            System.err.println("Error on inventory cursor:");
            e.printStackTrace();
            cursor.close();
            cursor = null;
         }
         return hasNext;
      }

      @Override
      public Beacon next() {
         return nextBeacon;
      }
   }

   BeaconsIterable(Cursor cursor) {
      this.iterator = new TheInterator(cursor);
   }

   @Override
   public Iterator<Beacon> iterator() {
      return iterator;
   }
}
