package com.github.tacowasa059.removedirectconnectionbutton.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.EditServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.LanServerDetection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen{
    @Shadow
    private static final Logger LOGGER = LogUtils.getLogger();
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
    private LanServerDetection.LanServerList lanServerList;
    @Shadow
    @Nullable
    private LanServerDetection.LanServerDetector lanServerDetector;
    @Shadow
    private boolean initedOnce;

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

    protected JoinMultiplayerScreenMixin(Component p_96550_) {
        super(p_96550_);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void removeDirectJoinButton(CallbackInfo ci) {
        JoinMultiplayerScreen screen = (JoinMultiplayerScreen) (Object)this;
        if (this.initedOnce) {
            this.serverSelectionList.setRectangle(this.width, this.height - 64 - 32, 0, 32);
        } else {
            this.initedOnce = true;
            this.servers = new ServerList(this.minecraft);
            this.servers.load();
            this.lanServerList = new LanServerDetection.LanServerList();

            try {
                this.lanServerDetector = new LanServerDetection.LanServerDetector(this.lanServerList);
                this.lanServerDetector.start();
            } catch (Exception exception) {
                LOGGER.warn("Unable to start LAN server detection: {}", exception.getMessage());
            }

            this.serverSelectionList = new ServerSelectionList(screen, this.minecraft, this.width, this.height - 64 - 32, 32, 36);
            this.serverSelectionList.updateOnlineServers(this.servers);
        }

        this.addWidget(this.serverSelectionList);
        this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.select"), (p_99728_) -> {
            this.joinSelectedServer();
        }).width(152).build());

        Button button1 = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.add"), (p_296190_) -> {
            this.editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", ServerData.Type.OTHER);
            this.minecraft.setScreen(new EditServerScreen(this, this::addServerCallback, this.editingServer));
        }).width(152).build());
        this.editButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.edit"), (p_99715_) -> {
            ServerSelectionList.Entry serverselectionlist$entry = this.serverSelectionList.getSelected();
            if (serverselectionlist$entry instanceof ServerSelectionList.OnlineServerEntry) {
                ServerData serverdata = ((ServerSelectionList.OnlineServerEntry)serverselectionlist$entry).getServerData();
                this.editingServer = new ServerData(serverdata.name, serverdata.ip, ServerData.Type.OTHER);
                this.editingServer.copyFrom(serverdata);
                this.minecraft.setScreen(new EditServerScreen(this, this::editServerCallback, this.editingServer));
            }

        }).width(74).build());
        this.deleteButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.delete"), (p_99710_) -> {
            ServerSelectionList.Entry serverselectionlist$entry = this.serverSelectionList.getSelected();
            if (serverselectionlist$entry instanceof ServerSelectionList.OnlineServerEntry) {
                String s = ((ServerSelectionList.OnlineServerEntry)serverselectionlist$entry).getServerData().name;
                if (s != null) {
                    Component component = Component.translatable("selectServer.deleteQuestion");
                    Component component1 = Component.translatable("selectServer.deleteWarning", s);
                    Component component2 = Component.translatable("selectServer.deleteButton");
                    Component component3 = CommonComponents.GUI_CANCEL;
                    this.minecraft.setScreen(new ConfirmScreen(this::deleteCallback, component, component1, component2, component3));
                }
            }

        }).width(74).build());
        Button button2 = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.refresh"), (p_99706_) -> {
            this.refreshServerList();
        }).width(74).build());
        Button button3 = this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (p_280867_) -> {
            this.minecraft.setScreen(this.lastScreen);
        }).width(74).build());
        LinearLayout linearlayout = LinearLayout.vertical();
        EqualSpacingLayout equalspacinglayout = linearlayout.addChild(new EqualSpacingLayout(308, 20, EqualSpacingLayout.Orientation.HORIZONTAL));
        equalspacinglayout.addChild(this.selectButton);

        equalspacinglayout.addChild(button1);
        linearlayout.addChild(SpacerElement.height(4));
        EqualSpacingLayout equalspacinglayout1 = linearlayout.addChild(new EqualSpacingLayout(308, 20, EqualSpacingLayout.Orientation.HORIZONTAL));
        equalspacinglayout1.addChild(this.editButton);
        equalspacinglayout1.addChild(this.deleteButton);
        equalspacinglayout1.addChild(button2);
        equalspacinglayout1.addChild(button3);
        linearlayout.arrangeElements();
        FrameLayout.centerInRectangle(linearlayout, 0, this.height - 64, this.width, 64);
        this.onSelectedChange();

        ci.cancel();
    }
}
