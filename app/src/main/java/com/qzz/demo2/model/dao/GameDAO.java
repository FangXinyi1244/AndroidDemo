package com.qzz.demo2.model.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.qzz.demo2.database.DatabaseManager;
import com.qzz.demo2.database.GameDatabaseHelper;
import com.qzz.demo2.model.dto.Game;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏数据访问对象 - 优化版本
 * 设计原理：
 * 1. 通过DatabaseManager统一管理数据库连接
 * 2. 移除频繁的连接开关操作
 * 3. 简化异常处理逻辑
 * 4. 提供更好的事务支持
 */
public class GameDAO {

    private static final String TAG = "GameDAO";
    private DatabaseManager databaseManager;

    public GameDAO() {
        databaseManager = DatabaseManager.getInstance();
    }

    /**
     * 获取数据库实例的便捷方法
     */
    private SQLiteDatabase getDatabase() {
        return databaseManager.getDatabase();
    }

    /**
     * 插入单个游戏记录
     * @param game 游戏对象
     * @return 插入记录的ID，失败返回-1
     */
    public long insertGame(Game game) {
        if (game == null) {
            Log.e(TAG, "Cannot insert null game");
            return -1;
        }

        try {
            SQLiteDatabase db = getDatabase();
            ContentValues values = gameToContentValues(game);
            long insertId = db.insert(GameDatabaseHelper.TABLE_GAMES, null, values);

            if (insertId != -1) {
                Log.d(TAG, "Game inserted successfully with ID: " + insertId);
            } else {
                Log.e(TAG, "Failed to insert game: " + game.getGameName());
            }
            return insertId;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting game: " + game.getGameName(), e);
            return -1;
        }
    }

    /**
     * 批量插入游戏记录（优化版本，使用事务）
     * @param games 游戏列表
     * @return 成功插入的记录数
     */
    public int insertGames(List<Game> games) {
        if (games == null || games.isEmpty()) {
            Log.w(TAG, "No games to insert");
            return 0;
        }

        Log.d(TAG, "Starting batch insert for " + games.size() + " games");
        SQLiteDatabase db = getDatabase();
        int successCount = 0;

        db.beginTransaction();
        try {
            for (Game game : games) {
                if (game != null) {
                    ContentValues values = gameToContentValues(game);
                    long insertId = db.insert(GameDatabaseHelper.TABLE_GAMES, null, values);
                    if (insertId != -1) {
                        successCount++;
                    }
                }
            }
            db.setTransactionSuccessful();
            Log.d(TAG, "Batch insert completed: " + successCount + "/" + games.size() + " records");
        } catch (Exception e) {
            Log.e(TAG, "Error in batch insert", e);
        } finally {
            db.endTransaction();
        }

        return successCount;
    }

