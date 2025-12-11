package com.ai.system.general.exception.user;

import com.ai.system.general.exception.base.BaseException;

/**
 * 用户信息异常类
 *
 * @author ai
 */
public class UserException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public UserException(String code, Object[] args)
    {
        super("user", code, args, null);
    }
}
