package com.pokemongobot;

import org.apache.log4j.Logger;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Pokeball;
import com.pokegoapi.api.listener.PokemonListener;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.pokemon.HatchedEgg;

import POGOProtos.Enums.EncounterTypeOuterClass.EncounterType;
import POGOProtos.Enums.PokemonFamilyIdOuterClass.PokemonFamilyId;

public class CatchPokemonListener implements PokemonListener {

	Logger logger;
	public CatchPokemonListener(){
		logger = Logger.getLogger(Thread.currentThread().getName());
	}
	
	@Override
	public boolean onEggHatch(PokemonGo api, HatchedEgg hatchedEgg) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onEncounter(PokemonGo api, long encounterId, CatchablePokemon pokemon, EncounterType encounterType) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onCatchEscape(PokemonGo api, CatchablePokemon pokemon, Pokeball pokeball, int throwCount) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void onCatchAttempted(PokemonGo api, CatchablePokemon pokemon, Pokeball pokeball, int throwCount) {
		logger.debug("catch attempted, using " + pokeball.name());
	}

	@Override
	public void onBuddyFindCandy(PokemonGo arg0, PokemonFamilyId arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

}
