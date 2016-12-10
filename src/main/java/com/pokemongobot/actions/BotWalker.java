package com.pokemongobot.actions;

import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokegoapi.google.common.geometry.S2LatLng;
import com.pokemongobot.Options;
import com.pokemongobot.PokemonBot;
import com.pokemongobot.listeners.HeartBeatListener;
import com.pokemongobot.listeners.LocationListener;
import com.pokemongobot.tasks.BotActivity;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class BotWalker {

    private static double MAX_WALKING_SPEED;
    private static double AVG_WALKING_SPEED;
    private static double SPEED_RANGE;

    private static int LOCATION_UPDATE_INTERVAL_MS;
    
    private final Logger logger;
    private final HeartBeatListener heartBeatListener;
    private final List<BotActivity> postStepActivities = new ArrayList<>();
    private final LocationListener locationListener;
    private final Options options;
    private PokemonBot bot;
    
    private AtomicReference<S2LatLng> currentLocation;
    private AtomicLong currentLocationTimestampMs = new AtomicLong(0);
    private static double currentSpeed;
    
    
    public BotWalker(final PokemonBot bot, final S2LatLng start, final LocationListener locationListener,
                     final HeartBeatListener heartBeatListener, final Options options) {
        this.bot = bot;
        this.currentLocation = new AtomicReference<>(start);
        this.locationListener = locationListener;
        this.heartBeatListener = heartBeatListener;
        this.options = options;
        
        LOCATION_UPDATE_INTERVAL_MS = options.getLocationUpdateIntervalMs();
        
        MAX_WALKING_SPEED = options.getMaxWalkingSpeed();
        AVG_WALKING_SPEED = options.getAvgWalkingSpeed();
        SPEED_RANGE = options.getSpeedRange();
        currentSpeed = AVG_WALKING_SPEED;
        
        logger = Logger.getLogger(Thread.currentThread().getName());
    }

    protected static long getTimeoutForDistance(double distance) {
        if (Double.isInfinite(distance) || Double.isNaN(distance) || (Double.compare(distance, 1) < 1)) {
            return 0;
        }
        Double ms = ((distance / MAX_WALKING_SPEED)) + 75;
        return ms.longValue();
    }

    public synchronized void addPostStepActivity(BotActivity activity) {
        this.postStepActivities.add(activity);
    }

    public synchronized void performPostStepActivities() throws LoginFailedException, RemoteServerException {
    	for(BotActivity activity : postStepActivities){
    		activity.performActivity();
    	}
    }

    protected synchronized void performHeartBeat() throws LoginFailedException, RemoteServerException {
        heartBeatListener.heartBeat();
    }

    public synchronized void walkTo(final S2LatLng start, final S2LatLng end) throws LoginFailedException, RemoteServerException {
    	
        S2LatLng[] steps = getStepsToDestination(start, end, randomizeSpeed()*LOCATION_UPDATE_INTERVAL_MS/1000);
        if (steps == null || steps.length==0) {
            setCurrentLocation(end);
            return;
        }

        for (S2LatLng step : steps) {
        	sleep(LOCATION_UPDATE_INTERVAL_MS);
        	
            setCurrentLocation(step);
            performHeartBeat();
        }
    }

    public synchronized void runTo(final S2LatLng origin, final S2LatLng destination) throws LoginFailedException, RemoteServerException {
        S2LatLng[] steps = getStepsToDestination(origin, destination, options.getRunningStepDistance());
        setCurrentLocation(origin);
        if (steps == null) {
            setCurrentLocation(destination);
            return;
        } else if (steps.length == 1) {
            setCurrentLocation(destination);
            performHeartBeat();
            return;
        }
        for (int i = steps.length - 1; i >= 0; i--) {
//            double speed = setCurrentLocation(steps[i]);
            sleep(10); //TODO make random
        }

        longSleep();
    }

    public final synchronized double setCurrentLocation(S2LatLng newLocation) {
        try {
            boolean update = true;
            S2LatLng current = currentLocation.get();
            if (Double.compare(newLocation.latDegrees(), current.latDegrees()) == 0 &&
                    Double.compare(newLocation.lngDegrees(), current.lngDegrees()) == 0)
                update = false;

            newLocation.add(S2LatLng.fromDegrees(getSmallRandom(), getSmallRandom()));

            bot.setCurrentLocation(newLocation);
            double speed = localUpdateAndGetSpeed(newLocation);
            
            if (update)
                locationListener.updateCurrentLocation();

            return speed;
        } catch (Exception e) {
            logger.debug("Error setting current location", e);
        }
        return 0;
    }


    public double getSmallRandom() {
        return Math.random() * 0.0001 - 0.00005;
    }

    public final S2LatLng[] getStepsToDestination(final S2LatLng start, final S2LatLng end, final double stepMeters) {
        if (start.getEarthDistance(end) == 0)
            return new S2LatLng[]{start};
        
        double distance = start.getEarthDistance(end);
        final int stepsRequired = (int) Math.round(distance / stepMeters);
        if(stepsRequired==0){
        	return new S2LatLng[0];
        }

        double deltaLat = (end.latDegrees() - start.latDegrees())/stepsRequired;
        double deltaLng = (end.lngDegrees() - start.lngDegrees())/stepsRequired;

        S2LatLng[] steps = new S2LatLng[stepsRequired];
        S2LatLng previous = start;
        steps[0] = start;
        for (int i = 0; i < stepsRequired; i++) {
            steps[i] = S2LatLng.fromDegrees(previous.latDegrees() + deltaLat, previous.lngDegrees() + deltaLng);
            previous = steps[i];
        }
        return steps;
    }



    protected double localUpdateAndGetSpeed(S2LatLng newLocation) {
        S2LatLng current = currentLocation.get();
        long currentMs = currentLocationTimestampMs.get();
        long newMs = System.currentTimeMillis();

        double speed = 0;
        if (currentMs > 0) {
            double distance = current.getEarthDistance(newLocation);
            speed = distance*1000 / (newMs - currentMs);
        }

        currentLocation.set(newLocation);
        currentLocationTimestampMs.set(newMs);

        return speed;
    }

    protected boolean longSleep() {
        return sleep(new Double((Math.random() * 2000)).intValue() + 1000);
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
    
    
    private double randomizeSpeed() {
        if (SPEED_RANGE > currentSpeed) {
            return currentSpeed;
        }
        double speedDiff = 0.0;
        double minSpeed = currentSpeed - SPEED_RANGE;
        double maxSpeed = currentSpeed + SPEED_RANGE;
        
        // random value between -1 and  +1. There is always a 50:50 chance it will be slower or faster
        // The speedChange is now twice math.random so that it prefers small/slow acceleration, but has still a low chance of abruptly changing (like a human)
        double speedChangeNormalized = (Math.random() * 2 - 1) * Math.random();
        if (speedChangeNormalized > 0) {
            speedDiff = maxSpeed - AVG_WALKING_SPEED;
        } else if (speedChangeNormalized < 0) {
            speedDiff = AVG_WALKING_SPEED - minSpeed;
        }
        currentSpeed = AVG_WALKING_SPEED + speedChangeNormalized * speedDiff;
        return currentSpeed;
    }    

    
    public static void main(String[] args){
    	Options option = new Options();
    	option.setAvgWalkingSpeed(2.4);
    	option.setMaxWalkingSpeed(2.8);
    	option.setSpeedRange(0.2);
    	option.setLocationUpdateIntervalMs(500);
    	
    	BotWalker walker = new BotWalker(null, null, null, null, option);
    	S2LatLng start = S2LatLng.fromDegrees(37.3361851, -121.8938675);
    	S2LatLng end = S2LatLng.fromDegrees(37.3369955, -121.8933525);
    	S2LatLng[] steps = walker.getStepsToDestination(start, end, 2.5*LOCATION_UPDATE_INTERVAL_MS/1000);
    	for(S2LatLng step : steps){
    		System.out.println("[" + step.latDegrees() + ", " + step.lngDegrees() + "]");
    	}
    }
    
}
