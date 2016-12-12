package com.pokemongobot.actions;

import POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId;
import POGOProtos.Networking.Responses.CatchPokemonResponseOuterClass.CatchPokemonResponse.CatchStatus;

import com.pokegoapi.api.inventory.Pokeball;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.api.settings.CatchOptions;
import com.pokegoapi.api.settings.PokeballSelector;
import com.pokegoapi.exceptions.*;
import com.pokemongobot.PokemonBot;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CatchPokemon {

	public static void logPokeballs(Logger logger, PokemonBot bot){
//		try {
//			bot.getApi().getInventories().updateInventories(true);
//		} catch (LoginFailedException | RemoteServerException e) {
//            logger.debug("Error trying to refresh inventories", e);
//		}
		int nPokeball = bot.getApi().getInventories().getItemBag().getItem(ItemId.ITEM_POKE_BALL).getCount();
		int nGreatball = bot.getApi().getInventories().getItemBag().getItem(ItemId.ITEM_GREAT_BALL).getCount();
		int nUltraball = bot.getApi().getInventories().getItemBag().getItem(ItemId.ITEM_ULTRA_BALL).getCount();
		logger.debug("pokeballs: " + nPokeball + ", " + nGreatball + ", " + nUltraball);
	}

	public static List<CatchResult> catchPokemon(Logger logger, PokemonBot pokemonBot, List<CatchablePokemon> pokemonList)
	throws LoginFailedException, RemoteServerException, CaptchaActiveException
	{
		logPokeballs(logger, pokemonBot);
        List<CatchResult> results = new ArrayList<>(pokemonList.size());
        for(CatchablePokemon pokemon : pokemonList){
            CatchResult result = attemptCatch(logger, pokemonBot, pokemon);
            if (result != null && !result.isFailed())
                results.add(result);
            
            try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				logger.debug("failed to sleep after catching pokemon");
			}
        }
		logPokeballs(logger, pokemonBot);
        return results;
    }

    public static CatchResult attemptCatch(Logger logger, PokemonBot pokemonBot, CatchablePokemon pokemon) throws LoginFailedException, RemoteServerException, CaptchaActiveException {
        try {
            EncounterResult encounterResult = pokemon.encounterPokemon();
            if (encounterResult == null || !encounterResult.wasSuccessful())
                return null;

            CatchOptions catchOptions = new CatchOptions(pokemonBot.getApi());
            catchOptions.withPokeballSelector(PokeballSelector.SMART);
            catchOptions.maxPokeballs(3);
            
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
            return catchResult;
            
//        } catch(LoginFailedException e){
//            logger.debug("Error trying to catch pokemon", e);
//        	if(e.getMessage().contains("Invalid Auth status code recieved"))
//        		throw e;
//        } catch(RemoteServerException e){
//        	throw e;
//        } catch(AsyncPokemonGoException e){
//            logger.debug("Error trying to catch pokemon", e);
//        	if(e.getMessage().contains("Check auth required"))
//        		throw e;
        }catch (EncounterFailedException | NoSuchItemException e) {
            logger.debug("Error trying to catch pokemon", e);
        }
        return null;
    }

}
