## 介绍
TgDao是一款基于Mybatis的编译期SQL生成器，利用注解来表达SQL，能根据你的方法签名生成对应的Mapper.xml文件。
它能减少你日常开发中大量简单SQL的编写，由于它只是生成Mapper.xml文件，因此对于复杂的查询场景，
你同样可以自己编写来完成一些工具所无法生成的SQL。

```
@Table(name = "T_User")
public class User {
    @Id("id")
    private int id;
    private String username;
    private int age;
}

```
上面的model定义了模型和数据库表的关系，看到下面这些方法的签名，聪明的你肯定能猜出每个方法的sql吧，这就是这个库要做的工作。

```
@DaoGen(model = User.class)
public interface UserDao {
    @Select
    @OrderBy("id desc")
    List<User> queryUser(@Condition(criterion = Criterions.EQUAL, column = "username") String name,
                         @Condition(criterion = Criterions.GREATER, attach = Attach.OR) int age,
                         @Limit int limit, @OffSet int offset);

    @Select
    List<User> queryUser2(@Condition(criterion = Criterions.GREATER, column = "age") int min,
                          @Condition(criterion = Criterions.LESS, column = "age") int max);

    @Select
    List<User> queryUser3(@Condition(criterion = Criterions.EQUAL, column = "username") String name,
                          @Condition(attach = Attach.OR, column = "id", criterion = Criterions.IN) String[] ids);

    @Insert(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @BatchInsert(columns = "username,age")
    int batchInsert(List<User> users);

    @Update
    @ModelConditions({
            @ModelCondition(field = "id")
    })
    int update(User user);

    @Delete
    int delete(@Condition(criterion = Criterions.GREATER, column = "age") int min,
               @Condition(criterion = Criterions.LESS, column = "age") int max);
}
```

---

## 文档
引入如下依赖：

```
<dependency>
  <groupId>com.github.twogoods</groupId>
  <artifactId>tgdao-core</artifactId>
  <version>0.1.2</version>
</dependency>
```
### Table与Model关联
`@Table`记录数据表的名字
`@Id`记录主键信息
`@Column`映射了表字段和属性的关系，如果表字段和类属性同名，那么可以省略这个注解
`@Ingore`忽略这个类属性，没有哪个表字段与它关联

```
@Table(name = "T_User")
public class User {
    @Id("id")
    private int id;

    private String username;
    private String password;
    private int age;

    @Column("old_address")
    private String oldAddress;
    @Column("now_address")
    private String nowAddress;

    private int state;

    @Column("created_at")
    private Timestamp createdAt;
    @Column("updated_at")
    private Timestamp updatedAt;

    @Ignore
    private String remrk;
```
### 查询
```
@Select
@OrderBy("id desc")
List<User> queryUser(@Condition(criterion = Criterions.EQUAL, column = "username") String name,
                    @Condition(criterion = Criterions.GREATER, attach = Attach.OR) int age,
                    @Condition(column = "id", criterion = Criterions.IN) String[] ids,
                    @Limit int limit, @OffSet int offset);
```
##### @Select
* `columns`:默认 `select *`可以配置`columns("username,age")`选择部分字段；
* `SqlMode`:有两个选择，SqlMode.SELECTIVE 和 SqlMode.COMMON，区别是selective会检查查询条件的字段是否为null来实现动态的查询,
即`<if test="name != null">username = #{name}</if>`

##### @Condition
* `criterion`：查询条件，`=`,`<`,`>`,`in`等，具体见`Criterions`
* `column`：与表字段的对应，若与字段名相同可不配置
* `attach`：连接 `and`,`or`， 默认是`and`
* `test`：selective下的判断表达式，即`<if test="username != null">`里的test属性

`@Limit`，`@OffSet`为分页字段。
方法的参数不加任何注解一样会被当做查询条件，如下面两个函数效果是一样的：

```
@Select()
List<User> queryUser(Integer age);

@Select()
List<User> queryUser(@Condition(criterion = Criterions.EQUAL, column = "age") Integer age);
```

