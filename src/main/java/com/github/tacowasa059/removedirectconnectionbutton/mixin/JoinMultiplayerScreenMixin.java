package com.github.tacowasa059.removedirectconnectionbutton.mixin;

import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    @Shadow
    private  Screen lastScreen;
    @Shadow
    protected ServerSelectionList serverSelectionList;
    @Shadow
    private ServerList servers;
    @Shadow
    private Button editButton;
    @Shadow
    private Button selectButton;
    @Shadow
    private Button deleteButton;
    @Shadow
    private ServerData editingServer;
    @Shadow
    private LanServerDetector.LanServerList lanServerList;
    @Shadow
    private LanServerDetector.LanServerFindThread lanServerDetector;
    @Shadow
    private boolean initedOnce;

    protected JoinMultiplayerScreenMixin(ITextComponent p_i51108_1_) {
        super(p_i51108_1_);
    }

    @Shadow
    protected abstract void joinSelectedServer();

    @Shadow
    protected abstract void addServerCallback(boolean p_99722_);
    @Shadow
    protected abstract void editServerCallback(boolean p_99726_);
    @Shadow
    protected abstract void deleteCallback(boolean p_99726_);
    @Shadow
    protected abstract void onSelectedChange();
    @Shadow
    protected abstract void refreshServerList();

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void removeDirectJoinButton(CallbackInfo ci) {
        MultiplayerScreen screen = (MultiplayerScreen) (Object)this;
        super.init();
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        if (this.initedOnce) {
            this.serverSelectionList.updateSize(this.width, this.height, 32, this.height - 64);
        } else {
            this.initedOnce = true;
            this.servers = new ServerList(this.minecraft);
            this.servers.load();
            this.lanServerList = new LanServerDetector.LanServerList();

            try {
                this.lanServerDetector = new LanServerDetector.LanServerFindThread(this.lanServerList);
                this.lanServerDetector.start();
            } catch (Exception var2) {
                LOGGER.warn("Unable to start LAN server detection: {}", var2.getMessage());
            }

            this.serverSelectionList = new ServerSelectionList(screen, this.minecraft, this.width, this.height, 32, this.height - 64, 36);
            this.serverSelectionList.updateOnlineServers(this.servers);
        }

        this.children.add(this.serverSelectionList);
        this.selectButton = (Button)this.addButton(new Button(this.width / 2 - 154, this.height - 52, 152, 20, new TranslationTextComponent("selectServer.select"), (p_214293_1_) -> {
            this.joinSelectedServer();
        }));

        this.addButton(new Button(this.width / 2 + 4 + 50, this.height - 52, 152, 20, new TranslationTextComponent("selectServer.add"), (p_214288_1_) -> {
            this.editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", false);
            this.minecraft.setScreen(new AddServerScreen(this, this::addServerCallback, this.editingServer));
        }));
        this.editButton = (Button)this.addButton(new Button(this.width / 2 - 154, this.height - 28, 70, 20, new TranslationTextComponent("selectServer.edit"), (p_214283_1_) -> {
            ServerSelectionList.Entry serverselectionlist$entry = this.serverSelectionList.getSelected();
            if (serverselectionlist$entry instanceof ServerSelectionList.NormalEntry) {
                ServerData serverdata = ((ServerSelectionList.NormalEntry)serverselectionlist$entry).getServerData();
                this.editingServer = new ServerData(serverdata.name, serverdata.ip, false);
                this.editingServer.copyFrom(serverdata);
                this.minecraft.setScreen(new AddServerScreen(this, this::editServerCallback, this.editingServer));
            }

        }));
        this.deleteButton = (Button)this.addButton(new Button(this.width / 2 - 74, this.height - 28, 70, 20, new TranslationTextComponent("selectServer.delete"), (p_214294_1_) -> {
            ServerSelectionList.Entry serverselectionlist$entry = (ServerSelectionList.Entry)this.serverSelectionList.getSelected();
            if (serverselectionlist$entry instanceof ServerSelectionList.NormalEntry) {
                String s = ((ServerSelectionList.NormalEntry)serverselectionlist$entry).getServerData().name;
                if (s != null) {
                    ITextComponent itextcomponent = new TranslationTextComponent("selectServer.deleteQuestion");
                    ITextComponent itextcomponent1 = new TranslationTextComponent("selectServer.deleteWarning", new Object[]{s});
                    ITextComponent itextcomponent2 = new TranslationTextComponent("selectServer.deleteButton");
                    ITextComponent itextcomponent3 = DialogTexts.GUI_CANCEL;
                    this.minecraft.setScreen(new ConfirmScreen(this::deleteCallback, itextcomponent, itextcomponent1, itextcomponent2, itextcomponent3));
                }
            }

        }));
        this.addButton(new Button(this.width / 2 + 4, this.height - 28, 70, 20, new TranslationTextComponent("selectServer.refresh"), (p_214291_1_) -> {
            this.refreshServerList();
        }));
        this.addButton(new Button(this.width / 2 + 4 + 76, this.height - 28, 75, 20, DialogTexts.GUI_CANCEL, (p_214289_1_) -> {
            this.minecraft.setScreen(this.lastScreen);
        }));
        this.onSelectedChange();

        ci.cancel();
    }
}
