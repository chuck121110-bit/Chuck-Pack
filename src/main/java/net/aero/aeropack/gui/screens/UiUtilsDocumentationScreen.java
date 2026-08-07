package net.aero.aeropack.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;

public class UiUtilsDocumentationScreen extends WindowScreen {
    public UiUtilsDocumentationScreen(GuiTheme theme) {
        super(theme, "UI-Utils Documentation");
    }

    @Override
    public void initWidgets() {
        add(theme.label("UI-Utils - Button Guide")).expandX();
        add(theme.horizontalSeparator()).expandX();
        add(theme.label("Close without packet")).expandX();
        add(theme.label("  Closes the container screen without sending any packet to the server.")).expandX();
        add(theme.label("")).expandX();
        add(theme.label("Send packets: true/false")).expandX();
        add(theme.label("  Toggle whether UI-Utils sends packets to the server.")).expandX();
        add(theme.label("  When false, packets are intercepted and held.")).expandX();
        add(theme.label("")).expandX();
        add(theme.label("Delay packets: true/false")).expandX();
        add(theme.label("  Toggle whether UI-Utils delays packets instead of sending them.")).expandX();
        add(theme.label("  Delayed packets are queued and can be sent later.")).expandX();
        add(theme.label("")).expandX();
        add(theme.label("Leave & send packets")).expandX();
        add(theme.label("  Closes the screen and sends all queued delayed packets.")).expandX();
        add(theme.label("")).expandX();
        add(theme.label("Disconnect & send packets")).expandX();
        add(theme.label("  Disconnects from the server and sends all queued delayed packets.")).expandX();
        add(theme.label("")).expandX();
        add(theme.label("Fabricate packet")).expandX();
        add(theme.label("  Opens the fabricate packet overlay where you can manually construct")).expandX();
        add(theme.label("  ClickSlot or ButtonClick packets to send.")).expandX();
        add(theme.label("")).expandX();
        add(theme.label("Copy GUI Title JSON")).expandX();
        add(theme.label("  Copies the current screen title as a JSON string to your clipboard.")).expandX();
        add(theme.label("")).expandX();
        add(theme.label("Clear Queue / Queue: N")).expandX();
        add(theme.label("  Clear Queue empties the delayed packet queue.")).expandX();
        add(theme.label("  Queue: N shows the current number of queued packets.")).expandX();
        add(theme.label("")).expandX();
        add(theme.label("Resync Inv / Disconnect")).expandX();
        add(theme.label("  Resync Inv resets the screen handler to the player inventory screen.")).expandX();
        add(theme.label("  Disconnect disconnects from the server immediately.")).expandX();
        add(theme.label("")).expandX();
        add(theme.label("- / Spam (xN) / +")).expandX();
        add(theme.label("  Adjust the spam count and send the last fabricated packet N times.")).expandX();
        add(theme.label("")).expandX();
        add(theme.label("Send One / Pop Last")).expandX();
        add(theme.label("  Send One sends the first packet in the queue.")).expandX();
        add(theme.label("  Pop Last removes the last packet from the queue without sending.")).expandX();
        add(theme.label("")).expandX();
        add(theme.label("How to Use")).expandX();
        add(theme.label("  Opens this documentation screen.")).expandX();
    }
}
