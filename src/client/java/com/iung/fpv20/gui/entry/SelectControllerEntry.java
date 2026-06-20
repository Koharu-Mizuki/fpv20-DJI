package com.iung.fpv20.gui.entry;

import com.iung.fpv20.Fpv20;
import com.iung.fpv20.Fpv20Client;
import com.iung.fpv20.consts.Texts;
import com.iung.fpv20.input.Controller;
import com.iung.fpv20.input.DS5Preset;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SelectControllerEntry extends LinerEntry {
    private Screen parent_screen;
    private int controller_id;
    private String controller_name;
    private boolean isDS5;

    public SelectControllerEntry(Screen parent_screen, int controller_id, String controller_name) {
        this(parent_screen, controller_id, controller_name, DS5Preset.isDS5(controller_name));
    }

    public SelectControllerEntry(Screen parent_screen, int controller_id, String controller_name, boolean isDS5) {
        int height = 20;
        int preset_btn_width = 130;
        int gap = 4;
        this.parent_screen = parent_screen;
        this.controller_id = controller_id;
        this.controller_name = controller_name;
        this.isDS5 = isDS5;

        Text label = isDS5
                ? Text.of("[DS5] " + controller_name)
                : Text.of(controller_name);

        int select_width = isDS5
                ? parent_screen.width - preset_btn_width - gap
                : parent_screen.width;

        this.elements.add(ButtonWidget.builder(label, (btn) -> {
            Fpv20.LOGGER.info("selected controller {}", controller_id);

            Fpv20Client.controller = new Controller(this.controller_id);
            Fpv20Client.config.setSelected_controller(this.controller_id);
            Fpv20.LOGGER.info("selected controller {}", controller_id);
            parent_screen.close();
        }).dimensions(0, 0, select_width, height).build());

        if (isDS5) {
            this.elements.add(ButtonWidget.builder(Texts.BTN_DS5_PRESET, (btn) -> {
                // 先选中该 DS5 控制器，再套用默认布局预设
                Fpv20Client.controller = new Controller(this.controller_id);
                Fpv20Client.config.setSelected_controller(this.controller_id);
                DS5Preset.apply(Fpv20Client.controller);
                btn.setMessage(Texts.BTN_PRESET_APPLIED);
            }).dimensions(parent_screen.width - preset_btn_width, 0, preset_btn_width, height).build());
        }
    }
}
