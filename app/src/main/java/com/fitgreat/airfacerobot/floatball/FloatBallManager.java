package com.fitgreat.airfacerobot.floatball;

//import com.fitgreat.airfacerobot.floatball.controller.FloatButtonWindowController;
//import com.fitgreat.airfacerobot.floatball.controller.FloatPanelWindowController;

/**
 * 悬浮球管理器<p>
 *
 * @author zixuefei
 * @since 2020/3/27 0027 10:28
 */
public class FloatBallManager {
//    private void showFloatWindow() {
//        if (isFirst) {
//            AssistantApp assistantApp = (AssistantApp) getApplication();
//            FloatWindowManager floatWindowManager = new FloatWindowManager(this);
//            //填充和浮动面板浮动按钮
//            mFloatPanelVC = new FloatPanelWindowController(this, floatWindowManager);
//            mFloatButtonVC = new FloatButtonWindowController(this, floatWindowManager, mFloatPanelVC);
//            //mFloatForegroundVC = new FloatForegroundWindowController(this, floatWindowManager);
//            mFloatButtonVC.setOnFloatButtonPositionUpdateListener(new FloatButtonWindowController.OnFloatButtonPositionUpdateListener() {
//                @Override
//                public void onFloatButtonPositionUpdate(int newX, int newY) {
//                    mFloatPanelVC.followButtonPosition(newX, newY);
//                }
//            });
//            mFloatPanelVC.setOnPanelSizeChangeCallback(new ControlPanelView.OnPanelSizeChangeCallback() {
//                @Override
//                public void onPanelSizeChange(int newWidth, int newHeight) {
//                    //使用该监听，主要是为了解决第一次进入时，没有手动移动过悬浮球，控制面板没有跟随位置的问题
//                    int buttonX = mFloatButtonVC.getFloatWindow().getX();
//                    int buttonY = mFloatButtonVC.getFloatWindow().getY();
//                    mFloatPanelVC.followButtonPosition(buttonX, buttonY);
//                }
//            });
//            mFloatButtonVC.setOnStatusChangeListener(new FloatButtonWindowController.OnStatusChangeListener() {
//                @Override
//                public boolean onPrepareStatusChange(int prepareStatus) {
//                    return mFloatPanelVC.isCanChangeStatus();
//                }
//
//                @Override
//                public void onStatusChange(int newStatus) {
//                }
//            });
//            mFloatTimeTaskHolder = FloatTimeTaskHolder.create(CoreAccessibilityService.this.getApplicationContext(), mFloatButtonVC);
//            FloatViewLiveData floatViewLiveData = assistantApp.getFloatViewLiveData();
//            floatViewLiveData.addOnDataChangeCallback(new FloatViewLiveData.OnDataChangeCallback() {
//                @Override
//                public void onDataChange(boolean isOpen) {
//                    //这里统一做UI切换
//                    if (isOpen) {
//                        mFloatButtonVC.open();
//                        mFloatPanelVC.open();
//                        AppBroadcastManager
//                                .sendBroadcast(CoreAccessibilityService.this,
//                                        AccessibilityConstant.Action.ACTION_FLOAT_BUTTON_OPEN);
//                    } else {
//                        mFloatButtonVC.off();
//                        mFloatPanelVC.off();
//                        AppBroadcastManager
//                                .sendBroadcast(CoreAccessibilityService.this,
//                                        AccessibilityConstant.Action.ACTION_FLOAT_BUTTON_CLOSE);
//                    }
//                }
//            });
//            isFirst = false;
//        } else {
//            if (mFloatButtonVC != null) {
//                mFloatButtonVC.showFloatWindow();
//            }
//            if (mFloatForegroundVC != null) {
//                mFloatForegroundVC.showFloatWindow();
//            }
//        }
//    }
//
//    private void hideFloatWindow() {
//        if (mFloatButtonVC != null) {
//            mFloatButtonVC.hideFloatWindow();
//        }
//        if (mFloatForegroundVC != null) {
//            mFloatForegroundVC.hideFloatWindow();
//        }
//    }
}
