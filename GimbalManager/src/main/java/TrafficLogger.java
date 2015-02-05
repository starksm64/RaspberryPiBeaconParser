import org.jboss.logging.Logger;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

@Provider
@Priority(10)
public class TrafficLogger implements ClientRequestFilter, ClientResponseFilter {
   private static Logger log = Logger.getLogger(TrafficLogger.class);

   @Override
   public void filter(ClientRequestContext requestContext) throws IOException {
      log(requestContext);
   }

   @Override
   public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
      log(responseContext);

   }

    void log(ClientRequestContext requestContext) {
        URI uriInfo = requestContext.getUri();
       Collection<String> attrs = requestContext.getPropertyNames();
       log.infof("Reading url %s, attrs=%s, headers=%s", uriInfo, attrs, requestContext.getStringHeaders());
    }

    void log(ClientResponseContext responseContext) {
        MultivaluedMap<String, String> stringHeaders = responseContext.getHeaders();
       log.infof("ResponseHeaders: %s\n", stringHeaders);
    }
}
