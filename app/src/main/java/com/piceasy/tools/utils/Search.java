//package com.jxtools.wx.utils;
//
//import android.view.accessibility.AccessibilityNodeInfo;
//
//import java.util.Date;
//
///**
// * @author ZW
// * @description:
// * @date : 2020/11/25 11:15
// */
//public class Search {
//    Date start = new Date();
//
//    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
//
//    //读屏幕的变量
//    AccessibilityNodeInfo n1;
//    AccessibilityNodeInfo n2;
//    AccessibilityNodeInfo n3;
//    int childCount;
//    String contentScreen;
//
//    /**
//     * 自己的文件类
//     * 有 输出前后一行 等方法
//     */
//    XFile.Search search;
//
//    //search的变量
//    List<String> aroundSting;
//    String line;
//    String content;
//    String target;                  //撤回前后的内容
//    int size;                       //要找的数量
//    int num = 0;                    //连续撤回的数量
//    boolean flag = false;           //是正着扫还是反着扫
//
//    List<String> screenList = new ArrayList<>();
//    List<String> listMsg = new ArrayList<>();
//
//    //给getScreen调用的无参构造方法
//    Search() {
//    }
//
//    Search(String name) {
//        Log.i(TAG, "Searching...");
//
//        if (name == null) {
//            Log.e(TAG, "name is null !");
//            XToast.makeText(getApplicationContext(),
//                    "无法获取联系人名字 请\nshutdown软件\n打开辅助功能\n打开软件").show();
//            return;
//        }
//
//        try {
//            search = new XFile.Search(MainActivity.File_Dir   name);
//        } catch (IOException e) {
//            XToast.makeText(getApplicationContext(),
//                    "打开软件之前的消息是看不到的").show();
//            e.printStackTrace();
//            return;
//        }
//
//        aroundSting = getPreString();
//        size = aroundSting.size();
//        Log.d(TAG, "size: "   size);
//
//        /**
//         * 从列表右边开始扫 就是先找撤回消息的前一句再根据这个找下一句
//         */
//        flag = false;
//        for (int i = size - 1; i >= 0; i--)
//            scan(i);
//
//        /**
//         * 如果有没找到就换个方向扫 就是先找撤回消息的后一句再根据这个找前一句
//         */
//        if (listMsg.size() == 0 || num > 0) {
//            search.seekEnd();
//            num = 0;
//            flag = true;
//            Log.e(TAG, "scan from bottom");
//            aroundSting = getAftString();
//            size = aroundSting.size();
//            for (int i = 0; i < size; i  )
//                scan(i);
//        }
//
//        /**
//         * 如果还没找到,就从最近写入的地方读一条出来
//         * 最多从最近添加的两条里面找
//         */
//        if (listMsg.size() == 0 || num > 0) {
//            Log.w(TAG, "still not found");
//            search.seekEnd();
//            for (int i = 0; i < 2; ) {
//                line = search.nextLine();
//                content = getContent(line);
//                Log.i(TAG, "content: "   content);
//                if (content.contains(TEXT_WITHDRAW))
//                    continue;
//                i  ;
//                if (!screenList.contains(content)) {
//                    addToListMsg();
//                    num--;
//                    break;
//                }
//            }
//        }
//
//        /**
//         * 如果还是没找全的话
//         * 提醒不能对面整屏的撤回
//         */
//        if (listMsg.size() == 0 || num > 0) {
//            if (screenList.size() == 0) {
//                line = "屏幕内必须有对方说过的一句话\n/请不要暴力测试"
//                new Date().getTime();
//                addToListMsg();
//            }
//        }
//
//        /**
//         * 最后集中写入文件
//         */
//        if (listMsg.size() > 0) {
//            List<String> addedList = new ArrayList<>();
//            WD_MsgList = XListAdapter.MsgList;
//            int size = 0;
//            String addedString = null;
//
//            for (String msg : listMsg)
//                if (!WD_MsgList.contains(getContent(msg))) {
//                    Log.i(TAG, msg   " "   getContent(msg));
//                    xFile.writeFile(msg   '#'   name, File_Withdraw);
//                    addedList.add(msg);
//                    size  ;
//                } else addedString = getContent(msg);
//
//            if (size == 1)
//                XToast.makeText(getApplicationContext(), getContent(addedList.get(0))).show();
//            else if (size > 1)
//                XToast.makeText(getApplicationContext(), "撤回了多条消息\n请在软件里查看").show();
//            else XToast.makeText(getApplicationContext(), addedString).show();
//
//        } else
//            XToast.makeText(getApplicationContext(), "sorry 并没有截到消息\n可在帮助中查看原因").show();
//
//        xFile.refresh();   //刷新撤回消息列表
//
//        search.closeFile();
//
//        Date end = new Date();
//        Log.w(TAG, "searching cost "   (end.getTime() - start.getTime())   " mm");
//
//    }
//
//    void scan(int i) {
//
//        target = aroundSting.get(i);
//        //把换行变成空格
//        target = xFile.format(target);
//        Log.i(TAG, "i: "   i);
//        Log.w(TAG, "target : "   target);
//        //连续撤回的次数 QQ是null 微信是文字_某某撤回了一条消息
//        if (target == null || (target.contains(TEXT_WITHDRAW))) {
//            num  ;
//        } else {
//            Log.i(TAG, "num:"   num);
//            while (true) {
//                line = search.nextLine();                       //往下找
//                if (line == null)                               //找完了 没找到
//                    return;
//                content = getContent(line);                     //提取一行中的内容
//
//                if (content == null)
//                    continue;
//
//                if (target.equals(content)) {                   //匹配到了list里的内容
//                    Log.w(TAG, "search: FOUND "   target);
//                    if (flag)                                   //如果找的是后一句
//                        line = search.nextLine();               //就找前一句
//                    else                                        //如果找的是前一句
//                        line = search.preLine();                //就找下一句
//
//                    Log.i(TAG, "read : "   line);
//                    if (line == null) {                         //这种情况发生在target被刚写入的
//                        search.nextLine();
//                        continue;                               //这一行可能是之后滚屏加进来的
//                    }
//
//                    if (aroundSting.contains(getContent(line))) {
//                        Log.w(TAG, "Screen List Contains this content , Continue");
//                        continue;
//                    }
//
//                    content = getContent(line);
//                    Log.e(TAG, "撤回的消息是: "   content);
//
//                    addToListMsg();
//
//                    //连续撤回
//                    if (num > 0) {
//                        Log.i(TAG, "number > 0");
//                        screenList = getScreen();
//
//                        //加个偏置
//                        if (flag)
//                            line = search.preLine();
//                        else
//                            search.nextLine();
//                        while (true) {
//                            if (flag)
//                                line = search.nextLine();
//                            else
//                                line = search.preLine();
//
//                            if (line == null) {
//                                search.nextLine();
//                                break;
//                            }
//
//                            content = getContent(line);
//                            if (screenList.contains(content))
//                                continue;
//
//                            addToListMsg();
//
//                            if (num == 0)
//                                return;
//                            num--;
//                        }
//                    } else break;
//                }
//            }
//        }
//    }
//
//    void addToListMsg() {
//
//        //如果这条是刚刚加过的(比如之前正向的scan)
//        if (listMsg.contains(line))
//            //但如果是两张图片的几率还是挺大的需要保留
//            if (!content.equals("[图片]"))
//                return;
//
//        //如果是图片的话把从QQ缓存里找来的图片保存到自己的文件夹下
//        if (content.equals("[图片]")) {
//            long time = getTime_Long(line);
//            boolean b = getImageFileInQQ(time);
//            if (b)
//                line = "#image"   time   getTime_String(line);
//                else line = "该图片曾经发过\n所以无法找到"   getTime_String(line);
//        }
//
//
//        //有时候getContent报错了就没有加时间会导致解析错误
//        if (line.length() < 13)
//            line  = new Date().getTime();
//
//        listMsg.add(line);
//
//        Log.w(TAG, "add: "   line);
//
//    }
//
//    /**
//     * 由于微信控件的ID会经常变
//     * 所以不能直接用nodeInfo.findAccessibilityNodeInfosByViewId(resourceID);
//     * 所以我的解决方法是通过解析布局,通过根布局慢慢getChild
//     * id好改,布局就不好改了
//     * 找出来的内容只有对方发送的(可以加上自己发送的但没必要)
//     *
//     * @return ScreenList
//     */
//    List<String> getScreen() {
//
//        List<String> screenList = new ArrayList<>();
//
//        try {
//            if (is_wx) {
//                try {
//                    n1 = nodeInfo.getChild(0).getChild(0).getChild(4);
//                    getScreenList_wx(screenList, n1);
//                } catch (Exception ignored) {
//                }
//                if (screenList.size() == 0) {
//                    n1 = nodeInfo.getChild(8).getChild(0).getChild(4);
//                    getScreenList_wx(screenList, n1);
//                }
//            } else {
//                try {
//                    n1 = nodeInfo.getChild(5);
//                    getScreenList_qq(screenList, n1);
//                } catch (Exception ignored) {
//                }
//                if (screenList.size() == 0) {
//                    n1 = nodeInfo.getChild(4);
//                    getScreenList_qq(screenList, n1);
//                }
////                //通过ID查找消息,但测试出来有时候会找不全
////                String resourceID = "com.tencent.mobileqq:id/chat_item_content_layout";
////                List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(resourceID);
////                for (AccessibilityNodeInfo text : list) {
////                    content = text.getText().toString();
////                    screenList.add(content);
////                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // TODO: test
//        if (screenList.size() == 0)
//            new GetNodes();
//
//        Log.w(TAG, "Screen List is: "   screenList);
//
//        return screenList;
//
//    }
//
//    /**
//     * 先找到"某某撤回一条消息"然后把这之 前 的内容抓下来存入List
//     *
//     * @return Item before withdraw
//     */
//    List<String> getPreString() {
//
//        List<String> preSting = new ArrayList<>();
//
//        try {
//            if (is_wx) {
//                try {
//                    n1 = nodeInfo.getChild(0).getChild(0).getChild(4);
//                    getPreString_wx(preSting, n1);
//                } catch (Exception ignored) {
//                }
//                if (preSting.size() == 0) {
//                    n1 = nodeInfo.getChild(8).getChild(0).getChild(4);
//                    getPreString_wx(preSting, n1);
//                }
//
//            } else {
//                // QQ
//                try {
//                    n1 = nodeInfo.getChild(5);
//                    getPreString_qq(preSting, n1);
//                } catch (Exception ignored) {
//                }
//
//                if (preSting.size() == 0) {
//                    n1 = nodeInfo.getChild(4);
//                    getPreString_qq(preSting, n1);
//                }
//            }
//        } catch (Exception ignored) {
//        }
//
//        if (preSting.size() == 0)
//            new GetNodes();
//
//        Log.w(TAG, "pre String List is : "   preSting);
//
//        return preSting;
//
//    }
//
//    /**
//     * 先找到"某某撤回一条消息"然后把这之 后 的内容抓下来存入List
//     * 用于 撤回的前一句找不到的情况
//     *
//     * @return Item after withdraw
//     */
//    List<String> getAftString() {
//
//        List<String> aftSting = new ArrayList<>();
//
//        try {
//            if (is_wx) {
//                try {
//                    n1 = nodeInfo.getChild(0).getChild(0).getChild(4);
//                    getAftString_wx(aftSting, n1);
//                } catch (Exception ignored) {
//                }
//                if (aftSting.size() == 0) {
//                    n1 = nodeInfo.getChild(8).getChild(0).getChild(4);
//                    getAftString_wx(aftSting, n1);
//                }
//
//            } else {
//                // QQ
//                try {
//                    n1 = nodeInfo.getChild(5);
//                    getAftString_qq(aftSting, n1);
//                } catch (Exception ignored) {
//                }
//
//                if (aftSting.size() == 0) {
//                    n1 = nodeInfo.getChild(4);
//                    getAftString_qq(aftSting, n1);
//                }
//            }
//        } catch (Exception ignored) {
//        }
//
//        if (aftSting.size() == 0)
//            new GetNodes();
//
//        Log.w(TAG, "after String List is : "   aftSting);
//
//        return aftSting;
//
//    }
//
//    void getScreenList_wx(List<String> screenList, AccessibilityNodeInfo n1) {
//
//        for (int i = 0; i < n1.getChildCount(); i  ) {
//            n2 = n1.getChild(i);
//            childCount = n2.getChildCount();
//            if (childCount != 0) {
//                n3 = n2.getChild(childCount - 1);
//                if (n3.getText() != null) {
//                    content = n3.getText().toString();
//                    screenList.add(content);
//                } else if (n3.getChildCount() == 3) {
//                    CharSequence charSequence = n3.getChild(2).getText();
//                    if (charSequence == null)
//                        continue;
//                    if (charSequence.toString().contains("红包")) {
//                        // TODO: 抢红包
//                        Log.e(TAG, "收到红包 !");
//                    }
//                }
//            }
//        }
//    }
//
//    void getScreenList_qq(List<String> screenList, AccessibilityNodeInfo n1) {
//
//        for (int i = 0; i < n1.getChildCount(); i  ) {
//            n2 = n1.getChild(i);
//            childCount = n2.getChildCount();
//            if (childCount != 0) {
//                n3 = n2.getChild(childCount - 1);
//
//                if (n3.getText() != null) {
//                    content = n3.getText().toString();
//                    screenList.add(content);
//                } else if (n3.getClassName().equals("android.widget.RelativeLayout")) {
//                    //判断是不是红包
//                    if (n3.getChildCount() == 3) {
//                        CharSequence charSequence = n3.getChild(2).getText();
//                        if (charSequence == null)
//                            continue;
//                        if (charSequence.toString().contains("红包")) {
//                            // TODO: 抢红包
//                            Log.e(TAG, "收到红包 !");
////                                getHongBao(n3);
//                        }
//                    } else {
//                        content = "[图片]";
//                        screenList.add(content);
//                    }
//                }
//            }
//        }
//    }
//
//    void getPreString_wx(List<String> preSting, AccessibilityNodeInfo n1) {
//
//        String tempSting = null;
//
//        for (int i = 0; i < n1.getChildCount(); i  ) {
//            n2 = n1.getChild(i);
//            childCount = n2.getChildCount();
//            if (childCount != 0) {
//                n3 = n2.getChild(childCount - 1);
//                if (n3.getText() != null) {
//                    contentScreen = n3.getText().toString();
//                    if (contentScreen.contains(TEXT_WITHDRAW))
//                        preSting.add(tempSting);
//                    tempSting = contentScreen;
//                }
//            }
//        }
//    }
//
//    void getPreString_qq(List<String> preSting, AccessibilityNodeInfo n1) {
//
//        String tempSting = null;
//
//        for (int i = 0; i < n1.getChildCount(); i  ) {
//            n2 = n1.getChild(i);
//            childCount = n2.getChildCount();
//            if (childCount != 0) {
//                n3 = n2.getChild(childCount - 1);
//                if (n3.getText() != null) {
//                    content = n3.getText().toString();
//                    if (content.contains(TEXT_WITHDRAW)) {
//                        preSting.add(tempSting);
//                        tempSting = null;
//                    } else
//                        tempSting = content;
//                } else if (n3.getClassName().equals("android.widget.RelativeLayout"))
//                    tempSting = "[图片]";
//            }
//        }
//    }
//
//    void getAftString_wx(List<String> aftSting, AccessibilityNodeInfo n1) {
//
//        boolean flag = false;
//
//        for (int i = 0; i < n1.getChildCount(); i  ) {
//            n2 = n1.getChild(i);
//            childCount = n2.getChildCount();
//            if (childCount != 0) {
//                n3 = n2.getChild(childCount - 1);
//                if (n3.getText() != null) {
//                    content = n3.getText().toString();
//                    if (flag)
//                        aftSting.add(content);
//                    if (content.contains(TEXT_WITHDRAW))
//                        flag = true;
//                }
//            }
//        }
//    }
//
//    void getAftString_qq(List<String> aftString, AccessibilityNodeInfo n1) {
//
//        boolean flag = false;
//
//        for (int i = 0; i < n1.getChildCount(); i  ) {
//            n2 = n1.getChild(i);
//            childCount = n2.getChildCount();
//            if (childCount != 0) {
//                n3 = n2.getChild(childCount - 1);
//                if (flag) {
//                    if (n3.getText() != null) {
//                        content = n3.getText().toString();
//                        aftString.add(content);
//                        flag = false;
//                    } else if (n3.getClassName().equals("android.widget.RelativeLayout")) {
//                        aftString.add("[图片]");
//                        flag = false;
//                    }
//                }
//                if (n3.getText() != null) {
//                    content = n3.getText().toString();
//                    if (content.contains(TEXT_WITHDRAW))
//                        flag = true;
//                }
//            }
//        }
//    }
//
//}
//
//    void getHongBao(AccessibilityNodeInfo nodeInfo) {
//
//        new GetNodes(nodeInfo);
//
//        if (!is_wx) {
//            CharSequence text = nodeInfo.getChild(1).getText();
//            if (text == null || text.toString().equals("已拆开")) {
//                Log.e(TAG, "已拆开 !");
//                return;
//            } else Log.w(TAG, "text : "   text);
//        }
//
//        if (!nodeInfo.isClickable()) {
//            Log.e(TAG, "unClickable ! ");
//            return;
//        }
//        if (!nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
//            Log.e(TAG, "click failed !");
//            return;
//        }
//
//        if (is_wx) {
//            if (nodeInfo.getChildCount() != 5) {
//                Log.e(TAG, "wx : childCount != 5 !");
//                return;
//            }
//
//            AccessibilityNodeInfo btn = nodeInfo.getChild(3);
//            if (!btn.isClickable()) {
//                Log.e(TAG, "wx : child unClickable !");
//                return;
//            }
//            btn.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//        }
//    }
//
//    /**
//     * 处理点击事件 单击多击
//     * 我这边两次点击时间差为300毫秒
//     */
//    private void onClick(AccessibilityEvent event, String name) {
//        ClickTime3 = ClickTime2;
//        ClickTime2 = ClickTime;
//        ClickTime = event.getEventTime();
//        if ((ClickTime - ClickTime3) < 600) {
//            //三击 先取消双击单击的post
//            if (doubleClick != null)
//                mHandler.removeCallbacks(doubleClick);
//            if (singleClick != null)
//                mHandler.removeCallbacks(singleClick);
//            trebleClick = new TrebleClick(name);
//            mHandler.post(trebleClick);
//            //防止连按四下多次执行三击操作
//            ClickTime3 = 0;
//        } else if ((ClickTime - ClickTime2) < 300) {
//            //双击 先取消单击的post
//            if (singleClick != null)
//                mHandler.removeCallbacks(singleClick);
//            doubleClick = new DoubleClick(name);
//            mHandler.postDelayed(doubleClick, 300);
//        } else {
//            //单击
//            singleClick = new SingleClick(name);
//            mHandler.postDelayed(singleClick, 300);
//        }
//    }
//
///**
// * 单击
// * 判断撤回消息列表里是否存在当前的聊天对象 如果有,就直接输出
// * 如果没有,就查找
// */
//class SingleClick implements Runnable {
//
//    String name;
//
//    SingleClick(String name) {
//        this.name = name;
//    }
//
//    @Override
//    public void run() {
//        Log.w(TAG, "Single Click");
//        if (WD_NameList.contains(name)) {
//            String text = WD_MsgList.get(WD_NameList.indexOf(name));
//            Log.w(TAG, "text : "   text);
//            XToast.makeText(getApplicationContext(), text).show();
//        } else {
//            new Search(name);
//        }
//    }
//
//}
//
///**
// * 双击
// * 直接查找
// */
//class DoubleClick implements Runnable {
//
//    String name;
//
//    DoubleClick(String name) {
//        this.name = name;
//    }
//
//    @Override
//    public void run() {
//        Log.e(TAG, "Double Click");
//        new Search(name);
//
//    }
//
//}
//
///**
// * 三击
// * 删除当前联系人加入的最后一行消息
// * 在滚屏和切换窗口时会多加消息
// * 主要是调试用
// */
//class TrebleClick implements Runnable {
//
//    String name;
//
//    TrebleClick(String name) {
//        this.name = name;
//    }
//
//    @Override
//    public void run() {
//
//        Log.e(TAG, "TREBLE CLICKED");
//
//        new XFile.RemoveLine(name, getApplicationContext()).remove();
//
//    }
//
//}
//
///**
// * 往本地写内容
// */
//class AddNewMessage implements Runnable {
//    @Override
//    public void run() {
//        try {
//            List<String> list = new Search().getScreen();
//            if (list.size() < 1) {
//                Log.d(TAG, "Screen List is Empty, return");
//                return;
//            }
//            String item = list.get(list.size() - 1);
//
//            Log.w(TAG, "MESSAGE IS "   item);
//            //判断是不是刚刚加过的 这边偷懒了没有去文件里查找确认
//            //微信会在滚屏时加入大量历史消息
//            if (item.equals(tempMessage)) {
//                //4.0.3:
//                // 图片相同的可能性很大
////                    if (!item.equals("[图片]")) {
//
//                Log.d(TAG, "Equal to Last Msg, return");
//                return;
////                    }
//            }
//
//            if (item.contains(TEXT_WITHDRAW)) {
//                Log.i(TAG, "Contains 撤回了一条消息 , return");
//                return;
//            }
//
//            //给消息加上时间戳
//            long date = new Date().getTime();
//
//            //4.0.3 保存的时间为格式化好的, 由于该格式只精确到分,查找图片的精度不够
//            //所以改为在显示的时候再加sdf.format
//            //String line = item   sdf.format(date);
//
//            String line = item   date;
//            tempMessage = item;
//            String name = getName();
//            if (!QQ_NameList.contains(name)) {
//                QQ_NameList.add(name);
//                Log.w(TAG, "add new name");
//            }
//            xFile.writeFile(line, name);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//}
//
//    /**
//     * 判断是否是在其他人的聊天界面收到了消息
//     * 为了在 QQ-不是当前联系人-发来消息 时检查是否出现过这个人
//     * QQ比较重,会在当前屏幕生成一个内部的弹窗
//     * 这种消息我截下来和普通消息一样,只是内容是这样的形式:
//     * "Name"   ' : '   "Message"
//     * 我根据这里是否存在冒号
//     * 然后判断Name是否在NameList中来区分 QQ-普通消息和别人发的消息
//     * 但微信不一样,只要是不在当前聊天窗口发来的消息都会给Notification
//     */
//    private boolean isOtherConversation(CharSequence cs) {
//        String string = cs.toString();
//        int len = cs.length();
//        int index1 = string.indexOf(":");
//        if (index1 > 0) {
//            if (len - index1 == 3)          //是时间
//                return true;
//            String name = string.substring(0, index1);
//            Log.i(TAG, "name: "   name);
//            //如果在联系人列表里出现过的,那么就是在其他人的聊天界面
//            if (QQ_NameList.contains(name)) {
//                String content = string.substring(index1   1);
//                long date = new Date().getTime();
//                String line = content   date;
//                xFile.writeFile(line, name);
//                return true;
//            } else {
//                //判断是不是群消息
//                int index2 = string.indexOf("-");
//                if (index2 > 0) {
//                    name = string.substring(0, index2);
//                    Log.i(TAG, "name: "   name);
//                    if (QQ_NameList.contains(name)) {
//                        String content = string.substring(index1   1);
//                        long date = new Date().getTime();
//                        String line = content   date;
//                        xFile.writeFile(line, name);
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 把已经存下来的名字拉到一个Set里
//     *
//     * @return Known Name List
//     */
//    Set<String> getNameList() {
//
//        Set<String> nameList = new HashSet<>();
//
//        File fileDir = getFilesDir();
//        for (File file : fileDir.listFiles()) {
//            if (file.isFile())
//                nameList.add(file.getName());
//        }
//
//        return nameList;
//
//    }
//
//    /**
//     * 把通知栏里截获的消息处理并写入本地
//     */
//    void getNotification(AccessibilityEvent event) {
//        Log.i(TAG, "Notification Changed");
//        List<CharSequence> texts = event.getText();
//        if (texts.isEmpty() || texts.size() == 0)
//            return;
//        for (CharSequence text : texts) {
//            if (text == null)
//                return;
//            String string = text.toString();
//            Log.w(TAG, "Notification Text:"   string);
//            if (string.equals("你的帐号在电脑登录"))
//                return;
//
//            String content;
//            String name;
//
//            int i = string.indexOf(':');
//            if (i < 1) {
//                Log.d(TAG, "Notification does not contains ':'");
//                return;
//            }
//            name = string.substring(0, i);
//            content = string.substring(i   2);
//            //是QQ群消息
//            if (!is_wx)
//                if (name.charAt(i - 1) == ')' && name.contains("(")) {
//                    content = string.substring(i   1);
//                    name = name.substring(name.indexOf('(')   1, name.indexOf(')'));
//                }
//            long date = new Date().getTime();
//            Log.w(TAG, "name : "   name   "    content : "   content   "    time : "   date);
//            String line = content   date;
//            tempMessage = content;
//            xFile.writeFile(line, name);
//        }
//
//    }
//
//    /**
//     * 根据UI解析出屏幕中Name
//     *
//     * @return Name
//     */
//    String getName() {
//
//        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
//        String s = "";
//        if (is_wx) {
//            try {
//                s = nodeInfo.getChild(0).getChild(0).getChild(1).getText().toString();
//            } catch (Exception e) {
//                try {
//                    s = nodeInfo.getChild(8).getChild(0).getChild(1).getText().toString();
//                } catch (Exception ignored) {
//                }
//            }
//        } else {
//            try {
//                List<AccessibilityNodeInfo> qq = nodeInfo.findAccessibilityNodeInfosByViewId(NameID_qq);
//                s = qq.get(0).getText().toString();
//            } catch (Exception ignored) {
//            }
//        }
//        if (s.length() != 0) {
//            Log.w(TAG, "name : "   s);
//            return s;
//        } else {
//            new GetNodes();
//            Log.e(TAG, "Get Name ERROR !");
//            return null;
//        }
//    }
//
//    /**
//     * 把自己存入本地的"line"
//     * 格式为 Content   Time
//     * 中的 Content分离出来
//     *
//     * @param line line in file
//     * @return content
//     */
//    String getContent(String line) {
//
//        try {
//            return line.substring(0, line.length() - 13);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Error";
//        }
//    }
//
//    /**
//     * 把Time分离出来
//     * 得到的是字符串 12:34 02/18
//     *
//     * @param line line in file
//     * @return time
//     */
//    String getTime_String(String line) {
//        return line.substring(line.length() - 13);
//    }
//
//    /**
//     * 把Time分出来
//     * 并sdf.parse
//     * 把String类型的time转换成Long的time
//     * 为了能够查找QQ撤回的图片
//     * 因为QQ图片文件名是根据第一次收到的时间命名的
//     * 之后的图片只会生成一个链
//     * 所以QQ只能查看第一次发的图片
//     *
//     * @param line line in file
//     * @return time
//     */
//    long getTime_Long(String line) {
//
//        //4.0.3
////        try {
////            Date date = sdf.parse(line.substring(line.length() - 11));
////            return date.getTime();
////        } catch (ParseException e) {
////            e.printStackTrace();
////        }
//
//        String substring = line.substring(line.length() - 13);
//
//        return Long.parseLong(substring);
//    }
//
///**
// * 调试工具
// * 用于输出屏幕的node信息
// */
//class GetNodes {
//
//    String print(AccessibilityNodeInfo nodeInfo) {
//
//        CharSequence text = nodeInfo.getText();
//        CharSequence description = nodeInfo.getContentDescription();
//        CharSequence packageName = nodeInfo.getPackageName();
//        CharSequence className = nodeInfo.getClassName();
//        boolean focusable = nodeInfo.isFocusable();
//        boolean clickable = nodeInfo.isClickable();
//        Rect rect = new Rect();
//        nodeInfo.getBoundsInScreen(rect);
//
//        return "| "
//        "text: "   text   " \t"
//        "description: "   description   " \t"
//        "location: "   rect   " \t"
//        "package name: "   packageName   " \t"
//        "class name: "   className   " \t"
//        "focusable: "   focusable   " \t"
//        "clickable: "   clickable   " \t"
//        '\n';
//
//    }
//
//    //无参就打印根布局
//    GetNodes() {
//        AccessibilityNodeInfo n0 = getRootInActiveWindow();
//        show(n0);
//    }
//
//    //传了参数就只打印这个节点下的所有自节点
//    GetNodes(AccessibilityNodeInfo n) {
//        show(n);
//    }
//
//    private void show(AccessibilityNodeInfo n) {
//        try {
//            Log.w(TAG, "\nv0                            "   print(n));
//            int v1 = n.getChildCount();
//            for (int i1 = 0; i1 < v1; i1  ) {
//                AccessibilityNodeInfo n1 = n.getChild(i1);
//                Log.w(TAG, "\n    v1: "   i1   "                     "   print(n1));
//                int v2 = n1.getChildCount();
//                for (int i2 = 0; i2 < v2; i2  ) {
//                    AccessibilityNodeInfo n2 = n1.getChild(i2);
//                    Log.w(TAG, "\n        v2: "   i2   "                 "   print(n2));
//                    int v3 = n2.getChildCount();
//                    for (int i3 = 0; i3 < v3; i3  ) {
//                        AccessibilityNodeInfo n3 = n2.getChild(i3);
//                        Log.w(TAG, "\n            v3: "   i3   "             "   print(n3));
//                        int v4 = n3.getChildCount();
//                        for (int i4 = 0; i4 < v4; i4  ) {
//                            AccessibilityNodeInfo n4 = n3.getChild(i4);
//                            Log.w(TAG, "\n                v4: "   i4   "         "   print(n4));
//                            int v5 = n4.getChildCount();
//                            for (int i5 = 0; i5 < v5; i5  ) {
//                                AccessibilityNodeInfo n5 = n4.getChild(i5);
//                                Log.w(TAG, "\n                    v5: "   i5   "     "   print(n5));
//                                int v6 = n5.getChildCount();
//                                for (int i6 = 0; i6 < v6; i6  ) {
//                                    AccessibilityNodeInfo n6 = n5.getChild(i6);
//                                    Log.w(TAG, "\n                        v6: "   i6   " "   print(n6));
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
//
//}
