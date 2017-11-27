package com.tg.dao.test;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tg.dao.test.model.User;
import com.tg.dao.test.model.UserSearch;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by twogoods on 2017/8/5.
 */
public class UserDaoTest {

    private SqlSession session;
    private UserMapper mapper;

    @Before
    public void setup() throws Exception {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        session = sqlSessionFactory.openSession(true);
        mapper = session.getMapper(UserMapper.class);
        System.out.println(session);
        System.out.println(mapper);
    }

    @After
    public void release() {
        session.close();
    }

    @Test
    public void testQueryUser() throws Exception {
        List<User> users = mapper.queryUser("twogoods", 20, 5, 0);
        Assert.assertTrue(users.size() >= 0);
    }

    @Test
    public void testQueryUserList() {
        List<User> users = mapper.queryUserList(new int[]{1, 2, 3});
        Assert.assertTrue(users.size() >= 0);
    }

    @Test
    public void testQueryUser1() throws Exception {
        List<User> users = mapper.queryUser1(22);
        Assert.assertTrue(users.size() >= 0);
    }

    @Test
    public void testQueryUser2param() throws Exception {
        List<User> users = mapper.queryUser2param(22, "test");
        Assert.assertTrue(users.size() >= 0);
    }

    @Test
    public void testQueryUser2() throws Exception {
        PageHelper.offsetPage(1, 10);
        List<User> users = mapper.queryUser2(12, 30);
        PageInfo page = new PageInfo(users);
        System.out.println(page.getTotal());
        Assert.assertTrue(page.getList().size() >= 0);
    }

    @Test
    public void testQueryUser3() throws Exception {
        List<User> users = mapper.queryUser3(null, new String[]{"1", "2", "3"});
        Assert.assertTrue(users.size() >= 0);
    }

    @Test
    public void testQueryUser4() throws Exception {
        List<User> users = mapper.queryUser4(Arrays.asList(1, 2));
        Assert.assertTrue(users.size() >= 0);
    }

    @Test
    public void testQueryUser5() throws Exception {
        UserSearch search = new UserSearch();
        search.setUsername("twogoods");
        search.setMinAge(10);
        search.setMaxAge(30);
        search.setOffset(0);
        search.setLimit(10);
        search.setIds(Arrays.asList(1, 2, 3));
        search.setIdArr(new int[]{1, 2, 3});
        List<User> users = mapper.queryUser5(search);
        Assert.assertTrue(users.size() >= 0);
    }

    @Test
    public void testCount() throws Exception {
        int count = mapper.count("twogoods", 24);
        Assert.assertTrue(count >= 0);
    }

    @Test
    public void testCount2() throws Exception {
        UserSearch search = new UserSearch();
        search.setMinAge(24);
        int count = mapper.count2(search);
        Assert.assertTrue(count >= 0);
    }

    @Test
    public void testInsert() throws Exception {
        User u = new User();
        u.setUsername("ha");
        int res = mapper.insert(u);
        Assert.assertTrue(res > 0);
    }

    @Test
    public void testBatchInsert() throws Exception {
        List<User> users = new ArrayList<>();
        User u = new User();
        u.setUsername("ha");
        User u1 = new User();
        u1.setUsername("hasa");
        users.add(u);
        users.add(u1);
        int res = mapper.batchInsert(users);
        Assert.assertTrue(res > 0);
    }

    @Test
    public void testUpdate() throws Exception {
        User user = new User();
        user.setId(8L);
        user.setPassword("123");
        user.setAge(23);
        user.setOldAddress("上海");
        user.setNowAddress("北京");
        int res = mapper.update(user);
        Assert.assertTrue(res >= 0);
    }

    @Test
    public void testDelete() throws Exception {
        int count = mapper.delete(30, 40);
        Assert.assertTrue(count == 0);
    }

    @Test
    public void testDelete2() throws Exception {
        UserSearch search = new UserSearch();
        search.setMinAge(40);
        search.setMaxAge(50);
        int count = mapper.delete2(search);
        Assert.assertTrue(count == 0);
    }
}