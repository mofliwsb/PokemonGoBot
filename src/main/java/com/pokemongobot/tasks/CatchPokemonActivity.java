package com.pokemongobot.tasks;

import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokemongobot.PokemonBot;
import com.pokemongobot.actions.CatchPokemon;
import org.apache.log4j.Logger;

import java.util.List;

public class CatchPokemonActivity implements BotActivity {

    private final Logger logger;
    private final PokemonBot pokemonBot;

    public CatchPokemonActivity(PokemonBot pokemonBot) {
        this.pokemonBot = pokemonBot;
        this.logger = Logger.getLogger(Thread.currentThread().getName());
    }

    public List<CatchResult> catchNearbyPokemon() throws LoginFailedException, RemoteServerException {
        return CatchPokemon.catchPokemon(logger, pokemonBot, pokemonBot.getCatchablePokemon());
    }

    @Override
    public void performActivity() throws LoginFailedException, RemoteServerException {
        catchNearbyPokemon();
    }

}
