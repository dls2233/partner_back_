package com.partner.boot.service.impl;

import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.partner.boot.common.Constants;
import com.partner.boot.common.enums.EmailCodeEnum;
import com.partner.boot.controller.domain.LoginDTO;
import com.partner.boot.controller.domain.UserRequest;
import com.partner.boot.entity.Permission;
import com.partner.boot.entity.Role;
import com.partner.boot.entity.RolePermission;
import com.partner.boot.entity.User;
import com.partner.boot.exception.ServiceException;
import com.partner.boot.mapper.RolePermissionMapper;
import com.partner.boot.mapper.UserMapper;
import com.partner.boot.service.IPermissionService;
import com.partner.boot.service.IRoleService;
import com.partner.boot.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.partner.boot.util.EmailUtils;
import com.partner.boot.util.RedisUtils;
import io.swagger.models.auth.In;
import jdk.internal.org.objectweb.asm.TypeReference;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.util.validation.Validation;
import org.apache.poi.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/*
   服务实现类
 */
@Service
@Slf4j//service下尽量都使用slf4j，可以使用log等函数，且可以查看日志
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    //key是code，value是当是的时间戳
    //private static final Map<String,Long> CODE_MAP=new ConcurrentHashMap<String, Long>();
    private static final long TIME_IN_MS5 =5*60*1000;//表示五分钟的毫秒数

    @Autowired
    EmailUtils emailUtils;

    @Resource
    RolePermissionMapper rolePermissionMapper;

    @Autowired
    IRoleService roleService;

    @Resource
    IPermissionService permissionService;
    @Override
    public LoginDTO login(UserRequest user) {
        User dbUser=null;
        try {
            dbUser=getOne(new UpdateWrapper<User>().eq("username",user.getUsername()).or()
                    .eq("email",user.getUsername()));
        }catch (Exception e){
            throw new RuntimeException("数据库异常");//会在GlobalExceptionHandle的result对象中返回500（异常）
        }//若出现网络异常等情况中断连接

        if(dbUser==null){
            throw new ServiceException("未找到用户");
        }
//        String securePass = SaSecureUtil.aesEncrypt(Constants.LOGIN_USER_KEY,user.getPassword());
//        if (!securePass.equals(dbUser.getPassword())){
//            throw new ServiceException("用户名或密码错误");
//        }
        if(!BCrypt.checkpw(user.getPassword(),dbUser.getPassword())){
            throw new ServiceException("用户名或密码错误");
        }
        //登录
        StpUtil.login(dbUser.getUid());
        StpUtil.getSession().set(Constants.LOGIN_USER_KEY,dbUser);
        String tokenValue = StpUtil.getTokenInfo().getTokenValue();
        //new LoginDTO= new LoginDTO(dbUser,tokenValue);

        //查询用户的菜单树（2层）
        String flag = dbUser.getRole();
        List<Permission> all = getPermissions(flag);//水平菜单集合
        List<Permission> menus = getTreePermissions(all);//树级
        //页面的按钮权限集合
        List<Permission> auths = all.stream().filter(permission -> permission.getType() == 3).collect(Collectors.toList());
        return LoginDTO.builder().user(dbUser).token(tokenValue).menus(menus).auths(auths).build();
    }
    public List<Permission> getPermissions(String roleFlag){
        Role role = roleService.getOne(new QueryWrapper<Role>().eq("flag",roleFlag));
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(new QueryWrapper<RolePermission>().eq("role_id",role.getId()));
        List<Integer> permissionIds = rolePermissions.stream().map(RolePermission::getPermissionId).collect(Collectors.toList());
        List<Permission> permissionList = permissionService.list();
        List<Permission> all = new ArrayList<>();//水平菜单树，无关联
        for (Integer permissionId : permissionIds){
            permissionList.stream().filter(permission -> permission.getId().equals(permissionId)).
                    findFirst().ifPresent(all::add);
        }
        return all;
    }

    @Override
    public void passwordChange(UserRequest userRequest) {
        User dbUser = getOne(new UpdateWrapper<User>().eq("uid",userRequest.getUid()));
        if(dbUser == null) {
            throw new ServiceException("未找到用户");
        }
        boolean checkpw = BCrypt.checkpw(userRequest.getPassword(),dbUser.getPassword());
        if(!checkpw) {
            throw new ServiceException("原密码错误");
        }
        String newPass = userRequest.getNewPassword();
        dbUser.setPassword(BCrypt.hashpw(newPass));
        updateById(dbUser);
    }

    //获取角色对应的菜单树
    private List<Permission> getTreePermissions(List<Permission> all){
        //树 1级->2级
        List<Permission> parentList = all.stream().filter(permission ->
                permission.getType()==1 || (permission.getType()==2 &&
                        permission.getPid() == null)).collect(Collectors.toList());//type=1,null是目录
        for (Permission permission : parentList){
            Integer pid = permission.getId();
            List<Permission> level2List = all.stream().filter(permission1 -> pid.equals(permission1.getPid())).collect(Collectors.toList());//2级菜单
            permission.setChildren(level2List);
        }
        return parentList.stream().sorted((p1,p2) -> p1.getOrders().compareTo(p2.getOrders())).collect(Collectors.toList());//排序
    }
    @Override
    public void register(UserRequest user) {
        //校验邮箱
        String key=Constants.EMAIL_CODE+ EmailCodeEnum.REGISTER.getValue()+user.getEmail();
        validateEmail(key,user.getEmailCode());
        try {
            User saveUser = new User();
            BeanUtils.copyProperties(user,saveUser);//把请求数据的属性copy给存储数据的属性
            //存储用户信息
            saveUser(saveUser);
        }catch (Exception e){
            throw new RuntimeException("数据库异常",e);
        }
    }

    @Override
    public void sendEmail(String email, String type) {
        String emailPrefix = EmailCodeEnum.getValue(type);//email前缀
        if(StrUtil.isBlank(emailPrefix)){
            throw new ServiceException("不支持的邮箱验证类型");
        }
        //设置rediskey
        String key=Constants.EMAIL_CODE + emailPrefix + email;
        Long expireTime = RedisUtils.getExpireTime(key);
        //限制时间超过一分钟才可以继续发送邮件，判断过期时间是否大于四分钟
        if (expireTime!=null && expireTime > 4*60 ){
            throw new ServiceException("发送邮箱过于频繁");
        }
        Integer code = Integer.valueOf(RandomUtil.randomNumbers(6));//生成随机六位长度的验证码
        log.info("本次验证的code是：{}",code);
        String context = "<b>尊敬的用户，</b><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;您好，" +
                "Partner交友网提醒您本次的验证码是：<b>{}<b>，有效期为5分钟。<br><br><br><b>Partner交友网</b>";
        String html= StrUtil.format(context,code);//替换context中的code
        User user=getOne(new QueryWrapper<User>().eq("email",email));
        if(EmailCodeEnum.REGISTER.equals(EmailCodeEnum.getEnum(type))){ //注册(无需权限验证，即可发送邮箱验证码)
            //校验邮箱是否注册
            if(user!=null){
                throw new ServiceException("邮箱已注册");
            }
        }else if(EmailCodeEnum.RESET_PASSWORD.equals(EmailCodeEnum.getEnum(type))){
            if(user==null){
                throw new ServiceException("未找到用户");
            }
        }
        //忘记密码时
        ThreadUtil.execAsync(()->{
            emailUtils.sendHtml("【Partner交友网】邮箱验证提醒",html,email);

            //设置redis缓存
            RedisUtils.setCacheObject(key,code,TIME_IN_MS5,TimeUnit.MILLISECONDS);
        });//异步请求（多线程）可与后面一步同时实现，防止网络卡顿等情况
        //CODE_MAP.put(email+code,System.currentTimeMillis());//code和当前的系统时间，往缓存中put
        //设置redis缓存
        RedisUtils.setCacheObject(key,code,TIME_IN_MS5,TimeUnit.MILLISECONDS);
    }
    //重置密码
    @Override
    public String passwordReset(UserRequest userRequest) {
        String email = userRequest.getEmail();
        User dbUser = getOne(new UpdateWrapper<User>().eq("email",email));
        if(dbUser == null){
            throw new ServiceException("未找到用户");
        }
        //校验邮箱验证码
        String key=Constants.EMAIL_CODE+ EmailCodeEnum.RESET_PASSWORD.getValue()+email;
        validateEmail(key,userRequest.getEmailCode());
        String newPass = "123";
        dbUser.setPassword(BCrypt.hashpw(newPass));
        try {
            updateById(dbUser);//将重置密码设置到数据库
        }catch (Exception e){
            throw new RuntimeException("修改失败",e);
        }
        return newPass;
    }
    @Override
    public void logout(String uid) {
        //退出登录
        StpUtil.logout(uid);
        log.info("用户{}退出成功",uid);
    }
    private void validateEmail(String emailKey,String emailCode){
        //String key=email+emailCode;
        //校验邮箱

        Integer code=RedisUtils.getCacheObject(emailKey);//自带计时器功能
        if(code == null){
            throw new ServiceException("验证码失效");
        }
        if(!emailCode.equals(code.toString())){
            throw new ServiceException("验证码错误");
        }
//        Long timestamp =CODE_MAP.get(key);
//        if(timestamp == null){
//            throw new ServiceException("请先验证邮箱");
//        }
//        if(timestamp + TIME_IN_MS5 < System.currentTimeMillis()){//说明验证码过期
//            throw new ServiceException("验证码过期");
//       }
//       CODE_MAP.remove(emailCode);、
        // 清除缓存
        RedisUtils.deleteObject(emailKey);
    }
    public User saveUser(User user){
        User dbUser = getOne(new UpdateWrapper<User>().eq("username",user.getUsername()));
        if(dbUser != null){
            throw new ServiceException("用户已存在");
        }
        //设置昵称(传来的参数中有名称时，不设置默认名称)
        if(StrUtil.isBlank(user.getName())){
            String name=Constants.USER_NAME_PREFIX + RandomUtil.randomString(4);
            user.setName(name);//format中前一个参数表示传递的时间，后一个表示时间格式
        }
        if(StrUtil.isBlank(user.getPassword())){
            user.setPassword("123");//如果没有密码，则设置默认密码
        }
        //加密用户密码
        //user.setPassword(SaSecureUtil.aesEncrypt(Constants.LOGIN_USER_KEY,user.getPassword()));
        user.setPassword(BCrypt.hashpw(user.getPassword()));//BCrypt加密
        //设置唯一标识
        user.setUid(IdUtil.fastSimpleUUID());

        try {
            save(user);//保存数据至数据库
        }catch (Exception e){
            throw new RuntimeException("注册失败",e);

        }
        return user;
    }
}
