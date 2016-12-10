package com.pokemongobot.listeners;

public interface LocationListener extends AutoCloseable {

    void updateCurrentLocation();

}
