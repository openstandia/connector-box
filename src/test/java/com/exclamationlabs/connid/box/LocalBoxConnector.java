package com.exclamationlabs.connid.box;

/**
 * BoxConnector implementation for local environment which uses mock Box API instead of real Box API.
 *
 * @author Hiroyuki Wada
 */
public class LocalBoxConnector extends BoxConnector {

    @Override
    protected void authenticateResource() {
        boxAPI = MockBoxAPIConnection.instance().getAPIConnection();
    }
}
