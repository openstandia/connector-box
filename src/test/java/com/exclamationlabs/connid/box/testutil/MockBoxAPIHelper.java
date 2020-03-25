package com.exclamationlabs.connid.box.testutil;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxJSONResponse;
import com.box.sdk.RequestInterceptor;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Helper class for managing mock BoxAPIConnection.
 *
 * @author Hiroyuki Wada
 */
public class MockBoxAPIHelper {

    private static final MockBoxAPIHelper INSTANCE = new MockBoxAPIHelper();

    private final BoxAPIConnection api;
    private final LinkedList<RequestInterceptor> interceptors;

    public static MockBoxAPIHelper instance() {
        return INSTANCE;
    }

    private MockBoxAPIHelper() {
        this.api = new BoxAPIConnection("dummy");
        this.api.setRefreshToken("dummy");
        this.interceptors = new LinkedList<>();
    }

    public BoxAPIConnection getAPIConnection() {
        return api;
    }

    public void init() {
        this.api.setMaxRequestAttempts(1); // Set 1 for testing
        this.api.setRequestInterceptor(req -> {
            if (interceptors.size() == 0) {
                fail("Mock Box API wasn't set but an API was called.\n" + req.toString());
            }

            System.out.println("-->");
            System.out.println(req.toString());
            System.out.println("-->");

            // Call pushed Mock API
            try {
                BoxAPIResponse res = interceptors.pop().onRequest(req);

                if (res instanceof BoxJSONResponse) {
                    BoxJSONResponse jsonRes = (BoxJSONResponse) res;

                    System.out.println("<--");
                    System.out.println("Response(JSON)");
                    System.out.println("");
                    System.out.println(jsonRes.getJSON());
                    System.out.println("<--");
                } else {
                    System.out.println("<--");
                    System.out.println("Response(EMPTY)");
                    System.out.println("<--");
                }

                return res;

            } catch (RuntimeException e) {
                System.out.println("<--");
                System.out.println("Exception");
                System.out.println("");
                System.out.println(e.toString());
                System.out.println("<--");

                throw e;
            }
        });
        this.interceptors.clear();
    }

    public void setMaxRequestAttempts(int attempts) {
        this.api.setMaxRequestAttempts(attempts);
    }

    /**
     * Push an interceptor which pretends the Box API.
     * If you have a test scenario which calls multiple Box API internally, call this method multiple times.
     *
     * @param interceptor
     */
    public void push(RequestInterceptor interceptor) {
        this.interceptors.add(interceptor);
    }
}
