package com.pokemongobot.listeners;

import com.pokegoapi.exceptions.CaptchaActiveException;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import com.pokemongobot.tasks.BotActivity;

import java.util.List;

public interface HeartBeatListener {

    void heartBeat() throws LoginFailedException, RemoteServerException, CaptchaActiveException;

    int incrementHeartBeat();

    int getHeartBeatCount();

    void setHeartBeatCount(int count);

    void addHeartBeatActivity(BotActivity activity);

    List<BotActivity> getHeartbeatActivities();

}
