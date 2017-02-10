package com.ddmeng.todorealm.data;

import android.support.annotation.NonNull;

import com.ddmeng.todorealm.data.models.TodoList;
import com.ddmeng.todorealm.utils.LogUtils;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class TodoRepository {

    private Realm realm;
    private volatile static TodoRepository instance;

    private TodoRepository() {
    }

    public static TodoRepository getInstance() {
        if (instance == null) {
            synchronized (TodoRepository.class) {
                if (instance == null) {
                    instance = new TodoRepository();
                }
            }
        }
        return instance;
    }

    public RealmResults<TodoList> getAllLists() {
        return realm.where(TodoList.class).findAll();
    }

    public void addNewList(final String title, final Realm.Transaction.OnSuccess onSuccess,
                           final Realm.Transaction.OnError onError) {
        Number maxIdNumber = realm.where(TodoList.class).max("id");
        final long nextId = maxIdNumber != null ? maxIdNumber.longValue() + 1 : 1;
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                TodoList list = realm.createObject(TodoList.class, nextId);
                list.setTitle(title);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                LogUtils.d("insert success");
                if (onSuccess != null) {
                    onSuccess.onSuccess();
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                LogUtils.e("insert failed: " + error);
                if (onError != null) {
                    onError.onError(error);
                }
            }
        });
    }

    public void deleteLists(@NonNull final List<Long> listIds) {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                LogUtils.d("delete: " + listIds);
                Long[] ids = listIds.toArray(new Long[listIds.size()]);
                RealmResults<TodoList> toDeleteLists = realm.where(TodoList.class).in("id", ids).findAll();
                LogUtils.d("queryed results: " + toDeleteLists.size());
                toDeleteLists.deleteAllFromRealm();
            }
        });
    }

    public void getRealm() {
        realm = Realm.getDefaultInstance();
    }

    public void close() {
        realm.close();
    }
}