    /**
     * 根据ID查询游戏
     * @param id 游戏ID
     * @return 游戏对象，不存在返回null
     */
    public Game getGameById(long id) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getDatabase();
            String selection = GameDatabaseHelper.COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(id)};

            cursor = db.query(GameDatabaseHelper.TABLE_GAMES, null,
                    selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                return cursorToGame(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying game by ID: " + id, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 查询所有游戏
     * @return 游戏列表
     */
    public List<Game> getAllGames() {
        return getGames(null, null, GameDatabaseHelper.COLUMN_ID + " DESC");
    }

    /**
     * 根据游戏名称模糊查询
     * @param gameName 游戏名称关键字
     * @return 匹配的游戏列表
     */
    public List<Game> getGamesByName(String gameName) {
        if (gameName == null || gameName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String selection = GameDatabaseHelper.COLUMN_GAME_NAME + " LIKE ?";
        String[] selectionArgs = {"%" + gameName.trim() + "%"};
        return getGames(selection, selectionArgs, GameDatabaseHelper.COLUMN_GAME_NAME + " ASC");
    }

    /**
     * 根据评分范围查询游戏
     * @param minScore 最低评分
     * @param maxScore 最高评分
     * @return 符合评分条件的游戏列表
     */
    public List<Game> getGamesByScoreRange(double minScore, double maxScore) {
        String selection = GameDatabaseHelper.COLUMN_SCORE + " BETWEEN ? AND ?";
        String[] selectionArgs = {String.valueOf(minScore), String.valueOf(maxScore)};
        return getGames(selection, selectionArgs, GameDatabaseHelper.COLUMN_SCORE + " DESC");
    }

    /**
     * 通用查询方法（优化版本）
     * @param selection 查询条件
     * @param selectionArgs 查询参数
     * @param orderBy 排序方式
     * @return 游戏列表
     */
    private List<Game> getGames(String selection, String[] selectionArgs, String orderBy) {
        List<Game> games = new ArrayList<>();
        Cursor cursor = null;

        try {
            SQLiteDatabase db = getDatabase();
            cursor = db.query(GameDatabaseHelper.TABLE_GAMES, null,
                    selection, selectionArgs, null, null, orderBy);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Game game = cursorToGame(cursor);
                    if (game != null) {
                        games.add(game);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying games", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return games;
    }

    public List<Game> getRandomGames(int limit) {
        if (limit <= 0) {
            return new ArrayList<>();
        }

        List<Game> games = new ArrayList<>();
        Cursor cursor = null;

        try {
            SQLiteDatabase db = getDatabase();
            String query = "SELECT * FROM " + GameDatabaseHelper.TABLE_GAMES +
                    " ORDER BY RANDOM() LIMIT ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(limit)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Game game = cursorToGame(cursor);
                    if (game != null) {
                        games.add(game);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying random games", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return games;
    }

    /**
     * 更新游戏信息
     * @param game 更新的游戏对象
     * @return 影响的行数
     */
    public int updateGame(Game game) {
        if (game == null || game.getId() == null) {
            Log.e(TAG, "Cannot update game: game is null or ID is null");
            return 0;
        }

        try {
            SQLiteDatabase db = getDatabase();
            ContentValues values = gameToContentValues(game);
            String whereClause = GameDatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(game.getId())};

            int rowsAffected = db.update(GameDatabaseHelper.TABLE_GAMES, values, whereClause, whereArgs);
            Log.d(TAG, "Updated " + rowsAffected + " rows for game ID: " + game.getId());
            return rowsAffected;
        } catch (Exception e) {
            Log.e(TAG, "Error updating game: " + game.getId(), e);
            return 0;
        }
    }

    /**
     * 删除游戏
     * @param id 游戏ID
     * @return 删除的行数
     */
    public int deleteGame(long id) {
        try {
            SQLiteDatabase db = getDatabase();
            String whereClause = GameDatabaseHelper.COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(id)};

            int rowsAffected = db.delete(GameDatabaseHelper.TABLE_GAMES, whereClause, whereArgs);
            Log.d(TAG, "Deleted " + rowsAffected + " rows for game ID: " + id);
            return rowsAffected;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting game: " + id, e);
            return 0;
        }
    }

    /**
     * 清空所有游戏数据
     * @return 删除的行数
     */
    public int deleteAllGames() {
        try {
            SQLiteDatabase db = getDatabase();
            int rowsAffected = db.delete(GameDatabaseHelper.TABLE_GAMES, null, null);
            Log.d(TAG, "Deleted all games: " + rowsAffected + " rows");
            return rowsAffected;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting all games", e);
            return 0;
        }
    }

    /**
     * 获取游戏总数
     * @return 游戏总数
     */
    public long getGameCount() {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + GameDatabaseHelper.TABLE_GAMES, null);
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting game count", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    /**
     * 将Game对象转换为ContentValues
     */
    private ContentValues gameToContentValues(Game game) {
        ContentValues values = new ContentValues();

        // 注意：ID字段在插入时通常不设置，让数据库自动递增
        if (game.getId() != null) {
            values.put(GameDatabaseHelper.COLUMN_ID, game.getId());
        }

        values.put(GameDatabaseHelper.COLUMN_GAME_NAME, game.getGameName());
        values.put(GameDatabaseHelper.COLUMN_PACKAGE_NAME, game.getPackageName());
        values.put(GameDatabaseHelper.COLUMN_APP_ID, game.getAppId());
        values.put(GameDatabaseHelper.COLUMN_ICON, game.getIcon());
        values.put(GameDatabaseHelper.COLUMN_INTRODUCTION, game.getIntroduction());
        values.put(GameDatabaseHelper.COLUMN_BRIEF, game.getBrief());
        values.put(GameDatabaseHelper.COLUMN_VERSION_NAME, game.getVersionName());
        values.put(GameDatabaseHelper.COLUMN_APK_URL, game.getApkUrl());
        values.put(GameDatabaseHelper.COLUMN_TAGS, game.getTags());
        values.put(GameDatabaseHelper.COLUMN_SCORE, game.getScore());
        values.put(GameDatabaseHelper.COLUMN_PLAY_NUM_FORMAT, game.getPlayNumFormat());
        values.put(GameDatabaseHelper.COLUMN_CREATE_TIME, game.getCreateTime());

        return values;
    }

    /**
     * 将Cursor转换为Game对象（增强错误处理）
     */
    private Game cursorToGame(Cursor cursor) {
        try {
            Game game = new Game();

            game.setId(cursor.getLong(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_ID)));
            game.setGameName(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_GAME_NAME)));
            game.setPackageName(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_PACKAGE_NAME)));
            game.setAppId(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_APP_ID)));
            game.setIcon(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_ICON)));
            game.setIntroduction(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_INTRODUCTION)));
            game.setBrief(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_BRIEF)));
            game.setVersionName(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_VERSION_NAME)));
            game.setApkUrl(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_APK_URL)));
            game.setTags(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_TAGS)));

            // 处理可能为null的Double类型
            int scoreIndex = cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_SCORE);
            if (!cursor.isNull(scoreIndex)) {
                game.setScore(cursor.getDouble(scoreIndex));
            }

            game.setPlayNumFormat(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_PLAY_NUM_FORMAT)));
            game.setCreateTime(cursor.getString(cursor.getColumnIndexOrThrow(GameDatabaseHelper.COLUMN_CREATE_TIME)));

            return game;
        } catch (Exception e) {
            Log.e(TAG, "Error converting cursor to game", e);
            return null;
        }
    }
}
