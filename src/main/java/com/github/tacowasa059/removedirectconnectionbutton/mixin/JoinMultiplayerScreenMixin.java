package com.github.tacowasa059.removedirectconnectionbutton.mixin;

import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;


@Mixin(GuiMultiplayer.class)
public abstract class JoinMultiplayerScreenMixin extends GuiScreen{

    @Shadow
    private GuiButton btnEditServer;
    @Shadow
    private GuiButton btnSelectServer;
    @Shadow
    private GuiButton btnDeleteServer;
    @Shadow
    public ServerSelectionList serverListSelector;
    @Shadow
    public abstract void selectServer(int p_146790_1_);

    @Inject(method = "createButtons", at = @At("HEAD"), cancellable = true)
    private void removeDirectJoinButton(CallbackInfo ci) {
        this.btnEditServer = this.addButton(new GuiButton(7, this.width / 2 - 154, this.height - 28, 70, 20, I18n.format("selectServer.edit", new Object[0])));
        this.btnDeleteServer = this.addButton(new GuiButton(2, this.width / 2 - 74, this.height - 28, 70, 20, I18n.format("selectServer.delete", new Object[0])));
        this.btnSelectServer = this.addButton(new GuiButton(1, this.width / 2 - 154, this.height - 52, 150, 20, I18n.format("selectServer.select", new Object[0])));
        this.buttonList.add(new GuiButton(3, this.width / 2 + 4, this.height - 52, 150, 20, I18n.format("selectServer.add", new Object[0])));
        this.buttonList.add(new GuiButton(8, this.width / 2 + 4, this.height - 28, 70, 20, I18n.format("selectServer.refresh", new Object[0])));
        this.buttonList.add(new GuiButton(0, this.width / 2 + 4 + 76, this.height - 28, 75, 20, I18n.format("gui.cancel", new Object[0])));
        this.selectServer(this.serverListSelector.getSelected());

        ci.cancel();
    }

    @Redirect(
            method = "keyTyped",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;get(I)Ljava/lang/Object;",
                    ordinal = 0
            )
    )
    private Object redirectButtonGet(List<GuiButton> buttonList, int index) {
        return buttonList.get(1);
    }
}
