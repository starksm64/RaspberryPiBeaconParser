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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TextUtils {
   /**
    * Adds a static mask to the specified text field.
    * @param tf  the text field.
    * @param mask  the mask to apply.
    * Example of usage: addMask(txtDate, "####-#####");
    */
   public static void addMask(final TextField tf, final String mask) {
       tf.setText(mask);

       tf.textProperty().addListener(new ChangeListener<String>() {
           @Override
           public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
               String value = stripMask(tf.getText(), mask);
               tf.setText(merge(value, mask));
           }
       });

       tf.setOnKeyPressed(new EventHandler<KeyEvent>() {
           @Override
           public void handle(final KeyEvent e) {
               int caretPosition = tf.getCaretPosition();
               if (caretPosition < mask.length()-1 && mask.charAt(caretPosition) != '#' && e.getCode() != KeyCode.BACK_SPACE && e.getCode() != KeyCode.LEFT) {
                   tf.positionCaret(caretPosition + 1);
               }
           }
       });
   }
   static String merge(final String value, final String mask) {
       final StringBuilder sb = new StringBuilder(mask);
       int k = 0;
       for (int i = 0; i < mask.length(); i++) {
           if (mask.charAt(i) == '#' && k < value.length()) {
               sb.setCharAt(i, value.charAt(k));
               k++;
           }
       }
       return sb.toString();
   }

   static String stripMask(String text, final String mask) {
       final Set<String> maskChars = new HashSet<>();
       for (int i = 0; i < mask.length(); i++) {
           char c = mask.charAt(i);
           if (c != '#') {
               maskChars.add(String.valueOf(c));
           }
       }
       for (String c : maskChars) {
           text = text.replace(c, "");
       }
       return text;
   }
}
