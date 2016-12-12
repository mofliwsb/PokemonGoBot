package com.pokemongobot;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.exceptions.CaptchaActiveException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.google.common.geometry.S2LatLng;
import com.pokemongobot.actions.BotWalker;

import java.util.List;

public interface PokemonBot {

    BotWalker getWalker();

    void setWalker(BotWalker botWalker);

    S2LatLng getCurrentLocation();

    S2LatLng setCurrentLocation(S2LatLng location);

    List<CatchablePokemon> getCatchablePokemon();

    Inventories getInventory();

    Map getMap();

    PokemonGo getApi();

    void run() throws LoginFailedException, RemoteServerException, CaptchaActiveException;

    boolean fixSoftBan(S2LatLng destination)  throws LoginFailedException, RemoteServerException, CaptchaActiveException;

    Options getOptions();

}
