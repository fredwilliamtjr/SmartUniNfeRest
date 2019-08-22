package br.com.smartfull.controllers;

import javafx.application.HostServices;

public enum HostServicesProvider {

    INSTANCE ;

    private HostServices hostServices ;
    public void init(HostServices hostServices) {
        if (this.hostServices != null) {
            throw new IllegalStateException("Host services already initialized");
        }
        this.hostServices = hostServices ;
    }
    public HostServices getHostServices() {
        if (hostServices == null) {
            throw new IllegalStateException("Host services not initialized");
        }
        return hostServices ;
    }
}
