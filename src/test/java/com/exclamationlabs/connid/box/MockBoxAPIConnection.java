package com.exclamationlabs.connid.box;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxJSONResponse;
import com.box.sdk.RequestInterceptor;

import java.util.LinkedList;

public class MockBoxAPIConnection {

    private static final MockBoxAPIConnection INSTANCE = new MockBoxAPIConnection();

    private final BoxAPIConnection api;
    private final LinkedList<RequestInterceptor> interceptors;

    public static MockBoxAPIConnection instance() {
        return INSTANCE;
    }

    private MockBoxAPIConnection() {
        this.api = new BoxAPIConnection("dummy");
        this.api.setRefreshToken("dummy");
        this.interceptors = new LinkedList<>();
    }

    public BoxAPIConnection getAPIConnection() {
        return api;
    }

    public void init() {
        this.api.setRequestInterceptor(req -> {
            if (interceptors.size() == 0) {
                throw new IllegalStateException("Mock Box API wasn't set but an API was called.\n" + req.toString());
            }

            System.out.println("-->");
            System.out.println(req.toString());
            System.out.println("-->");

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
        });
        this.interceptors.clear();
    }

    public void mock(RequestInterceptor interceptor) {
        this.interceptors.add(interceptor);
    }
}
