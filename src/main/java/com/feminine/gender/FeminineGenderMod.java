package com.feminine.gender;

import com.feminine.gender.network.GenderNetworking;

import net.ornithemc.osl.entrypoints.api.ModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FeminineGenderMod implements ModInitializer {

    public static final String MOD_ID = "feminine_gender_mod";
    public static final Logger LOGGER = LogManager.getLogger("Feminine Gender Mod");

    @Override
    public void init() {
        GenderNetworking.initServer();
    }
}
