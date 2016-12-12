package com.pokemongobot.tasks;

import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.api.settings.CatchOptions;
import com.pokegoapi.api.settings.PokeballSelector;
import com.pokegoapi.google.common.geometry.S2LatLng;
import com.pokemongobot.PokemonBot;

import POGOProtos.Networking.Responses.CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus;

import org.apache.log4j.Logger;

import java.util.List;

public class SnipePokemonActivity implements BotActivity {

    private final Logger logger;
    private final PokemonBot pokemonBot;

    private S2LatLng locationToTeleportAfterEncouter;
    private final String pokemonName;
    
    private boolean toStop;

    public SnipePokemonActivity(PokemonBot pokemonBot, S2LatLng locationToTeleportAfterEncouter, String pokemonName) {
        this.pokemonBot = pokemonBot;
        this.logger = Logger.getLogger(Thread.currentThread().getName());
        this.pokemonName = pokemonName;
        this.locationToTeleportAfterEncouter = locationToTeleportAfterEncouter;
        
        toStop = false;
    }

    @Override
    public void performActivity(){
    	if(toStop)
    		return;
    	
    	List<CatchablePokemon> pokemons = pokemonBot.getCatchablePokemon();
    	for(CatchablePokemon pokemon : pokemons){
    		if(pokemon.getPokemonId().name().equals(pokemonName))
    		{
    			logger.info("Found pokemon: " + pokemonName);
    	        try {
    	            EncounterResult encounterResult = pokemon.encounterPokemon();
    	            if (encounterResult == null || !encounterResult.wasSuccessful())
    	                break;

    	            // teleport before catch
    	            if(locationToTeleportAfterEncouter!=null){
    	            	Thread.sleep(3*1000);
    	            	pokemonBot.setCurrentLocation(locationToTeleportAfterEncouter);
    	            }
    	            
    	            CatchOptions catchOptions = new CatchOptions(pokemonBot.getApi());
    	            catchOptions.withPokeballSelector(PokeballSelector.HIGHEST);
    	            catchOptions.maxPokeballs(5);
    	            
    	            CatchResult catchResult;
    	            catchResult = pokemon.catchPokemon(encounterResult, catchOptions);
    	            CatchStatus catchStatus = catchResult.getStatus();
    	            while (catchStatus == CatchStatus.CATCH_MISSED) {
    	                catchResult = pokemon.catchPokemon();
    	                catchStatus = catchResult.getStatus();
    	            }
    	            switch (catchResult.getStatus()) {
    	                case CATCH_SUCCESS:
    	                    logger.info("Caught pokemon " + pokemon.getPokemonId().name());
    	                    break;
    	                default:
    	                    logger.info("" + pokemon.getPokemonId().name() + " got away reason " + catchResult.getStatus().toString());
    	                    break;
    	            }
    	        }catch(Exception e) {
    	            logger.debug("Error trying to catch pokemon", e);
    	        }

    	        throw new RuntimeException("Saw pokemon while sniping, quiting regardless result");
    		}
    	}
    }

}
