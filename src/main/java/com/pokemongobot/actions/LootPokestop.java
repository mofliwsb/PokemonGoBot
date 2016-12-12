package com.pokemongobot.actions;

import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.exceptions.AsyncPokemonGoException;
import com.pokegoapi.exceptions.CaptchaActiveException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import org.apache.log4j.Logger;
import POGOProtos.Networking.Responses.FortSearchResponseOuterClass.FortSearchResponse.Result;

import java.util.ArrayList;
import java.util.List;

public class LootPokestop {

    public static List<PokestopLootResult> lootPokestops(final Logger logger, final List<Pokestop> pokestops) throws LoginFailedException, RemoteServerException, CaptchaActiveException {
        final List<PokestopLootResult> result = new ArrayList<>(pokestops.size());
        for(Pokestop pokestop : pokestops){
            PokestopLootResult r;
			r = lootPokestop(logger, pokestop);
            if(!(r == null))
                result.add(r);
        }
        return result;
    }

    public static PokestopLootResult lootPokestop(final Logger logger, final Pokestop pokestop) throws LoginFailedException, RemoteServerException, CaptchaActiveException {
//        try {
            if (pokestop.canLoot()) {
                PokestopLootResult pokestopLootResult = pokestop.loot();
                if (pokestopLootResult.getResult()==Result.SUCCESS) {
                    logger.info("Looted pokestop " + pokestop.getDetails().getName() + " - " + pokestopLootResult.getResult().name());
                } else {
                    logger.info("Failed looting pokestops reason - " + pokestopLootResult.getResult().name());
                }
                return pokestopLootResult;
            }
//        } catch(LoginFailedException e) {
//            logger.debug("Error looting pokestop", e);
//        	if(e.getMessage().contains("Invalid Auth status code recieved"))
//        		throw e;
//	    } catch(RemoteServerException e){
//	    	throw e;
//	    } catch(AsyncPokemonGoException e){
//	        logger.debug("Error trying to catch pokemon", e);
//	    	if(e.getMessage().contains("Check auth required"))
//	    		throw e;
//	    }
//    	catch (Exception e) {
//            logger.debug("Error looting pokestop", e);
//        }
        return null;
    }

}
