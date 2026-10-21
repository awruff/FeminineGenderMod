package com.feminine.gender.client.cloud;

import java.lang.reflect.Field;

import com.feminine.gender.FeminineGenderMod;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;

import net.minecraft.client.Minecraft;

public final class CloudUtils {

    private static final String EXPECTED_JOIN_URL = "https://sessionserver.mojang.com/session/minecraft/join";

    private static boolean loggedSessionTamperWarning = false;

    private CloudUtils() {
    }

    static boolean hasTheSessionServiceBeenTamperedWith() {
        MinecraftSessionService sessionService = Minecraft.getInstance().getSessionService();

        // vanilla always uses yggdrasil here unless something has replaced the session service
        if (sessionService.getClass() != YggdrasilMinecraftSessionService.class) {
            logSessionTamperWarning("Detected likely session service tampering; got {} instead of the expected Yggdrasil session service", sessionService.getClass());
            return true;
        }

        // cracked clients often point the join URL somewhere else
        String joinUrl;
        try {
            Field field = YggdrasilMinecraftSessionService.class.getDeclaredField("JOIN_URL");
            field.setAccessible(true);
            joinUrl = String.valueOf(field.get(null));
        } catch (ReflectiveOperationException e) {
            joinUrl = null;
        }
        if (!EXPECTED_JOIN_URL.equals(joinUrl)) {
            logSessionTamperWarning("Detected likely session service tampering; Yggdrasil join URL is not the expected Mojang-provided value");
            return true;
        }

        return false;
    }

    private static void logSessionTamperWarning(String message, Object... args) {
        if (loggedSessionTamperWarning) {
            return;
        }
        FeminineGenderMod.LOGGER.warn(message, args);
        FeminineGenderMod.LOGGER.warn("Cloud sync will be unavailable for this session");
        loggedSessionTamperWarning = true;
    }
}
