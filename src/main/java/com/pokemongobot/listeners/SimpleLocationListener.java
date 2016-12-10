package com.pokemongobot.listeners;

import com.pokemongobot.PokemonBot;
import com.pokemongobot.actions.CatchPokemon;
import org.apache.log4j.Logger;

public class SimpleLocationListener implements LocationListener {

    private final PokemonBot bot;
    private final Logger logger;

    public SimpleLocationListener(PokemonBot bot) {
        this.bot = bot;
        this.logger = Logger.getLogger(Thread.currentThread().getName());
    }

    @Override
    public void updateCurrentLocation() {
//        if (bot.getOptions().isCatchPokemon())
//            CatchPokemon.catchPokemon(logger, bot, bot.getCatchablePokemon());
    }

    @Override
    public void close() throws Exception {

    }

}
