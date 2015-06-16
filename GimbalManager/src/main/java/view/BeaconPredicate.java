package view;

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

import model.BeaconModel;

import java.util.function.Predicate;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BeaconPredicate implements Predicate<BeaconModel> {
   private String lowerCaseFilter;
   private boolean prioritizeMinorID;
   int testID = -1;

   public BeaconPredicate(String lowerCaseFilter, boolean prioritizeMinorID) {
      this.lowerCaseFilter = lowerCaseFilter;
      this.prioritizeMinorID = prioritizeMinorID;
      if(prioritizeMinorID) {
         testID = Integer.parseInt(lowerCaseFilter);
         System.out.printf("testID=%d\n", testID);
      }
   }

   @Override
   public boolean test(BeaconModel beacon) {
      boolean match = false;
      if(prioritizeMinorID) {
         int diff = beacon.getMinorID() - testID;
         match = testID == beacon.getMinorID() || (diff > 0 && diff <= 10);
         System.out.printf("%s; %d: diff: %d\n", match, beacon.getMinorID(), diff);
      }
      if (!match) {
         match = beacon.getName().toLowerCase().contains(lowerCaseFilter);
      }
      if (!match) {
         match = beacon.getFactoryID().toLowerCase().contains(lowerCaseFilter);
      }
      return match;
   }
}
