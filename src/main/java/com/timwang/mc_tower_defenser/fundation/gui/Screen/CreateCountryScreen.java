package com.timwang.mc_tower_defenser.fundation.gui.Screen;

import com.timwang.mc_tower_defenser.fundation.network.payloads.RegisterNationPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * 创建国家的屏幕
 * 这是一个客户端 Screen，不需要与 Menu 绑定，因为不涉及物品同步
 *
 * 坐标系说明：
 * - 原点在屏幕左上角 (0, 0)
 * - X 轴向右为正
 * - Y 轴向下为正
 * - 当屏幕缩放时，init() 会自动被调用，需要重新计算所有组件位置
 */
@OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
public class CreateCountryScreen extends Screen {

    // GUI 面板尺寸
    private static final int PANEL_WIDTH = 256;
    private static final int PANEL_HEIGHT = 220;

    // GUI 面板位置（相对于屏幕左上角）
    private int leftPos;
    private int topPos;

    // 国家名称输入框
    private EditBox countryNameInput;

    // 上一个屏幕（用于返回）
    @Nullable
    private final Screen previousScreen;
    
    /**
     * 构造函数
     * @param previousScreen 返回的屏幕
     */
    public CreateCountryScreen(Screen previousScreen) {
        super(Component.literal("创建国家"));
        this.previousScreen = previousScreen;
    }
    
    /**
     * 初始化屏幕
     * 当以下情况发生时会被调用：
     * 1. 屏幕首次打开
     * 2. 窗口大小改变
     * 3. GUI 缩放设置改变
     * 
     * 因此需要在这里重新计算所有组件位置
     */
    @Override
    protected void init() {
        super.init();
        
        // 计算 GUI 面板的位置（屏幕居中）
        this.leftPos = (this.width - PANEL_WIDTH) / 2;
        this.topPos = (this.height - PANEL_HEIGHT) / 2;
        
        // 清除之前的组件（防止重复添加）
        this.clearWidgets();
        
        // 创建国家名称输入框
        this.countryNameInput = new EditBox(
            this.font,
            this.leftPos + 20,      // X 坐标：面板左边距 20 像素
            this.topPos + 50,       // Y 坐标：面板上边距 50 像素
            PANEL_WIDTH - 40,       // 宽度：面板宽度 - 40（左右各 20）
            20,                     // 高度：20 像素
            Component.literal("国家名称")
        );
        this.countryNameInput.setMaxLength(32);
        this.countryNameInput.setValue(""); // 初始值为空
        this.addRenderableWidget(this.countryNameInput);

        // 创建"创建"按钮
        this.addRenderableWidget(
            Button.builder(Component.literal("创建"), (button) -> this.onClickCreate())
                .pos(this.leftPos + 50, this.topPos + 170)   // 位置：面板内左下方
                .size(60, 20)                               // 大小：60x20
                .build()
        );

        // 创建"取消"按钮
        this.addRenderableWidget(
            Button.builder(Component.literal("取消"), (button) -> this.onClickCancel())
                .pos(this.leftPos + 150, this.topPos + 170)  // 位置：面板内右下方
                .size(60, 20)                                // 大小：60x20
                .build()
        );

        // 设置焦点到输入框
        this.setInitialFocus(this.countryNameInput);
    }
    
    /**
     * 渲染屏幕
     * 此方法每帧调用一次
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 绘制背景
        //this.renderBg(guiGraphics);

        // 调用父类渲染方法（渲染所有组件）
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制面板标题
        guiGraphics.drawCenteredString(
            this.font,
            "创建你的国家",
            this.leftPos + PANEL_WIDTH / 2,  // X 坐标：面板水平中心
            this.topPos + 20,                // Y 坐标：面板上方 20 像素
            0xFFFFFF                         // 颜色：白色
        );

        // 绘制输入框标签
        guiGraphics.drawString(
            this.font,
            "国家名称:",
            this.leftPos + 20,    // X 坐标
            this.topPos + 35,     // Y 坐标
            0xFFFFFF              // 颜色：白色
        );
    }

    /**
     * 渲染背景
     */
    public void renderBg(GuiGraphics guiGraphics) {
        // 渲染半透明黑色背景（覆盖整个屏幕）
        guiGraphics.fillGradient(
            0, 0,
            this.width, this.height,
            0xCC000000,  // 起始颜色：半透明黑色
            0xCC000000   // 结束颜色：半透明黑色
        );
        
        // 绘制 GUI 面板背景
        guiGraphics.fill(
            this.leftPos, this.topPos,
            this.leftPos + PANEL_WIDTH, this.topPos + PANEL_HEIGHT,
            0xFF8B8B8B  // 颜色：灰色
        );
        
        // 绘制面板边框（4 条边）
        int borderColor = 0xFFFFFFFF;  // 白色边框
        int borderWidth = 2;
        
        // 上边框
        guiGraphics.fill(
            this.leftPos - borderWidth, this.topPos - borderWidth,
            this.leftPos + PANEL_WIDTH + borderWidth, this.topPos,
            borderColor
        );
        
        // 左边框
        guiGraphics.fill(
            this.leftPos - borderWidth, this.topPos,
            this.leftPos, this.topPos + PANEL_HEIGHT,
            borderColor
        );
        
        // 右边框
        guiGraphics.fill(
            this.leftPos + PANEL_WIDTH, this.topPos,
            this.leftPos + PANEL_WIDTH + borderWidth, this.topPos + PANEL_HEIGHT + borderWidth,
            borderColor
        );
        
        // 下边框
        guiGraphics.fill(
            this.leftPos - borderWidth, this.topPos + PANEL_HEIGHT,
            this.leftPos + PANEL_WIDTH + borderWidth, this.topPos + PANEL_HEIGHT + borderWidth,
            borderColor
        );
    }
    
    /**
     * "创建"按钮的点击回调
     * 此方法在玩家点击"创建"按钮时被调用
     */
    private void onClickCreate() {
        String countryName = this.countryNameInput.getValue();

        // 验证国家名称是否为空
        if (countryName.isEmpty() || countryName.trim().isEmpty()) {
            // 名称为空，可以添加错误提示（暂时不处理）
            return;
        }

        // TODO: 发送网络包到服务端创建国家
        // PacketHandler.INSTANCE.sendToServer(new CreateCountryPacket(countryName));
        // System.out.println("创建国家: " + countryName);
        PacketDistributor.sendToServer(new RegisterNationPayloads("test","test"));

        // 关闭此屏幕，返回到之前的屏幕
        this.onClose();
    }

    /**
     * "取消"按钮的点击回调
     * 此方法在玩家点击"取消"按钮时被调用
     */
    private void onClickCancel() {
        // 直接关闭此屏幕，返回到之前的屏幕
        this.onClose();
    }

    @Override
    public void removed() {
        super.removed();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    /**
     * 处理键盘按下事件
     * 此方法在玩家按下键盘按键时被调用
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 按下 Escape 键时关闭屏幕
        if (keyCode == 256) {  // Escape 键的代码
            this.onClickCancel();
            return true;
        }
        
        // 按下 Enter 键时创建国家
        if (keyCode == 257) {  // Enter 键的代码
            this.onClickCreate();
            return true;
        }
        
        // 调用父类方法处理其他按键
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    /**
     * 设置此屏幕是否暂停游戏
     * @return true 时打开此屏幕会暂停游戏
     */
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
