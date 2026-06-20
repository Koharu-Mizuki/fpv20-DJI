package com.iung.fpv20.generate.lang;

import com.iung.fpv20.consts.ModBlocks;
import com.iung.fpv20.consts.TranslateKeys;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

public class ChineseLangProvider extends FabricLanguageProvider {
    public ChineseLangProvider(FabricDataOutput dataOutput) {
        super(dataOutput,"zh_cn");
    }

    @Override
    public void generateTranslations(TranslationBuilder translationBuilder) {
        translationBuilder.add(ModBlocks.RECEIVER_BLOCK,"信号接收器");

        translationBuilder.add(TranslateKeys.BTN_OPTION_ENTRY,"FPV 无人机");
        translationBuilder.add(TranslateKeys.TITLE_MAIN_OPTION_SCREEN,"FPV 无人机设置");
        translationBuilder.add(TranslateKeys.BTN_SELECT_CONTROLLER,"选择控制器");
        translationBuilder.add(TranslateKeys.BTN_SELECT,"选择");
        translationBuilder.add(TranslateKeys.RATE_1,"灵敏度: %s");
        translationBuilder.add(TranslateKeys.BTN_CALIBRATE,"校准");

        translationBuilder.add(TranslateKeys.BTN_CALIBRATE_CENTER,"校准摇杆中点");
        translationBuilder.add(TranslateKeys.BTN_CALIBRATE_RANGE,"校准摇杆行程");

        translationBuilder.add(TranslateKeys.BTN_START,"开始");
        translationBuilder.add(TranslateKeys.BTN_END,"结束");

        translationBuilder.add(TranslateKeys.BTN_OPTION,"选项");

        translationBuilder.add(TranslateKeys.BTN_CALIBRATE_MAX_MIN,"校准方法: 最大-最小");
        translationBuilder.add(TranslateKeys.BTN_CALIBRATE_MAX_MID_MIN,"校准方法: 最大-中点-最小");
        translationBuilder.add(TranslateKeys.BTN_CALIBRATE_RAW,"校准方法: 直通");

        translationBuilder.add(TranslateKeys.TEXT_SET_CHANNEL_NAME,"设置通道名称");

        translationBuilder.add(TranslateKeys.BTN_SHOW_INPUT,"显示输入信号");
        translationBuilder.add(TranslateKeys.BTN_BTN_CONFIG,"按钮或开关设置");

        translationBuilder.add(TranslateKeys.BTN_REVERSED,"反转输入信号");
        translationBuilder.add(TranslateKeys.BTN_NO_REVERSED,"不反转输入信号");

        translationBuilder.add(TranslateKeys.BTN_RELOAD_CONFIG,"重新加载配置");

        translationBuilder.add(TranslateKeys.BTN_OSD_ON, "显示摇杆图: 开");
        translationBuilder.add(TranslateKeys.BTN_OSD_OFF, "显示摇杆图: 关");
        translationBuilder.add(TranslateKeys.KEYBINDS_CATEGORY, "FPV无人机");
        translationBuilder.add(TranslateKeys.KEYBIND_OSD, "切换摇杆图显示");
    }


}