#### 查询Model
上面的例子在查询条件比较多时方法参数会比较多，我们可以把查询条件封装到一个类里，使用`@ModelConditions`来注解查询条件，注意被`@ModelConditions`只能有一个参数。

```
@Select
@Page
@ModelConditions({
       @ModelCondition(field = "username", criterion = Criterions.EQUAL),
       @ModelCondition(field = "minAge", column = "age", criterion = Criterions.GREATER),
       @ModelCondition(field = "maxAge", column = "age", criterion = Criterions.LESS),
       @ModelCondition(field = "ids", column = "id", criterion = Criterions.IN),
       @ModelCondition(field = "idArr", column = "id", criterion = Criterions.IN, paramType = InType.ARRAY)
})
List<User> queryUser5(UserSearch userSearch);
```
##### @ModelCondition
* `field`:必填，查询条件中类对应的属性
* `column`：对应的表字段
* `paramType`：in 查询下才需要配置，数组为`array`,List为`collection`类型
* `test`：selective下的判断表达式，即`<if test="username != null">`里的test属性

`@Page`只能用在ModelConditions下的查询，并且方法参数的那个类应该有`offset`，`limit`这两个属性。

**注：**

```
@Select(columns = "username,age")
List<User> queryUser(Integer age);

@Select(columns = "username,age")
List<User> queryUser2param(Integer age, String username);

<select id="queryUser" resultMap="XXX">select username,age from T_User
    <where>
      <if test="age != null">AND age = #{age}</if>
    </where>
</select>

<select id="queryUser2param" resultMap="XXX">select username,age from T_User
    <where>
      <if test="age != null">AND age = #{age}</if>
      <if test="username != null">AND username = #{username}</if>
    </where>
</select>
```
两个函数生成的sql如上，`@Select`的属性`SqlMode`默认是`Selective`，所以两个都有<if>条件判断，但是这里第一个函数的sql，
Mybatis不支持，执行会报错，类似`no age getter in java.lang.Interger`，Mybatis会把这唯一的一个参数当做对象来取里面的值。
解决方法：函数签名里强加`@Param()`注解，或者`@Select`里使用`sqlMode = SqlMode.COMMON`去掉生成sql里的if判断。
这个问题只会在方法只有一个参数的情况下发生，第二个函数生成的sql是ok的。
#### 分页
查询参数里`@Limit`，`@OffSet`或查询model里`@Page`的分页功能都比较原始，TgDao只是一款SQL生成器而已，因此你可以使用各种插件，
或者与其他框架集成。对于分页，可以无缝与[PageHelper](https://github.com/pagehelper/Mybatis-PageHelper)整合。

```
@Select
List<User> queryUser2(@Condition(criterion = Criterions.GREATER, column = "age") int min,
                @Condition(criterion = Criterions.LESS, column = "age") int max);


@Test
public void testQueryUser2() throws Exception {
   PageHelper.offsetPage(1, 10);
   List<User> users = mapper.queryUser2(12, 30);
   PageInfo page = new PageInfo<>(users);
   System.out.println(page.getTotal());
   Assert.assertTrue(page.getList().size() > 0);
}
```

---
### 插入
```
@Insert(useGeneratedKeys = true, keyProperty = "id")//获取自增id
int insert(User user);

@BatchInsert(columns = "username,age")//插入的列
int batchInsert(List<User> users);
```
`BatchInsert`强烈建议写columns，因为生成的语句并不会过滤null字段，数据库中插入null易报错。

---
### 更新
```
@Update(columns = "username,age")//选择更新某几个列
@ModelConditions({
       @ModelCondition(field = "id")
})
int update(User user);
```

---
### 删除
```
@Delete
int delete(@Condition(criterion = Criterions.GREATER, column = "age") int min,
          @Condition(criterion = Criterions.LESS, column = "age") int max);

@Delete
@ModelConditions({
       @ModelCondition(attach = Attach.AND, field = "minAge", column = "age", criterion = Criterions.GREATER),
       @ModelCondition(attach = Attach.AND, field = "maxAge", column = "age", criterion = Criterions.LESS)
})
int delete2(UserSearch userSearch);
```
### selective
`@Select`，`@Count`，`@Update`，`@Delete`都有`selective`这个属性，这个属性有两个值，分别是`SqlMode.COMMON`和`SqlMode.SELECTIVE`。
它们的区别在下面这段生成的xm里显示的很清楚，`SqlMode.SELECTIVE`引入了Mybatis的动态SQL能力。
```
  <!-- SELECTIVE -->
  <select id="queryUser" resultMap="BaseResultMap">select username,age from T_User 
    <where>
      <if test="name!=null and name!=''">AND username = #{name}</if>
      <if test="age != null">OR age = #{age}</if>
    </where>
  </select>
  
  <!-- COMMON -->
  <select id="queryUser" resultMap="BaseResultMap">select username,age from T_User 
    <where>
      AND username = #{name} OR age = #{age}
    </where>
  </select>
```

`@Select`，`@Count`默认的selective属性是`SqlMode.SELECTIVE`，这样查询语句可以充分利用Mybatis的动态SQL能力。
而`@Update`，`@Delete`默认是`SqlMode.COMMON`，这样做的原因是：selective模式下如果参数全是`null`会使得where语句里没有任何条件，
最终变成全表的更新和删除，这是一个极其危险的动作。所以`@Update`，`@Delete`慎用`SqlMode.SELECTIVE`模式。

### @Params
在介绍这个注解时要先介绍一下Mybatis自己的`@Param`注解，`@Param`注解在方法的参数上，给参数定义了一个名字，
这样可以在xml的sql里使用这个名字来取得参数所对应的值。如下：
```
    List<User> queryUser(@Param("name") String name);
    
    <select id="queryUser">select * from T_User where username=#{name} </select>
```
明明参数就叫name,为什么还要`@Param`注解一个名字name呢？这是因为Java编译完，会丢掉参数名，以至于运行期mybatis不知道这个参数叫什么，所以需要注解一个名字。
在运行时看到mybatis报错如：`Parameter 'XXX' not found. Available parameters are...` 这就是没有这个注解导致的问题。
但是在Java8里我们已经可以通过给javac 添加`-parameters`参数来保留参数名字信息，这样mybatis会利用这个信息，这样就不需要加`@Param`注解了。
maven可以通过如下方式设置：
```
  <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.1</version>
      <configuration>
          <compilerArgs>
              <arg>-parameters</arg>
          </compilerArgs>
      </configuration>
  </plugin>
```
然而有一种情况`-parameters`也无能为力，`List<User> queryUser4(List ids);`当参数是collection或者数组类型时，mybatis依旧无法认出`ids`这个参数，只认`collection`和`array`。
而`@Params`注解是Mybatis自身注解`@Param`和`-parameters`外的另外一种解决方案。`@Params`可以注解在类和方法上，
被它注解的类和方法会在编译期自动给所有方法参数加上`@Param`注解，它借鉴了lombok的方式在编译期修改抽象语法树从而改变字节码文件。
```
    @Select(columns = "username,age")
    @Params
    List<User> queryUser(Integer age, String username);
    
    //编译后
    List<User> queryUser(@Param("age") Integer var1, @Param("username") String var2);
```

更多请看[example](https://github.com/twogoods/TgDao/tree/master/example)

---
## 说明
* 编译生成的XML文件与Mapper接口在同一个包下
* 只支持Java8和MySql
* 修改了源代码中方法的定义或者model里和数据表的映射关系，发现编译出来的xml却没有改变，这是增量编译的原因。
你修改了一部分代码，还有一部分未修改的代码编译器就不做处理，这样无法得到这部分信息，所以TgDao无法生成最新版本的xml。
解决方法是每次`mvn clean compile`先清除一下编译目录，更好的方案正在寻找...

### 资料
增量编译和`annotation processors` https://issues.gradle.org/browse/GRADLE-3259

how to debug http://blog.jensdriller.com/how-to-debug-a-java-annotation-processor-using-intellij/

修改ast的helloworld：https://gist.github.com/pietrocaselani/8624554

拓展lombok http://notatube.blogspot.hk/2010/12/project-lombok-creating-custom.html