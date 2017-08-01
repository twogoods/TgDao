package com.tg.exception;

/**
 * Created by twogoods on 2017/8/1.
 */
public class TgDaoException extends RuntimeException {
    public TgDaoException(String message) {
        super(message);
    }

    public TgDaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public TgDaoException(Throwable cause) {
        super(cause);
    }
}
