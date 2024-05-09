package com.adl.genius.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    public final static int CODE_SUCCESS = 200;
    public final static int CODE_ERROR = 400;

    private int code;
    private String msg;
    private T data;

    public static <R> Response<R> success(R data) {
        return new Response<>(CODE_SUCCESS, "", data);
    }

    public static <R> Response<R> error(String msg) {
        return new Response<>(CODE_ERROR, msg, null);
    }
}
