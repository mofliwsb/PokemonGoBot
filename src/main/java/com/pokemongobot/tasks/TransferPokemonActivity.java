package com.pokemongobot.tasks;

import POGOProtos.Networking.Responses.ReleasePokemonResponseOuterClass.ReleasePokemonResponse.Result;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokemongobot.Options;
import com.pokemongobot.PokemonBot;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class TransferPokemonActivity implements BotActivity {

    private final Logger logger;
    private final PokemonBot bot;
    private final Options options;

    private final boolean test = false;
    private final String testName = "DRATINI";
    
    public TransferPokemonActivity(PokemonBot bot, Options options) {
        this.bot = bot;
        this.options = options;
        this.logger = Logger.getLogger(options.getName());
    }

    @Override
    public void performActivity() throws LoginFailedException, RemoteServerException {
        this.transferPokemon();
    }

    public List<Result> transferPokemon() throws LoginFailedException, RemoteServerException {
        List<Pokemon> pokemonList = bot.getInventory().getPokebank().getPokemons();
        if(pokemonList==null || pokemonList.size()==0)
        	return new ArrayList<Result>();
        
        Pokemon[] pokemonArray = new Pokemon[0];
        Pokemon[] pokemons = pokemonList.toArray(pokemonArray);
        List<Result> transferred = new ArrayList<>();
        
        List<Options.TransferFilter> transferFilters = options.getTransferFilters();

        int playerLevel = bot.getApi().getPlayerProfile().getStats().getLevel();
        
        for(Pokemon p : pokemons){
            boolean protect = false;
            boolean obligatory = false;
            boolean good = false;
            
            for (String name : options.getProtect()) {
                if (p.getPokemonId().name().equalsIgnoreCase(name)) {
                    protect = true;
                    break;
                }
            }

            for (String name : options.getObligatory()) {
                if (p.getPokemonId().name().equalsIgnoreCase(name)) {
                    obligatory = true;
                    break;
                }
            }

            int pokemonLevel = (int)p.getLevel();
            int pokemonIV = (int)p.getIvInPercentage();
            int levelDiff = playerLevel - pokemonLevel;
            
            for(int i=0;i<transferFilters.size();i++){
            	Options.TransferFilter filter = transferFilters.get(i);
            	if(levelDiff <= filter.level_range && pokemonIV >= filter.iv_min)
            		good = true;
            }
            
            if( ( !test || (test && p.getPokemonId().name().equals(testName)) )
            	&& (!protect && !p.isFavorite()) && (!good || obligatory) ){

            	Result result = p.transferPokemon();
                logger.info("transfered pokemon: " + p.getPokemonId().name() + ", CP: " + p.getCp() + ", level: " + p.getLevel() + ", IV: " + (int)p.getIvInPercentage());
                transferred.add(result);
            }
        }
        
        return transferred;
    }

}
