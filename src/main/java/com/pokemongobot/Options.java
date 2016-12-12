package com.pokemongobot;

import com.pokegoapi.google.common.geometry.S2LatLng;

import java.net.InetSocketAddress;
import java.util.List;

public class Options {

    private String name;

    private InetSocketAddress proxy;
    private String proxyCredentials;
    private boolean google;
    private String username;
    private String password;

    private S2LatLng startingLocation;
    private int locationUpdateIntervalMs;
    private int heartBeatIntervalMs;
    
	private double walkingStepDistance;
    private double maxWalkingSpeed;
    private double avgWalkingSpeed;
    private double speedRange;
    
	private double runningStepDistance;
    private double maxDistance;
    private double timeReset;

	private S2LatLng snipeLocation;
    private String snipePokemonName;

    private boolean snipe;
	private int snipeLocationUpdateIntervalMs;
    private int snipeHeartBeatIntervalMs;
    private double snipeAvgWalkingSpeed;
    private double SnipeSpeedRange;
    

    private int ballsToKeep;
    private int potionsToKeep;
    private int revivesToKeep;
    private int berriesToKeep;
    
    private boolean catchPokemon;
    private boolean lootPokestops;
    private boolean manageEggs;
    private boolean evolve;
    private List<String> keepUnevolved;
    private List<String> onlyEvolvePokemons;

    private boolean transferPokemon;
    private boolean ivOverCp;
    private int iv;
    private int cp;
    private List<String> obligatory;
    private List<String> protect;
    
    public class TransferFilter{
    	public int level_range;
    	public int iv_min;
    	public TransferFilter(int level_range, int iv_min){
    		this.level_range = level_range;
    		this.iv_min = iv_min;
    	}
    }
    private List<TransferFilter> transferFilters;

    public Options() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isManageEggs() {
        return manageEggs;
    }

    public void setManageEggs(boolean manageEggs) {
        this.manageEggs = manageEggs;
    }

    public boolean isIvOverCp() {
        return ivOverCp;
    }

    public void setIvOverCp(boolean ivOverCp) {
        this.ivOverCp = ivOverCp;
    }

    public InetSocketAddress getProxy() {
        return proxy;
    }

    public void setProxy(InetSocketAddress proxy) {
        this.proxy = proxy;
    }

    public String getProxyCredentials() {
        return proxyCredentials;
    }

    public void setProxyCredentials(String proxyCredentials) {
        this.proxyCredentials = proxyCredentials;
    }

    public boolean isGoogle() {
        return google;
    }

