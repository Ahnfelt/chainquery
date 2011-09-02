package com.chainquery;

public interface Row {
    public String getUnique();
    public Class<? extends Row> type();
}

