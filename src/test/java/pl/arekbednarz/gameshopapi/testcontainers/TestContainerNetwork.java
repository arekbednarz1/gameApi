package pl.arekbednarz.gameshopapi.testcontainers;

import org.testcontainers.containers.Network;

public interface TestContainerNetwork {
    Network NETWORK = Network.newNetwork();
}