    public void setGoogle(boolean google) {
        this.google = google;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public S2LatLng getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(S2LatLng startingLocation) {
        this.startingLocation = startingLocation;
    }

    public double getWalkingStepDistance() {
        return walkingStepDistance;
    }

    public void setWalkingStepDistance(double walkingStepDistance) {
        this.walkingStepDistance = walkingStepDistance;
    }

    public double getMaxWalkingSpeed() {
        return maxWalkingSpeed;
    }

    public void setMaxWalkingSpeed(double maxWalkingSpeed) {
        this.maxWalkingSpeed = maxWalkingSpeed;
    }

    public double getRunningStepDistance() {
        return runningStepDistance;
    }

    public void setRunningStepDistance(double runningStepDistance) {
        this.runningStepDistance = runningStepDistance;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public double getTimeReset() {
        return timeReset;
    }

    public void setTimeReset(double timeReset) {
        this.timeReset = timeReset;
    }

    public boolean isCatchPokemon() {
        return catchPokemon;
    }

    public void setCatchPokemon(boolean catchPokemon) {
        this.catchPokemon = catchPokemon;
    }

    public boolean isLootPokestops() {
        return lootPokestops;
    }

    public void setLootPokestops(boolean lootPokestops) {
        this.lootPokestops = lootPokestops;
    }

    public boolean isEvolve() {
        return evolve;
    }

    public void setEvolve(boolean evolve) {
        this.evolve = evolve;
    }

    public List<String> getKeepUnevolved() {
        return keepUnevolved;
    }

    public void setKeepUnevolved(List<String> keepUnevolved) {
        this.keepUnevolved = keepUnevolved;
    }

    public List<String> getOnlyEvolvePokemons() {
        return onlyEvolvePokemons;
    }

    public void setOnlyEvolvePokemons(List<String> onlyEvolvePokemons) {
        this.onlyEvolvePokemons = onlyEvolvePokemons;
    }

    public boolean isTransferPokemon() {
        return transferPokemon;
    }

    public void setTransferPokemon(boolean transferPokemon) {
        this.transferPokemon = transferPokemon;
    }

    public int getIv() {
        return iv;
    }

    public void setIv(int iv) {
        this.iv = iv;
    }

    public int getCp() {
        return cp;
    }

    public void setCp(int cp) {
        this.cp = cp;
    }

    public List<String> getObligatory() {
        return obligatory;
    }

    public void setObligatory(List<String> obligatory) {
        this.obligatory = obligatory;
    }

    public List<String> getProtect() {
        return protect;
    }

    public void setProtect(List<String> protect) {
        this.protect = protect;
    }
    
    public List<TransferFilter> getTransferFilters(){
    	return transferFilters;
    }

    public void setTransferFilters(List<TransferFilter> filters){
    	transferFilters = filters;
    }

    public int getLocationUpdateIntervalMs() {
		return locationUpdateIntervalMs;
	}

	public void setLocationUpdateIntervalMs(int locationUpdateIntervalMs) {
		this.locationUpdateIntervalMs = locationUpdateIntervalMs;
	}

    public double getAvgWalkingSpeed() {
		return avgWalkingSpeed;
	}

	public void setAvgWalkingSpeed(double avgWalkingSpeed) {
		this.avgWalkingSpeed = avgWalkingSpeed;
	}

	public double getSpeedRange() {
		return speedRange;
	}

	public void setSpeedRange(double speedRange) {
		this.speedRange = speedRange;
	}

	public int getHeartBeatIntervalMs() {
		return heartBeatIntervalMs;
	}

	public void setHeartBeatIntervalMs(int heartBeatIntervalMs) {
		this.heartBeatIntervalMs = heartBeatIntervalMs;
	}


    public int getBallsToKeep() {
		return ballsToKeep;
	}

	public void setBallsToKeep(int ballsToKeep) {
		this.ballsToKeep = ballsToKeep;
	}

	public int getPotionsToKeep() {
		return potionsToKeep;
	}

	public void setPotionsToKeep(int potionsToKeep) {
		this.potionsToKeep = potionsToKeep;
	}

	public int getRevivesToKeep() {
		return revivesToKeep;
	}

	public void setRevivesToKeep(int revivesToKeep) {
		this.revivesToKeep = revivesToKeep;
	}

	public int getBerriesToKeep() {
		return berriesToKeep;
	}

	public void setBerriesToKeep(int berriesToKeep) {
		this.berriesToKeep = berriesToKeep;
	}
    
    public S2LatLng getSnipeLocation() {
		return snipeLocation;
	}

	public void setSnipeLocation(S2LatLng snipeLocation) {
		this.snipeLocation = snipeLocation;
	}

	public String getSnipePokemonName() {
		return snipePokemonName;
	}

	public void setSnipePokemonName(String snipePokemonName) {
		this.snipePokemonName = snipePokemonName;
	}

    public int getSnipeLocationUpdateIntervalMs() {
		return snipeLocationUpdateIntervalMs;
	}

	public void setSnipeLocationUpdateIntervalMs(int snipeLocationUpdateIntervalMs) {
		this.snipeLocationUpdateIntervalMs = snipeLocationUpdateIntervalMs;
	}

	public int getSnipeHeartBeatIntervalMs() {
		return snipeHeartBeatIntervalMs;
	}

	public void setSnipeHeartBeatIntervalMs(int snipeHeartBeatIntervalMs) {
		this.snipeHeartBeatIntervalMs = snipeHeartBeatIntervalMs;
	}

	public double getSnipeAvgWalkingSpeed() {
		return snipeAvgWalkingSpeed;
	}

	public void setSnipeAvgWalkingSpeed(double snipeAvgWalkingSpeed) {
		this.snipeAvgWalkingSpeed = snipeAvgWalkingSpeed;
	}

	public double getSnipeSpeedRange() {
		return SnipeSpeedRange;
	}

	public void setSnipeSpeedRange(double snipeSpeedRange) {
		SnipeSpeedRange = snipeSpeedRange;
	}
	
	public boolean isSnipe() {
		return snipe;
	}

	public void setSnipe(boolean snipe) {
		this.snipe = snipe;
	}


}
