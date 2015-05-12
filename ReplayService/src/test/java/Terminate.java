import com.google.gson.JsonObject;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public interface Terminate {
   boolean shouldTerminate(int count, JsonObject event);
}
