import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestJsonConfig {
   static class Event {
      private String name;
      private String source;

      private Event(String name, String source) {
         this.name = name;
         this.source = source;
      }

      @Override
      public String toString() {
         return String.format("(name=%s, source=%s)", name, source);
      }
   }
   static class BaseConfig {
      String brokerURL;
      String user;
      String password;
      ArrayList<Properties> roomProperties;

      @Override
      public String toString() {
         return "BaseConfig{" +
            "brokerURL='" + brokerURL + '\'' +
            ", user='" + user + '\'' +
            ", password='" + password + '\'' +
            ", roomProperties=" + roomProperties +
            '}';
      }
   }

   @Test
   public void testCollection() {
      Gson gson = new Gson();
      Collection collection = new ArrayList();
      collection.add("hello");
      collection.add(5);
      collection.add(new Event("GREETINGS", "guest"));
      String json = gson.toJson(collection);
      System.out.println("Using Gson.toJson() on a raw collection: " + json);
      JsonParser parser = new JsonParser();
      JsonArray array = parser.parse(json).getAsJsonArray();

      String message = gson.fromJson(array.get(0), String.class);
      int number = gson.fromJson(array.get(1), int.class);
      Event event = gson.fromJson(array.get(2), Event.class);
      System.out.printf("Using Gson.fromJson() to get: %s, %d, %s", message, number, event);
   }
   @Test
   public void testProperties() {
      Properties props = new Properties();
      props.setProperty("brokerURL", "host1:port");
      props.setProperty("user", "theUser1");
      props.setProperty("password", "thePassword1");
      Properties props2 = new Properties();
      props2.setProperty("brokerURL", "host2:port");
      props2.setProperty("user", "theUser2");
      props2.setProperty("password", "thePassword2");

      Gson gson = new Gson();
      String json = gson.toJson(props);
      System.out.println("Using Gson.toJson() on a raw collection: " + json);

      Collection collection = new ArrayList();
      collection.add(props);
      collection.add(props2);
      System.out.println("Using Gson.toJson() on a raw collection: " + gson.toJson(collection));
   }
   @Test
   public void testBaseConfig() {
      BaseConfig config = new BaseConfig();
      config.brokerURL = "host1:port";
      config.user = "user1";
      config.password = "password1";
      config.roomProperties = new ArrayList<>();

      Properties props = new Properties();
      props.setProperty("brokerURL", "host1:port");
      props.setProperty("user", "theUser1");
      props.setProperty("password", "thePassword1");
      Properties props2 = new Properties();
      props2.setProperty("brokerURL", "host2:port");
      props2.setProperty("user", "theUser2");
      props2.setProperty("password", "thePassword2");
      config.roomProperties.add(props);
      config.roomProperties.add(props2);

      Gson gson = new Gson();
      System.out.println("Using Gson.toJson() on a raw collection: " + gson.toJson(config));
   }

   @Test
   public void testReadConfig() throws Exception {
      System.out.printf("pwd=%s\n", new File(".").getAbsolutePath());
      FileReader fr = new FileReader("src/test/resources/config.json");
      Gson gson = new Gson();
      BaseConfig baseConfig = gson.fromJson(fr, BaseConfig.class);
      System.out.printf("baseConfig: %s\n", baseConfig);
   }
}
