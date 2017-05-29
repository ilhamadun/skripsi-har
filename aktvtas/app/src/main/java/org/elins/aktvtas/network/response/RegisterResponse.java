package org.elins.aktvtas.network.response;

public class RegisterResponse extends ResponseMessage {
    private String device;
    private String token;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
