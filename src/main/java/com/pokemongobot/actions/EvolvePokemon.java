package com.pokemongobot.actions;

import com.pokegoapi.api.inventory.CandyJar;
import com.pokegoapi.api.map.pokemon.EvolutionResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.AsyncPokemonGoException;
import com.pokegoapi.exceptions.CaptchaActiveException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class EvolvePokemon {

    public static List<EvolutionResult> evolvePokemon(Logger logger, List<Pokemon> pokemons, CandyJar candyJar) throws LoginFailedException, RemoteServerException, CaptchaActiveException {
        List<EvolutionResult> results = new ArrayList<>();
        for(Pokemon pokemon : pokemons){
            int candies = candyJar.getCandies(pokemon.getPokemonFamily());
            int candiesEvolve = pokemon.getCandiesToEvolve();
            if(candies >= candiesEvolve) {
                EvolutionResult result = evolve(logger, pokemon);
                if(result != null)
                    results.add(result);
            }
        }
        return results;
    }

    public static EvolutionResult evolve(Logger logger, Pokemon pokemon) throws LoginFailedException, RemoteServerException, CaptchaActiveException {
        if (pokemon == null)
            return null;
        EvolutionResult result = pokemon.evolve();
        if(result.isSuccessful()) {
            logger.info("Evolved pokemon " + pokemon.getPokemonId().name() + " to " + result.getEvolvedPokemon().getPokemonId().name());
        }
        return result;
    }

}
