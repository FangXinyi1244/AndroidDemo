package com.qzz.demo2.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

public class GameDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "GameDatabaseHelper";

    // 数据库基本信息
    private static final String DATABASE_NAME = "game_database.db";
    private static final int DATABASE_VERSION = 1;

    // 表名和字段名常量
    public static final String TABLE_GAMES = "games";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_GAME_NAME = "game_name";
    public static final String COLUMN_PACKAGE_NAME = "package_name";
    public static final String COLUMN_APP_ID = "app_id";
    public static final String COLUMN_ICON = "icon";
    public static final String COLUMN_INTRODUCTION = "introduction";
    public static final String COLUMN_BRIEF = "brief";
    public static final String COLUMN_VERSION_NAME = "version_name";
    public static final String COLUMN_APK_URL = "apk_url";
    public static final String COLUMN_TAGS = "tags";
    public static final String COLUMN_SCORE = "score";
    public static final String COLUMN_PLAY_NUM_FORMAT = "play_num_format";
    public static final String COLUMN_CREATE_TIME = "create_time";

    // 建表SQL语句
    private static final String CREATE_TABLE_GAMES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_GAMES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_GAME_NAME + " TEXT NOT NULL, " +
                    COLUMN_PACKAGE_NAME + " TEXT, " +
                    COLUMN_APP_ID + " TEXT, " +
                    COLUMN_ICON + " TEXT, " +
                    COLUMN_INTRODUCTION + " TEXT, " +
                    COLUMN_BRIEF + " TEXT, " +
                    COLUMN_VERSION_NAME + " TEXT, " +
                    COLUMN_APK_URL + " TEXT, " +
                    COLUMN_TAGS + " TEXT, " +
                    COLUMN_SCORE + " REAL, " +
                    COLUMN_PLAY_NUM_FORMAT + " TEXT, " +
                    COLUMN_CREATE_TIME + " TEXT" +
                    ");";

    // 创建索引的SQL语句
    private static final String CREATE_INDEX_GAME_NAME =
            "CREATE INDEX IF NOT EXISTS idx_game_name ON " + TABLE_GAMES + "(" + COLUMN_GAME_NAME + ");";
    private static final String CREATE_INDEX_PACKAGE_NAME =
            "CREATE INDEX IF NOT EXISTS idx_package_name ON " + TABLE_GAMES + "(" + COLUMN_PACKAGE_NAME + ");";

    private static GameDatabaseHelper instance;
    private Context context;

    // 单例模式实现
    public static synchronized GameDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new GameDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private GameDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        Log.d(TAG, "DatabaseHelper initialized");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database and tables...");

        try {
            // 创建games表
            db.execSQL(CREATE_TABLE_GAMES);
            Log.d(TAG, "Table " + TABLE_GAMES + " created successfully");

            // 创建索引
            db.execSQL(CREATE_INDEX_GAME_NAME);
            db.execSQL(CREATE_INDEX_PACKAGE_NAME);
            Log.d(TAG, "Indexes created successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error creating database: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        try {
            if (oldVersion < newVersion) {
                // 这里可以根据具体的版本进行不同的升级策略
                // 示例：保留数据的升级方式
                upgradeDatabase(db, oldVersion, newVersion);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database: " + e.getMessage());
            // 如果升级失败，采用重建策略
            recreateDatabase(db);
        }
    }

    /**
     * 数据库升级处理（保留数据）
     */
    private void upgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 根据版本号进行不同的升级策略
        switch (oldVersion) {
            case 1:
                // 从版本1升级的逻辑
                // 例如：添加新字段、创建新表等
                break;
            default:
                // 默认重建
                recreateDatabase(db);
                break;
        }
    }

    /**
     * 重建数据库（删除所有表重新创建）
     */
    private void recreateDatabase(SQLiteDatabase db) {
        Log.w(TAG, "Recreating database...");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAMES);
        onCreate(db);
    }

    /**
     * 检查数据库文件是否存在
     * @return true如果数据库文件存在
     */
    public boolean isDatabaseExists() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        boolean exists = dbFile.exists();
        Log.d(TAG, "Database file exists: " + exists + ", path: " + dbFile.getAbsolutePath());
        return exists;
    }

    /**
     * 检查指定表是否存在
     * @param tableName 表名
     * @return true如果表存在
     */
    public boolean isTableExists(String tableName) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean exists = false;

        try {
            db = this.getReadableDatabase();
            String query = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
            cursor = db.rawQuery(query, new String[]{tableName});
            exists = cursor.getCount() > 0;
            Log.d(TAG, "Table " + tableName + " exists: " + exists);
        } catch (Exception e) {
            Log.e(TAG, "Error checking table existence: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return exists;
    }

    /**
     * 手动创建表（如果不存在）
     */
    public void createTableIfNotExists() {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            // 创建主表
            db.execSQL(CREATE_TABLE_GAMES);
            Log.d(TAG, "Ensured table " + TABLE_GAMES + " exists");

            // 创建索引
            db.execSQL(CREATE_INDEX_GAME_NAME);
            db.execSQL(CREATE_INDEX_PACKAGE_NAME);
            Log.d(TAG, "Ensured indexes exist");

        } catch (Exception e) {
            Log.e(TAG, "Error creating table: " + e.getMessage());
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * 获取数据库信息
     */
    public void printDatabaseInfo() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            Log.d(TAG, "=== Database Information ===");
            Log.d(TAG, "Database Name: " + DATABASE_NAME);
            Log.d(TAG, "Database Version: " + db.getVersion());
            Log.d(TAG, "Database Path: " + db.getPath());

            // 查询所有表
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            Log.d(TAG, "Tables in database:");
            while (cursor.moveToNext()) {
                String tableName = cursor.getString(0);
                Log.d(TAG, "  - " + tableName);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error getting database info: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }
}
