package com.exclamationlabs.connid.box;

import com.box.sdk.BoxAPIRequest;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxJSONResponse;
import com.eclipsesource.json.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.fail;

public class TestUtils {

    public static JsonObject toJsonObject(BoxAPIRequest request) {
        try {
            request.getBody().reset();
            return JsonObject.readFrom(new BufferedReader(new InputStreamReader(request.getBody(), "UTF-8")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getJsonAttr(BoxAPIRequest request, String attrName) {
        JsonObject json = toJsonObject(request);
        if (json.get(attrName) == null) {
            return null;
        }
        return json.get(attrName).asString();
    }

    public static BoxAPIResponse createOK(String path) {
        return new BoxJSONResponse(201, new TreeMap(String.CASE_INSENSITIVE_ORDER), readJSONFile(path));
    }

    public static BoxAPIResponse updateOK(String path) {
        return new BoxJSONResponse(200, new TreeMap(String.CASE_INSENSITIVE_ORDER), readJSONFile(path));
    }

    public static BoxAPIResponse deleteOK() {
        return new BoxAPIResponse(204, new TreeMap(String.CASE_INSENSITIVE_ORDER));
    }

    public static BoxAPIResponse getOK(String path) {
        return new BoxJSONResponse(200, new TreeMap(String.CASE_INSENSITIVE_ORDER), readJSONFile(path));
    }

    public static JsonObject readJSONFile(String path) {
        InputStream in = MockBoxAPIConnection.class.getResourceAsStream("/" + path);
        if (in == null) {
            fail(path + " is not found.");
        }
        try {
            JsonObject json = JsonObject.readFrom(new BufferedReader(new InputStreamReader(in, "UTF-8")));
            return json;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
