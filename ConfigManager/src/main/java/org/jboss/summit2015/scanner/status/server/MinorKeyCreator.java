package org.jboss.summit2015.scanner.status.server;
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

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import org.jboss.summit2015.beacon.Beacon;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class MinorKeyCreator implements SecondaryKeyCreator {

    private final TupleBinding theBinding;

    // Use the constructor to set the tuple binding
    MinorKeyCreator(TupleBinding binding) {
        theBinding = binding;
    }

    // Abstract method that we must implement
    public boolean createSecondaryKey(SecondaryDatabase secDb,
             DatabaseEntry keyEntry,      // From the primary
             DatabaseEntry dataEntry,     // From the primary
             DatabaseEntry resultEntry) { // set the key data on this.
        if (dataEntry != null) {
            Beacon beacon = (Beacon) theBinding.entryToObject(dataEntry);
            // Get the minorID and use that as the key
            Integer minorID = beacon.getMinor();
            IntegerBinding.intToEntry(minorID, resultEntry);
        }
        return true;
    }
}