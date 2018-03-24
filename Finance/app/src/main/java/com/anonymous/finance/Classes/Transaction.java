package com.anonymous.finance.Classes;

/**
 * Created by ANONYMOUS on 23-Mar-18.
 */

public class Transaction {

    private String comment, type, amount, username, date, board, changedBalance, uuid;

    public Transaction(String comment, String type, String amount, String username, String date, String board, String changedBalance, String uuid) {
        this.comment = comment;
        this.type = type;
        this.amount = amount;
        this.username = username;
        this.date = date;
        this.board = board;
        this.changedBalance = changedBalance;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getComment() {
        return comment;
    }

    public String getType() {
        return type;
    }

    public String getAmount() {
        return amount;
    }
}
