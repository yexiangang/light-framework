# authorization
登录验证,权限验证模块
## maven
```xml
<dependency>
    <groupId>net.shmin</groupId>
    <artifactId>authorization</artifactId>
    <version>2.1.0-SNAPSHOT</version>
</dependency>
```
## 实现PasswordValidator
```java
@Compent("customValidator")
public class CustomValidator implements PasswordValidator {
    public CommonResponseDTO login(HttpServletRequest request) throws Exception {

        String username = request.getParameter("username");
        //TODO your login logic
        return CommonResponseDTO.success();
    }
}
```

## 配置拦截器
```xml
<mvc:interceptors>
        <!-- 登录验证拦截器 -->
        <mvc:interceptor>
            <mvc:mapping path="/**"/>
            <!-- 登录接口路径 -->
            <mvc:exclude-mapping path="/authorize"/>
            <!-- 其他不需要登录验证的接口 -->
            <mvc:exclude-mapping path="/other_path"/>
            <bean class="AuthorizationInterceptor">
                <!-- 请看verfiyCode详解 -->
                <property name="verifyCode" value="false"></property>
                <!-- 登录页地址 默认值"/login.html"-->
                <property name="loginUrl" value="/login.html"/>
            </bean>
        </mvc:interceptor>
        <!-- 权限验证拦截器 -->
        <mvc:interceptor>
            <mvc:mapping path="/**"/>
            <bean class="PrivilegeInterceptor">
            </bean>
        </mvc:interceptor>
</mvc:interceptors>
```    
## verifyCode详解
verfiyCode目的在于校验请求在传输过程中是否被篡改. true 启用校验 false 不启用校验.
### 校验规则:
* 1.将所有参数按照字典顺序排序,例如参数排序结果:a=b&c=d&e=f
* 2.对排序结果进行md5签名
* 3.将签名值放入Request Header中, 键为Verify-Code 值为第二步中的md5签名值.

对于无参数的请求将不传递Verify-Code值.

## 权限验证
```java
@org.springframework.web.bind.annotation.RestController
@org.springframework.web.bind.annotation.RequestMapping("a")
public class AController {
    @org.springframework.web.bind.annotation.RequestMapping("b")
    @net.shmin.auth.permission.Privilege(resourceId = 1, needed = {net.shmin.auth.permission.model.ACLEnum.CREATE}, profile = "dev")
    public CommonResponseDTO login(HttpServletRequest request) throws Exception {

        String username = request.getParameter("username");
        //TODO your login logic
        return CommonResponseDTO.success();
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Privilege {

    /**
     * 要访问的资源id
     *
     * @return
     */
    String[] resourceId();

    /**
     * 执行检查权限检查的类名
     */
    Class<? extends IPermissionValidator> permissionValidator() default LebiPermissionManagerImpl.class;

    /**
     * 访问该资源应该具有什么权限
     */
    ACLEnum[] needed();

    /**
     * 属于哪个环境下
     * @return
     */
    String profile();

}
```

## authorization 验证相关配置
```java
##
# authorization 验证相关配置
##
# 内置登录验证器 (此属性不要使用 已废弃 参照 password-validator.beanName)
#password-validator.class = OSSPasswordValidator
## 内置登录验证器(传递beanName 纳入spring ioc容器管理) 默认(内置le oss 登录)
## 如果用以上的password-validator 则值为 customValidator
password-validator.beanName = passwordValidator 

# 单位分钟 (默认 7天) access-token有效期
access-token.expires =

# 单位分钟 默认(10分钟) authorization-code有效期
authorization-code.expires =

# 单位分钟 默认(30天) refresh-token有效期
refresh-token.expires =

# 防止命名冲突 token的key的前缀 (默认为空) 
token.key.prefix =

# 防止命名冲突 token的key的后缀 格式: token.key.prefix + username + access-token.key.suffix
access-token.key.suffix = _ACCESS_TOKEN

# 防止命名冲突 token的key的后缀 格式: token.key.prefix + username + authorization-code.key.suffix
authorization-code.key.suffix = _AUTHORIZATION_CODE

# 防止命名冲突 token的key的后缀 格式: token.key.prefix + username + refresh-token.key.suffix
refresh-token.key.suffix = _REFRESH_TOKEN

# access_token 存放的redis数据库
access-token.redis.database = 0

# authorization.code 存放的redis数据库
authorization-code.redis.database = 0

# refresh.token 存放的redis数据库
refresh-token.redis.database = 0

# access_token 的cookie name 默认为access_token
cookie.access-token.name = access_token

# username 的cookie name 默认为username
cookie.username.name = username

# 登录请求request 中 username 的parameter name 默认为username
request.authorize.param.username = username
```

## [Demo案例(light-framework-project)](https://github.com/shangmin1990/light-framework-project)