package com.pokemongobot;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.exceptions.AsyncPokemonGoException;
import com.pokegoapi.exceptions.CaptchaActiveException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.google.common.geometry.S2LatLng;
import com.pokemongobot.actions.BotWalker;
import com.pokemongobot.listeners.HeartBeatListener;
import com.pokemongobot.listeners.LocationListener;
import com.pokemongobot.listeners.SimpleHeartBeatListener;
import com.pokemongobot.listeners.SimpleLocationListener;
import com.pokemongobot.tasks.SnipePokemonActivity;

public class SniperBot implements PokemonBot{
    private final Logger logger = Logger.getLogger(Thread.currentThread().getName());
    private final PokemonGo api;

    private Options options;
    
    private BotWalker botWalker;
		
	public SniperBot(PokemonGo api, Options options){
		this.api = api;
        api.addListener(new CatchPokemonListener());

        this.options = options;
		
		//replace a few options
		options.setAvgWalkingSpeed(options.getSnipeAvgWalkingSpeed());
		options.setSpeedRange(options.getSnipeSpeedRange());
		options.setLocationUpdateIntervalMs(options.getSnipeLocationUpdateIntervalMs());
		options.setHeartBeatIntervalMs(options.getSnipeHeartBeatIntervalMs());
		
        LocationListener locationListener = new SimpleLocationListener(this);

        String pokemonName = options.getSnipePokemonName();
		SnipePokemonActivity snipeActivity = new SnipePokemonActivity(this, options.getStartingLocation(), pokemonName);

        HeartBeatListener heartBeatListener = new SimpleHeartBeatListener(options.getHeartBeatIntervalMs()/options.getLocationUpdateIntervalMs(), this);
		heartBeatListener.addHeartBeatActivity(snipeActivity);

		botWalker = new BotWalker(this, options.getSnipeLocation(), locationListener, heartBeatListener, options);
	}
	
	public S2LatLng getNextStop(S2LatLng snipeLocation){
		double latRange = 0.0001;		// 15m radius
		double lngRange = 0.0001;		// 15m radius

		double newLat = (snipeLocation.latDegrees() - latRange) + Math.random() * latRange * 2;
		double newLng = (snipeLocation.lngDegrees() - lngRange) + Math.random() * lngRange * 2;
		
		return S2LatLng.fromDegrees(newLat,  newLng);
	}
	
	public void sleep(int ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run(){
		S2LatLng snipeLocation = options.getSnipeLocation();

		try{
			setCurrentLocation(snipeLocation);
			sleep(10*1000);
			
			botWalker.performHeartBeat();
			
			while(true){
				S2LatLng newLocation = getNextStop(snipeLocation);
				botWalker.walkTo(getCurrentLocation(), newLocation);
			}
		}
		catch(LoginFailedException | RemoteServerException | CaptchaActiveException e){
			logger.error("Something went wrong, quitting", e);
			
			throw new RuntimeException("Something went wrong, quitting", e);
		}
	}

	
	@Override
	public BotWalker getWalker() {
		return botWalker;
	}

	@Override
	public void setWalker(BotWalker botWalker) {
		this.botWalker = botWalker;
	}

    public final synchronized S2LatLng setCurrentLocation(S2LatLng newLocation) {
        getApi().setLocation(newLocation.latDegrees(), newLocation.lngDegrees(), 1);
        logger.debug("new location: [" + newLocation.latDegrees() + ", " + newLocation.lngDegrees() + "]");
        return newLocation;
    }

    public final synchronized S2LatLng getCurrentLocation() {
        return S2LatLng.fromDegrees(getApi().getLatitude(), getApi().getLongitude());
    }

	@Override
	public List<CatchablePokemon> getCatchablePokemon() {
        try {
            getCurrentLocation();
            return getMap().getCatchablePokemon();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
	}

	@Override
	public Inventories getInventory() {
        try {
            return getApi().getInventories();
        } catch (AsyncPokemonGoException e) {
            logger.debug("Error getting inventory", e);
        }
        return null;
	}

	@Override
	public Map getMap() {
        return getApi().getMap();
	}

	@Override
	public PokemonGo getApi() {
        return api;
	}

	@Override
	public boolean fixSoftBan(S2LatLng destination)
			throws LoginFailedException, RemoteServerException, CaptchaActiveException {
		return false;
	}

	@Override
	public Options getOptions() {
		return options;
	}
}
