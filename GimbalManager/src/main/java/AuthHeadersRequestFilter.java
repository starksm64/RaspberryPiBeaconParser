import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
@Priority(1)
public class AuthHeadersRequestFilter implements ClientRequestFilter {

    private final String authToken;

    public AuthHeadersRequestFilter(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().add("Authorization", authToken);
    }

}