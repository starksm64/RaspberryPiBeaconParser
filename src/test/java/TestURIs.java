import org.junit.Test;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestURIs {
   @Test
   public void testFragment() throws Exception {
      ConcurrentHashMap<Integer, byte[]> privateJPEGs = new ConcurrentHashMap<Integer, byte[]>();
      privateJPEGs.put(1, new byte[256]);
      URI test = new URI("content://com.example.starksm.cameratest/jpg#1");
      String fragment = test.getFragment();
      System.out.printf("fragment: %s\n", fragment);
      Integer key = Integer.valueOf(fragment);
      System.out.printf("fragment bytes: %d\n", privateJPEGs.get(key).length);

   }
}
