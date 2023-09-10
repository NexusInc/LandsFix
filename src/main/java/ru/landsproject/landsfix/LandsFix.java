package ru.landsproject.landsfix;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.landsproject.api.command.controller.CommandController;
import ru.landsproject.api.configuration.Configuration;
import ru.landsproject.api.configuration.Type;
import ru.landsproject.api.util.interfaces.Initable;
import ru.landsproject.landsfix.handler.MainHandler;

@Getter
public final class LandsFix extends JavaPlugin implements Initable {

    @Getter
    private static LandsFix instance;
    private Configuration configuration;
    private CommandController commandController;
    @Override
    public void onEnable() {
        instance = this;
        init();
    }

    @Override
    public void onDisable() {
        destruct();
    }


    @Override
    public void init() {
        configuration = new Configuration("config.yml", getDataFolder(), Type.YAML);
        commandController = new CommandController();

        configuration.init();
        commandController.init();

        configuration.useDefaultColorful();

        Bukkit.getPluginManager().registerEvents(new MainHandler(), this);
    }

    @Override
    public void destruct() {
        configuration.destruct();
        commandController.destruct();
    }

}
