package com.pokemongobot;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.exceptions.AsyncPokemonGoException;
import com.pokegoapi.exceptions.LoginFailedException;

import org.apache.log4j.Logger;

public class BotRunner extends Thread {

    private final Options options;
    private Logger logger;

    public BotRunner(Options options) {
        this.options = options;
        this.setName(options.getName());
    }

    @Override
    public void run() {
    	while(true){
	        try {
	            logger = Logger.getLogger(options.getName());
	            PokemonGo pokemonGo = Main.buildPokemonGo(this.options);
	            
	            if(options.isSnipe()){
		            SniperBot sniperBot = new SniperBot(pokemonGo, this.options);
		            sniperBot.run();
	            }else{
		            SimplePokemonBot simplePokemonBot = new SimplePokemonBot(pokemonGo, this.options);
		            Thread.sleep(500);
		            simplePokemonBot.run();
//		            simplePokemonBot.testPokestop();
//		            simplePokemonBot.dropItems();
//		            simplePokemonBot.doTransfers(true);
	            }
	        } catch (LoginFailedException | AsyncPokemonGoException e) {
	            logger.debug("Error running Bot " + this.getName(), e);
	            if(e.getMessage().contains("Invalid Auth status code recieved") || 
	            		e.getMessage().equals("Check auth required")){
	            	logger.debug("Trying to restart bot...");
	            	try {
						Thread.sleep(1*60*1000);
					} catch (InterruptedException e1) {
					}
	            	continue;
	            }
	        }
	        catch (Exception e) {
	            logger.debug("Error Starting " + this.getName(), e);
	            break;
	        }
    	}
    }

}
