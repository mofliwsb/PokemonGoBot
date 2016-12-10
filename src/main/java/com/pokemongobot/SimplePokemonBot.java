package com.pokemongobot;

import POGOProtos.Inventory.Item.ItemAwardOuterClass.ItemAward;
import POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId;
import POGOProtos.Map.Fort.FortDataOuterClass;
import POGOProtos.Networking.Responses.ReleasePokemonResponseOuterClass;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.CandyJar;
import com.pokegoapi.api.inventory.EggIncubator;
import com.pokegoapi.api.inventory.Inventories;
import com.pokegoapi.api.inventory.Stats;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.fort.FortDetails;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.EvolutionResult;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.AsyncPokemonGoException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.google.common.geometry.S2LatLng;
import com.pokemongobot.actions.*;
import com.pokemongobot.listeners.HeartBeatListener;
import com.pokemongobot.listeners.LocationListener;
import com.pokemongobot.listeners.SimpleHeartBeatListener;
import com.pokemongobot.listeners.SimpleLocationListener;
import com.pokemongobot.tasks.CatchPokemonActivity;
import com.pokemongobot.tasks.EvolvePokemonActivity;
import com.pokemongobot.tasks.TransferPokemonActivity;
import org.apache.log4j.Logger;

import POGOProtos.Networking.Responses.FortSearchResponseOuterClass.FortSearchResponse.Result;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;


public class SimplePokemonBot implements PokemonBot {

    private final Logger logger = Logger.getLogger(Thread.currentThread().getName());
    private final PokemonGo api;
    private State state;
    private BotWalker botWalker;
    private State currentOperation = State.NAN;

    private Options options;

    public SimplePokemonBot(PokemonGo api, Options options) {
        this.api = api;
        api.addListener(new CatchPokemonListener());
        
        this.setCurrentLocation(options.getStartingLocation());

        HeartBeatListener heartBeatListener = new SimpleHeartBeatListener(options.getHeartBeatIntervalMs()/options.getLocationUpdateIntervalMs(), this);

        CatchPokemonActivity catchPokemonActivity = new CatchPokemonActivity(this);
        EvolvePokemonActivity evolvePokemonActivity = new EvolvePokemonActivity(this, options);
        TransferPokemonActivity transferPokemonActivity = new TransferPokemonActivity(this, options);

        if(options.isCatchPokemon()){
        	heartBeatListener.addHeartBeatActivity(catchPokemonActivity);
        }
        if(options.isEvolve()){
        	heartBeatListener.addHeartBeatActivity(evolvePokemonActivity);
        }
        if (options.isTransferPokemon()) {
            heartBeatListener.addHeartBeatActivity(transferPokemonActivity);
        }

        LocationListener locationListener = new SimpleLocationListener(this);

        BotWalker botWalker = new BotWalker(this, options.getStartingLocation(), locationListener, heartBeatListener, options);
//        botWalker.addPostStepActivity(catchPokemonActivity);

        this.botWalker = botWalker;
        this.state = State.NAN;
        this.options = options;
    }

    protected static Double getRandom() {
        return Math.random() * 750;
    }

    protected boolean longSleep() {
        return sleep(new Double((Math.random() * 2000)).intValue() + 2000);
    }

    protected synchronized boolean sleep(long wait) {
        try {
            Thread.sleep(wait);
            return true;
        } catch (InterruptedException ignore) {
            logger.debug("Error pausing thread", ignore);
            return false;
        }
    }

    public String LocationToString(S2LatLng loc){
    	return "[" + loc.latDegrees() + ", " + loc.lngDegrees() + "]";
    }


    public Pokestop getNextPokestop(HashMap<Pokestop, Long> psMap){
    	double minDistance = Double.MAX_VALUE;
    	Pokestop minPs = null;
    	Iterator<Entry<Pokestop, Long>> it = psMap.entrySet().iterator();
    	while(it.hasNext()){
    		Entry<Pokestop, Long> entry = it.next();
    		if(entry.getValue() > System.currentTimeMillis())
    			continue;
    		
    		Pokestop ps = entry.getKey();
    		double distance = getCurrentLocation().getEarthDistance(S2LatLng.fromDegrees(ps.getLatitude(), ps.getLongitude()));
    		if(distance < minDistance){
    			minDistance = distance;
    			minPs = ps;
    		}
    	}
    	return minPs;
    }
    
