
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.CrossSine;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;

public final class ServerUtils extends MinecraftInstance {

    public static ServerData serverData;

    public static void connectToLastServer() {
        if(serverData == null)
            return;

        mc.displayGuiScreen(new GuiConnecting(new GuiMultiplayer(CrossSine.mainMenu), mc, serverData));
    }

    public static String getRemoteIp() {
        String serverIp = "MainMenu";

        if (mc.isIntegratedServerRunning()) {
            serverIp = "SinglePlayer";
        } else if (mc.theWorld != null && mc.theWorld.isRemote) {
            final ServerData serverData = mc.getCurrentServerData();
            if(serverData != null)
                serverIp = serverData.serverIP;
        }

        return serverIp;
    }
    public static boolean isHypixelDomain(String s1) {
        int chars = 0;
        String str = "www.hypixel.net";

        for (char c : str.toCharArray()) {
            if (s1.contains(String.valueOf(c))) chars++;
        }

        return chars == str.length();
    }
}