package com.picfix.tools.config;


import android.os.Handler;


public class Constant {

    public static final String PRODUCT_ID = "1";
    public static boolean isDebug = false;
    public static Handler mHandler = null;
    public static Handler mSecondHandler = null;
    public static String ROM = "";
    public static final String ROM_MIUI = "MIUI";
    public static final String ROM_EMUI = "EMUI";
    public static final String ROM_FLYME = "FLYME";
    public static final String ROM_OPPO = "OPPO";
    public static final String ROM_VIVO = "VIVO";
    public static final String ROM_OTHER = "OTHER";
    public static String CLIENT_TOKEN = "";
    public static String QUEST_TOKEN = "questToken";
    public static String USER_NAME = "";
    public static String USER_ID = "";
    public static Boolean ScanStop = false;

    public static Boolean OCPC = true;
    public static Boolean AD_OPENNING = false;
    public static String CHANNEL_ID = "5ccd4758a1115ff5";
    public static String CHANNEL_HUAWEI = "539cf1fbda8b8191";
    public static String CHANNEL_OPPO = "0f62d749fcd4d65f";
    public static String CHANNEL_XIAOMI = "6514e45c8c42f469";
    public static String CHANNEL_VIVO = "46abc5e760a15230";
    public static String CHANNEL_FLYME = "9e65372a35cdc6fa";
    public static String WEBSITE = "";
    public static int TEST = 0;

    //service_code
    public static String REC = "rec";
    public static String COM = "com";
    public static String REPL = "repl";
    public static String BILL = "billrec";
    public static String DELETE = "delete";
    public static String PHOTO_FIX = "photofix";
    public static String PHOTO_FIX_TIMES = "times";
    public static String PHOTO_HAND_FIX = "atf";

    //service_expire
    public static String EXPIRE_TYPE_FOREVER = "2";
    public static String EXPIRE_TYPE_YEAR = "1";
    public static String EXPIRE_TYPE_MONTH = "3";

    public static String EXPORT_PATH = "/export/";
    public static String WX_HIGN_VERSION_PATH = "/Android/data/com.tencent.mm/";
    public static String MM_RESOURCE_PATH = "/Android/data/com.immomo.momo/";
    public static String SOUL_RESOURCE_PATH = "/Android/data/cn.soulapp.android/";
    public static String WX_PICTURE_PATH = "/Pictures/WeiXin/";
    public static String PICTURE_PATH = "/Pictures/";
    public static String DOWNLOAD_PATH = "/Download/";
    public static String DCIM_PATH = "/DCIM/";
    public static String WX_DB_PATH = "/App/com.tencent.mm/MicroMsg/";
    public static String WX_ZIP_PATH = "APP/com.tencent.mm.zip";
    public static String WX_RESOURCE_PATH = "/tencent/";
    public static String WX_DOWNLOAD_PATH = "/Download/Weixin/";
    public static String QQ_RESOURCE_PATH = "/tencent/MobileQQ/";
    public static String QQ_HIGN_VERSION_PATH = "/Android/data/com.tencent.mobileqq/";
    public static String FLYME_BACKUP_PATH = "/backup/";
    public static String WX_PACK_NAME = "com.tencent.mm";

    public static String BACKUP_PATH = "/aA123456在此/";
    public static String XM_BACKUP_PATH = "/MIUI/backup/AllBackup/";
    public static String OPPO_BACKUP_PATH = "/backup/App/";
    public static String HW_BACKUP_NAME_TAR = "com.tencent.mm.tar";
    public static String HW_BACKUP_APP_DATA_TAR = "com.tencent.mm_appDataTar";
    public static String XM_BACKUP_NAME_BAK = "微信(com.tencent.mm).bak";
    public static String OPPO_BACKUP_NAME_TAR = "com.tencent.mm.tar";
    public static String VIVO_BACKUP_NAME_TAR = "5a656b0891e6321126f9b7da9137994c72220ce7";
    public static String HW_BACKUP_NAME_XML = "info.xml";
    public static String JX_BACKUP_PATH = "/backup/";

    public static String DB_NAME = "EnMicroMsg.db";

    public static int PERMISSION_CAMERA_REQUEST_CODE = 0x00000011;
    public static int CAMERA_REQUEST_CODE = 0x00000012;

    //Realm
    public static String ROOM_DB_NAME = "EnMicroMsg";

    //Bugly
    public static String BUGLY_APPID = "79f60ce42b";

    //oss
    public static String END_POINT = "http://oss-cn-shenzhen.aliyuncs.com";
    public static String END_POINT_WITHOUT_HTTP = "oss-cn-shenzhen.aliyuncs.com";
    public static String BUCKET_NAME = "qlrecovery";

    //tencent Pay
    public static String TENCENT_APP_ID = "wx402f05656134648b";
    public static String TENCENT_MINI_PROGRAM_APP_ID = "gh_72629534a52d";
    public static String TENCENT_PARTNER_ID = "1605572449";
}