    @Override
    public void wander() throws LoginFailedException, RemoteServerException{
    	List<Pokestop> pokestops;

        int retry = 2;
    	int n = 0;
        do{
        	pokestops = getNearbyPokestops();
            logger.info("Found " + pokestops.size() + " Pokestops nearby");
        	
	        if(pokestops.size()==0){
	        	if(api.hasChallenge()){
	        		VerifyCaptcha.completeCaptcha(api, api.getChallengeURL(), logger);
	        		while(api.hasChallenge())
	                	sleep(60000);		// wait until captcha is resolved
	        	}
		        else{
		        	sleep(60000);
		        }
	        }
        } while(n++<retry && pokestops.size()==0);
        
        HashMap<Pokestop, Long> psMap = new HashMap<Pokestop, Long>();
        for(Pokestop ps : pokestops){
        	psMap.put(ps,  0L);
        }
        
        while(true){
        	Pokestop ps = getNextPokestop(psMap);
        	if(ps==null){
            	logger.info("No pokestop to loot, sleeping...");
        		sleep(600000);
        		continue;
        	}
        	
	    	S2LatLng target = S2LatLng.fromDegrees(ps.getLatitude(), ps.getLongitude());
            logger.info("Walking to " + ps.getDetails().getName() + " " + LocationToString(target) + " - "
                    + this.getCurrentLocation().getEarthDistance(target) + "m away");

            try{
	            botWalker.walkTo(getCurrentLocation(), target);
	            
	            longSleep();
	            
	            lootPokestop(ps);
            } finally {
            	psMap.put(ps, System.currentTimeMillis() + 10*60*1000); // don't visit again in 10 mins
            }
        }
    }
    
    
    public void wander_last() throws LoginFailedException, RemoteServerException{
    	String lastPokestopId = "";
    	while(true){
	    	List<Pokestop> pokestops = getNearbyPokestops();
            logger.info("Found " + pokestops.size() + " Pokestops nearby");

            Pokestop pokestop = null;
            for(Pokestop ps : pokestops){
            	if(ps.getCooldownCompleteTimestampMs()==0 && !ps.getId().equals(lastPokestopId)){
            		pokestop = ps;
            		break;
            	}
            }

            if(pokestop == null){
            	logger.info("No pokestop to loot, sleeping...");
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					logger.debug("Error pausing thread", e);
					e.printStackTrace();
				}
				
				continue;
            }
	    	
	    	S2LatLng target = S2LatLng.fromDegrees(pokestop.getLatitude(), pokestop.getLongitude());
            try {
                logger.info("Walking to " + pokestop.getDetails().getName() + " " + LocationToString(target) + " - "
                        + this.getCurrentLocation().getEarthDistance(target) + "m away");
            } catch (AsyncPokemonGoException | RemoteServerException | LoginFailedException e) {
                logger.debug(e);
            }
            
            botWalker.walkTo(getCurrentLocation(), target);
            
            longSleep();
            
            lootPokestop(pokestop);
            lastPokestopId = pokestop.getId();
            
            longSleep();
            
//            try {
//                Thread.sleep(5000);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

//            catchNearbyPokemon();
//            doEvolutions();
//            doTransfers(true);
    	}
    }
    
    public void dropItems() {
    	try {
			api.getInventories().updateInventories(true);
		} catch (LoginFailedException | RemoteServerException e1) {
			logger.debug("error when updating inventory before dropping items.");
		}

    	int keepPotion = options.getPotionsToKeep();
    	int keepRevive = options.getRevivesToKeep();
    	int keepBall = options.getBallsToKeep();
    	int keepBerry = options.getBerriesToKeep();

    	int totalPotion = 0;
        for(ItemId id : new ItemId[]
        		{
        				ItemId.ITEM_POTION,
        				ItemId.ITEM_SUPER_POTION,
        				ItemId.ITEM_HYPER_POTION,
        				ItemId.ITEM_MAX_POTION,
        		}){
        	totalPotion += api.getInventories().getItemBag().getItem(id).getCount();
        }

        for(ItemId id : new ItemId[]
        		{
        				ItemId.ITEM_POTION,
        				ItemId.ITEM_SUPER_POTION,
        				ItemId.ITEM_HYPER_POTION,
        				ItemId.ITEM_MAX_POTION
        		}){
        	int m = totalPotion - keepPotion;
        	if(m<=0)
        		break;
        	
        	int n = api.getInventories().getItemBag().getItem(id).getCount();
        	if(n==0)
        		continue;
        	
        	try {
        		int x = m>n?n:m;
				api.getInventories().getItemBag().removeItem(id, x);
				totalPotion -= x;
			} catch (RemoteServerException | LoginFailedException e) {
				logger.debug("failed to remove item " + id.name());
			}
        }
        
    	int totalBall = 0;
        for(ItemId id : new ItemId[]
        		{
        				ItemId.ITEM_POKE_BALL,
        				ItemId.ITEM_GREAT_BALL,
        				ItemId.ITEM_ULTRA_BALL,
        		}){
        	totalBall += api.getInventories().getItemBag().getItem(id).getCount();
        }

        for(ItemId id : new ItemId[]
        		{
        				ItemId.ITEM_POKE_BALL,
        				ItemId.ITEM_GREAT_BALL,
        				ItemId.ITEM_ULTRA_BALL,
        		}){
        	int m = totalBall - keepBall;
        	if(m<=0)
        		break;
        	
        	int n = api.getInventories().getItemBag().getItem(id).getCount();
        	if(n==0)
        		continue;
        	
        	try {
        		int x = m>n?n:m;
				api.getInventories().getItemBag().removeItem(id, x);
				totalBall -= x;
			} catch (RemoteServerException | LoginFailedException e) {
				logger.debug("failed to remove item " + id.name());
			}
        }

        
    	int totalRevive = 0;
        for(ItemId id : new ItemId[]
        		{
        				ItemId.ITEM_REVIVE,
        				ItemId.ITEM_MAX_REVIVE
        		}){
        	totalRevive += api.getInventories().getItemBag().getItem(id).getCount();
        }

        for(ItemId id : new ItemId[]
        		{
        				ItemId.ITEM_REVIVE,
        				ItemId.ITEM_MAX_REVIVE
        		}){
        	int m = totalRevive - keepRevive;
        	if(m<=0)
        		break;
        	
        	int n = api.getInventories().getItemBag().getItem(id).getCount();
        	if(n==0)
        		continue;
        	
        	try {
        		int x = m>n?n:m;
				api.getInventories().getItemBag().removeItem(id, x);
				totalRevive -= x;
			} catch (RemoteServerException | LoginFailedException e) {
				logger.debug("failed to remove item " + id.name());
			}
        }

        int totalBerry = 0;
        for(ItemId id : new ItemId[]
        		{
        				ItemId.ITEM_RAZZ_BERRY
        		}){
        	totalBerry += api.getInventories().getItemBag().getItem(id).getCount();
        }

        for(ItemId id : new ItemId[]
        		{
        				ItemId.ITEM_RAZZ_BERRY
        		}){
        	int m = totalBerry - keepBerry;
        	if(m<=0)
        		break;
        	
        	int n = api.getInventories().getItemBag().getItem(id).getCount();
        	if(n==0)
        		continue;
        	
        	try {
        		int x = m>n?n:m;
				api.getInventories().getItemBag().removeItem(id, x);
				totalBerry -= x;
			} catch (RemoteServerException | LoginFailedException e) {
				logger.debug("failed to remove item " + id.name());
			}
        }

        int nPokeball = api.getInventories().getItemBag().getItem(ItemId.ITEM_POKE_BALL).getCount();
		int nGreatball = api.getInventories().getItemBag().getItem(ItemId.ITEM_GREAT_BALL).getCount();
		int nUltraball = api.getInventories().getItemBag().getItem(ItemId.ITEM_ULTRA_BALL).getCount();
        int nPotions = api.getInventories().getItemBag().getItem(ItemId.ITEM_POTION).getCount();
        int nSuperPotions = api.getInventories().getItemBag().getItem(ItemId.ITEM_SUPER_POTION).getCount();
        int nHyperPotions = api.getInventories().getItemBag().getItem(ItemId.ITEM_HYPER_POTION).getCount();
        int nMaxPotions = api.getInventories().getItemBag().getItem(ItemId.ITEM_MAX_POTION).getCount();
        int nRevives = api.getInventories().getItemBag().getItem(ItemId.ITEM_REVIVE).getCount();
        int nMaxRevives = api.getInventories().getItemBag().getItem(ItemId.ITEM_MAX_REVIVE).getCount();
        int nBerries = api.getInventories().getItemBag().getItem(ItemId.ITEM_RAZZ_BERRY).getCount();
		logger.debug("pokeballs: (" + nPokeball + ", " + nGreatball + ", " + nUltraball + "), potions: (" + nPotions + ", " + nSuperPotions + ", " + nHyperPotions + ", " + nMaxPotions + "), revives: (" + nRevives + ", " + nMaxRevives + "), berries: " + nBerries);
    }
    

    public final boolean fixSoftBan() {
        boolean running = true;
        while (running) {
            try {
                Pokestop pokestop = getNearestPokestop().get();
                running = !this.fixSoftBan(S2LatLng.fromDegrees(pokestop.getLatitude(), pokestop.getLongitude()));
                return running;
            } catch (Exception e) {
                running = false;
            }
        }
        return false;
    }

    public final boolean fixSoftBan(S2LatLng destination) throws LoginFailedException, RemoteServerException {
        this.getWalker().runTo(this.getCurrentLocation(), destination);
        setCurrentLocation(destination);
        Optional<Pokestop> nearest = getNearestPokestop();
        if (!nearest.isPresent()) {
            return false;
        }

        Pokestop pokestop = nearest.get();

        try {
            long lon = Double.valueOf(pokestop.getLongitude()).longValue();
            long lat = Double.valueOf(pokestop.getLatitude()).longValue();

            Map map = getApi().getMap();

            for (int i = 0; i < 80; i++) {
                FortDetails d = map.getFortDetails(pokestop.getId(), lon, lat);

                if (d != null) {
                    logger.info("Attempted spin number " + i);
                } else {
                    logger.debug("Error getting pokestop");
                }

                PokestopLootResult r = pokestop.loot();
                if (r.wasSuccessful() && r.getItemsAwarded().size() > 0) {
                    //TODO log xp items gained etc
                    return true;
                } else {
                    logger.info("Failed unbanning");
                }
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            PokestopLootResult finalTry = pokestop.loot();
            return finalTry.wasSuccessful();
        } catch (AsyncPokemonGoException | RemoteServerException | LoginFailedException e) {
            logger.error("Error while trying to unban", e);
        }

        return false;
    }

    public final Optional<Pokestop> getNearestPokestop() {
        List<Pokestop> pokestops = getNearbyPokestops();
        return pokestops.stream().filter(Pokestop::canLoot).findFirst();
    }

    public final long getCurrentExperience() {
        try {
            Stats stats = getApi().getPlayerProfile().getStats();
            return stats.getExperience();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    protected final String getRuntime() {
        return "";
    }

    public List<Pokestop> getNearbyPokestops() {
        return getPokestops().stream().filter(pokestop ->
                getCurrentLocation().getEarthDistance(S2LatLng.fromDegrees(pokestop.getLatitude(), pokestop.getLongitude())) <= options.getMaxDistance() 
//                && pokestop.getCooldownCompleteTimestampMs()==0
                ).sorted(
                (Pokestop a, Pokestop b) ->
                        Double.compare(
                                getCurrentLocation().getEarthDistance(S2LatLng.fromDegrees(a.getLatitude(), a.getLongitude())),
                                getCurrentLocation().getEarthDistance(S2LatLng.fromDegrees(b.getLatitude(), b.getLongitude())))
        ).collect(Collectors.toList());
    }


    public List<ReleasePokemonResponseOuterClass.ReleasePokemonResponse.Result> doTransfers(boolean forceTransfer) throws LoginFailedException, RemoteServerException {
        if (!options.isTransferPokemon() && !forceTransfer)
            return new ArrayList<>();
        TransferPokemonActivity a = new TransferPokemonActivity(this, options);
        return a.transferPokemon();
    }


    public void manageEggs() {
        if (!options.isManageEggs())
            return;
        try {
            HatchEgg.getHatchedEggs(logger, getInventory().getHatchery()).forEach(hatchedEgg -> {
                //TODO log egg xp etc
            });

            final List<EggIncubator> filled = HatchEgg.fillIncubators(logger, getInventory());
            if (filled.size() > 0) {
                //TODO log filled incubator with egg
            }

            getInventory().getIncubators().stream().filter((incubator1) -> {
            return incubator1.isInUse();
            }).forEach(incubator -> {
                //TODO log egg stats such as distance
            });

        } catch (AsyncPokemonGoException e) {
            logger.debug("Error managing eggs", e);
        }
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

    protected synchronized final State updateOpStatus(State status) {
        State lastOperation = this.currentOperation;
        this.currentOperation = status;

        if (lastOperation != currentOperation)
            logger.debug("Switching from " + lastOperation + " to " + this.currentOperation);

        return lastOperation;
    }

    @Override
    public final Map getMap() {
        return getApi().getMap();
    }

    public List<CatchResult> catchNearbyPokemon() throws LoginFailedException, RemoteServerException {
        updateOpStatus(State.CATCHING);
        List<CatchablePokemon> catchablePokemon = getCatchablePokemon();
        if (catchablePokemon.size() == 0 || options.isCatchPokemon()) {
        	logger.info("No Pokemon nearby");
            return new ArrayList<>();
        }

        logger.info("Nearby Pokemons: " + catchablePokemon.size());
//        for(CatchablePokemon pm : catchablePokemon){
//        	logger.info("\t" + pm.getPokemonIdValue());
//        }

        return CatchPokemon.catchPokemon(logger, this, catchablePokemon);
    }

    public final Collection<Pokestop> getPokestops() {
        try {
            return getMap().getMapObjects().getPokestops();
        } catch (AsyncPokemonGoException | RemoteServerException | LoginFailedException e) {
            logger.debug("Error getting pokestops", e);
        }

        return new ArrayList<>();
    }

    public Collection<FortDataOuterClass.FortData> getGyms() {
        try {
            return getMap().getMapObjects().getGyms();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return new ArrayList<>();
    }

    public List<CatchablePokemon> getCatchablePokemon() {
        try {
            getCurrentLocation();
            return getMap().getCatchablePokemon();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public synchronized List<PokestopLootResult> lootNearbyPokestops(boolean walkToStops) throws LoginFailedException, RemoteServerException {

        if (options.isLootPokestops())
            return new ArrayList<>();

        final S2LatLng origin = getCurrentLocation();

        List<Pokestop> pokestops = getNearbyPokestops();
        final List<PokestopLootResult> results = LootPokestop.lootPokestops(logger, pokestops);

        if (!walkToStops) {
            return results;
        }

        for(Pokestop p : pokestops){
        	if(!p.canLoot())
        		continue;
        	
            double distance = options.getStartingLocation().getEarthDistance(S2LatLng.fromDegrees(p.getLatitude(), p.getLongitude()));
            if (distance < options.getMaxDistance()) {
				botWalker.runTo(getCurrentLocation(), S2LatLng.fromDegrees(p.getLatitude(), p.getLongitude()));
                
                try {
					results.add(lootPokestop(p));
				} catch (LoginFailedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }

        botWalker.runTo(getCurrentLocation(), origin);

        return results;
    }

    
    public PokestopLootResult lootPokestop(Pokestop pokestop) throws LoginFailedException, RemoteServerException {
        updateOpStatus(State.LOOTING);
        PokestopLootResult result = LootPokestop.lootPokestop(logger, pokestop);
        if(result==null){
        	return null;
        }
        if(result.getResult()==Result.OUT_OF_RANGE){
        	if(api.hasChallenge()){
        		VerifyCaptcha.completeCaptcha(api, api.getChallengeURL(), logger);
        		while(api.hasChallenge())
                	sleep(1000*60*60);		// wait until captcha is resolved
        	}
        	
        	longSleep();
        	result = LootPokestop.lootPokestop(logger, pokestop);
        	if(result==null)
        		return null;
        }
        else if(result.getResult()==Result.INVENTORY_FULL){
        	dropItems();
        	
        	sleep(5000); // sleep 5 seconds after dropping items
        	result = LootPokestop.lootPokestop(logger, pokestop);
        }
        
        if(result.wasSuccessful()){
        	String msg = "Awarded: ";
        	List<ItemAward> awards = result.getItemsAwarded();
        	for(ItemAward award : awards){
        		msg += award.getItemId() + ": " + award.getItemCount() + ", ";
        	}
        	logger.debug(msg);
        }

        return result;
    }

    
    
    public final S2LatLng getStartLocation() {
        return options.getStartingLocation();
    }

    public synchronized final PokemonGo getApi() {
        return api;
    }

    public Options getOptions() {
        return options;
    }

    public final synchronized S2LatLng setCurrentLocation(S2LatLng newLocation) {
        getApi().setLocation(newLocation.latDegrees(), newLocation.lngDegrees(), 1);
        logger.debug("new location: [" + newLocation.latDegrees() + ", " + newLocation.lngDegrees() + "]");
        return newLocation;
    }

    public final synchronized S2LatLng getCurrentLocation() {
        return S2LatLng.fromDegrees(getApi().getLatitude(), getApi().getLongitude());
    }

    public synchronized BotWalker getWalker() {
        return botWalker;
    }

    public synchronized void setWalker(BotWalker botWalker) {
        this.botWalker = botWalker;
    }

    

}
