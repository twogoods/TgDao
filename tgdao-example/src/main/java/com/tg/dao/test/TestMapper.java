package com.tg.dao.test;

import com.tg.dao.annotation.Params;
import com.tg.dao.test.model.User;


/**
 * Created by twogoods on 2017/11/3.
 */
@Params
public interface TestMapper {
    int insert(User user);
}
