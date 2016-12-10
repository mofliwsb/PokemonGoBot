package com.pokemongobot.tasks;

import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

public interface BotActivity {

    void performActivity()  throws LoginFailedException, RemoteServerException;

}
