package com.fitgreat.airfacerobot.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.fitgreat.archmvp.base.util.ExecutorManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库管理类<p>
 *
 * @author zixuefei
 * @since 2020/3/18 0018 15:00
 */

@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    private static final String DB_NAME = "UserDatabase.db";
    private static volatile UserDatabase instance;
    private static Context mContext;

    public static synchronized UserDatabase getInstance(Context context) {
        mContext = context.getApplicationContext();
        if (instance == null) {
            instance = create(mContext);
        }
        return instance;
    }

    private static UserDatabase create(Context context) {
        return Room.databaseBuilder(context, UserDatabase.class, DB_NAME).build();
    }

    public abstract UserDao getUserDao();

    //增
    public void insert(List<User> user) {
        ExecutorManager.getInstance().executeTask(() -> {
            UserDatabase.getInstance(mContext)
                    .getUserDao()
                    .insertAll(user);
        });
    }

    //删
    public void delete(User user) {
        ExecutorManager.getInstance().executeTask(() -> {
            UserDatabase.getInstance(mContext)
                    .getUserDao()
                    .delete(user);
        });
    }

    //查
    public List<User> queryAll() {
        ArrayList<User> users = new ArrayList<>();
        ExecutorManager.getInstance().executeTask(() -> {
            users.clear();
            users.addAll(UserDatabase.getInstance(mContext)
                    .getUserDao()
                    .getAllUsers());

        });
        return users;
    }
}

