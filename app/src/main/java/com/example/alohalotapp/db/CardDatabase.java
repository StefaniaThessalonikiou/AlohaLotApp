package com.example.alohalotapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.alohalotapp.cards.Card;

import java.util.ArrayList;

public class CardDatabase {
    private CardDbHelper dbHelper;
    private SQLiteDatabase db;

    public CardDatabase(Context context) {
        dbHelper = new CardDbHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    public long insert(String holder, String number, String expiry) {
        ContentValues values = new ContentValues();
        values.put(CardContract.CardEntry.COLUMN_NAME_HOLDER, holder);
        values.put(CardContract.CardEntry.COLUMN_NAME_NUMBER, number);
        values.put(CardContract.CardEntry.COLUMN_NAME_EXPIRY, expiry);
        return db.insert(CardContract.CardEntry.TABLE_NAME, null, values);
    }

    // Retrieve all cards
    public ArrayList<Card> getAllCards() {
        ArrayList<Card> cardList = new ArrayList<>();
        Cursor cursor = db.query(CardContract.CardEntry.TABLE_NAME,
                null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String holder = cursor.getString(cursor.getColumnIndexOrThrow(CardContract.CardEntry.COLUMN_NAME_HOLDER));
            String number = cursor.getString(cursor.getColumnIndexOrThrow(CardContract.CardEntry.COLUMN_NAME_NUMBER));
            String expiry = cursor.getString(cursor.getColumnIndexOrThrow(CardContract.CardEntry.COLUMN_NAME_EXPIRY));
            cardList.add(new Card(holder, number, expiry));
        }
        cursor.close();
        return cardList;
    }

    // Delete card by holder and number
    public void deleteCard(String holder, String number) {
        db.delete(CardContract.CardEntry.TABLE_NAME,
                CardContract.CardEntry.COLUMN_NAME_HOLDER + "=? AND " +
                        CardContract.CardEntry.COLUMN_NAME_NUMBER + "=?",
                new String[]{holder, number});
    }

    public void close() {
        db.close();
    }
}
