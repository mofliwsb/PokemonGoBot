package com.pokemongobot.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.pokegoapi.api.inventory.CandyJar;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.CaptchaActiveException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokemongobot.Options;
import com.pokemongobot.PokemonBot;
import com.pokemongobot.actions.EvolvePokemon;

public class EvolvePokemonActivity implements BotActivity {

    private final Logger logger;
    private final PokemonBot pokemonBot;
    private final Options options;


	public EvolvePokemonActivity(PokemonBot pokemonBot, Options options){
        this.pokemonBot = pokemonBot;
        this.options = options;
        this.logger = Logger.getLogger(Thread.currentThread().getName());		
	}
	
	@Override
	public void performActivity() throws LoginFailedException, RemoteServerException, CaptchaActiveException {
        Inventories inventories = pokemonBot.getInventory();
        if (inventories == null)
            return;

        final CandyJar candyJar = inventories.getCandyjar();

        List<Pokemon> pokemons = new ArrayList<Pokemon>();
        
        for(Pokemon pokemon : inventories.getPokebank().getPokemons()){
            for (String name : options.getOnlyEvolvePokemons()) {
                if (name.equalsIgnoreCase(pokemon.getPokemonId().name())){
                	if(pokemon.getCandiesToEvolve() > 0 && candyJar.getCandies(pokemon.getPokemonFamily()) >= 10*pokemon.getCandiesToEvolve())
                		pokemons.add(pokemon);
                }
            }
        	
        }
        
        EvolvePokemon.evolvePokemon(logger, pokemons, candyJar);
	}

}
