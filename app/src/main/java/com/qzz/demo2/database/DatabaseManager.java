package com.qzz.demo2.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 数据库连接管理器 - 统一管理数据库连接的生命周期
 * 设计原理：
 * 1. 利用SQLiteDatabase内置的线程安全特性
 * 2. 维护单例连接，避免频繁开关数据库
 * 3. 提供明确的生命周期管理接口
 */
public class DatabaseManager {
    private static final String TAG = "DatabaseManager";
    private static DatabaseManager instance;
    private GameDatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private boolean isInitialized = false;
    private final Object lock = new Object();

    private DatabaseManager() {
        // 私有构造函数，确保单例
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * 初始化数据库管理器
     * 应在Application onCreate或主Activity onCreate中调用
     *
     * @param context 上下文
     * @throws RuntimeException 初始化失败时抛出
     */
    public void initialize(Context context) {
        synchronized (lock) {
            if (isInitialized) {
                Log.w(TAG, "DatabaseManager is already initialized");
                return;
            }

            try {
                Log.d(TAG, "Initializing DatabaseManager...");
                dbHelper = GameDatabaseHelper.getInstance(context.getApplicationContext());

                // 预先获取数据库实例，触发数据库创建和初始化
                database = dbHelper.getWritableDatabase();

                // 验证数据库状态
                if (database == null || !database.isOpen()) {
                    throw new RuntimeException("Failed to open database");
                }

                // 执行数据库完整性检查
                performIntegrityCheck();

                isInitialized = true;
                Log.d(TAG, "DatabaseManager initialized successfully");

            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize DatabaseManager", e);
                cleanup();
                throw new RuntimeException("DatabaseManager initialization failed", e);
            }
        }
    }

    /**
     * 获取数据库实例
     * 注意：SQLiteDatabase是线程安全的，可以在多线程环境中安全使用
     *
     * @return 数据库实例
     * @throws IllegalStateException 如果管理器未初始化
     */
    public SQLiteDatabase getDatabase() {
        synchronized (lock) {
            if (!isInitialized) {
                throw new IllegalStateException("DatabaseManager is not initialized. Call initialize() first.");
            }

            if (database == null || !database.isOpen()) {
                Log.w(TAG, "Database connection lost, attempting to reopen...");
                try {
                    database = dbHelper.getWritableDatabase();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to reopen database", e);
                    throw new RuntimeException("Database connection failed", e);
                }
            }

            return database;
        }
    }

    /**
     * 检查管理器是否已初始化
     */
    public boolean isInitialized() {
        synchronized (lock) {
            return isInitialized && database != null && database.isOpen();
        }
    }

    /**
     * 执行数据库完整性检查
     */
    private void performIntegrityCheck() {
        try {
            // 检查表是否存在
            if (!dbHelper.isTableExists(GameDatabaseHelper.TABLE_GAMES)) {
                Log.d(TAG, "Creating missing tables...");
                dbHelper.createTableIfNotExists();
            }

            // 执行PRAGMA integrity_check
            android.database.Cursor cursor = database.rawQuery("PRAGMA integrity_check", null);
            if (cursor.moveToFirst()) {
                String result = cursor.getString(0);
                if (!"ok".equals(result)) {
                    Log.w(TAG, "Database integrity check failed: " + result);
                }
            }
            cursor.close();

            Log.d(TAG, "Database integrity check completed");

        } catch (Exception e) {
            Log.e(TAG, "Database integrity check failed", e);
            // 不抛出异常，允许继续使用数据库
        }
    }

    /**
     * 清理资源
     * 应在Application onTerminate或主Activity onDestroy中调用
     */
    public void cleanup() {
        synchronized (lock) {
            Log.d(TAG, "Cleaning up DatabaseManager...");

            try {
                if (database != null && database.isOpen()) {
                    database.close();
                    Log.d(TAG, "Database connection closed");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing database", e);
            } finally {
                database = null;
                isInitialized = false;
            }
        }
    }

    /**
     * 重新初始化数据库连接
     * 用于处理数据库连接丢失的情况
     */
    public void reinitialize(Context context) {
        synchronized (lock) {
            Log.d(TAG, "Reinitializing DatabaseManager...");
            cleanup();
            initialize(context);
        }
    }

    /**
     * 获取数据库信息（调试用）
     */
    public void logDatabaseInfo() {
        synchronized (lock) {
            if (isInitialized && database != null) {
                Log.d(TAG, "Database version: " + database.getVersion());
                Log.d(TAG, "Database path: " + database.getPath());
                Log.d(TAG, "Database is readonly: " + database.isReadOnly());
                Log.d(TAG, "Database is open: " + database.isOpen());
            } else {
                Log.d(TAG, "DatabaseManager is not initialized");
            }
        }
    }
}
