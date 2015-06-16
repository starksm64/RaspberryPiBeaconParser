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
import java.util.regex.Pattern;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BeaconRegex implements Predicate<BeaconModel> {
   private Predicate<String> stringMatch;
   private boolean prioritizeMinorID;

   public BeaconRegex(String pattern, boolean prioritizeMinorID) {
      this.prioritizeMinorID = prioritizeMinorID;
      Pattern p = Pattern.compile(pattern);
      stringMatch = p.asPredicate();
   }
   @Override
   public boolean test(BeaconModel e) {
      boolean match = false;
      String minorID = ""+e.getMinorID();
      if(prioritizeMinorID)
         match = stringMatch.test(minorID);
      if(!match)
         match = stringMatch.test(e.getName());
      if(!match)
         match = stringMatch.test(e.getFactoryID());
      if(!prioritizeMinorID && !match)
         match = stringMatch.test(minorID);
      return match;
   }
}
