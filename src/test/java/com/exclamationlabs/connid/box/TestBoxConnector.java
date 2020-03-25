package com.exclamationlabs.connid.box;

public class TestBoxConnector extends BoxConnector {

    @Override
    protected void authenticateResource() {
        api = MockBoxAPIConnection.instance().getApi();
    }
}
