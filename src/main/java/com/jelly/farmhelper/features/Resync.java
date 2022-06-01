package com.jelly.farmhelper.features;

import com.jelly.farmhelper.FarmHelper;
import com.jelly.farmhelper.config.enums.CropEnum;
import com.jelly.farmhelper.config.interfaces.FarmConfig;
import com.jelly.farmhelper.utils.Clock;
import com.jelly.farmhelper.utils.LogUtils;
import com.jelly.farmhelper.world.GameState;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCarrot;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.Sys;
import scala.tools.reflect.quasiquotes.Parsers;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Resync {
    private static BlockPos cachedPos;
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final ScheduledExecutorService executor;

    static {
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public static void update(BlockPos lastBrokenPos) {
        cachedPos = lastBrokenPos;
        executor.schedule(checkCrop, 4, TimeUnit.SECONDS);
    }

    static Runnable checkCrop = () -> {
        if (cachedPos != null && mc.theWorld.getBlockState(cachedPos) != null) {
            boolean desync = false;
            switch (FarmConfig.cropType) {
                case NETHERWART:
                    if (mc.theWorld.getBlockState(cachedPos).getValue(BlockNetherWart.AGE) > 2) desync = true;
                    break;
                case SUGARCANE:
                    if (mc.theWorld.getBlockState(cachedPos).getBlock().equals(Blocks.reeds)) desync = true;
                    break;
                default:
                    if (mc.theWorld.getBlockState(cachedPos).getValue(BlockCrops.AGE) > 4) desync = true;
            }

            if (desync && FarmHelper.gameState.currentLocation == GameState.location.ISLAND) {
                LogUtils.debugLog("Desync detected");
                LogUtils.webhookLog("Desync detected");
                mc.thePlayer.sendChatMessage("/hub");
            }
        }
    };
}