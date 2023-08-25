package com.partner.boot.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;

import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.io.Console;
import java.net.URLEncoder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.partner.boot.common.Constants;
import com.partner.boot.entity.*;
import com.partner.boot.service.*;
import io.netty.util.Constant;
import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.manager.util.SessionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.swing.text.ParagraphView;
import javax.swing.text.html.Option;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.partner.boot.common.Result;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 动态 前端控制器
 * </p>
 *
 * @author dalaoshi
 * @since 2023-02-12
 */
@RestController
@RequestMapping("/dynamic")
@Slf4j
public class DynamicController {

    @Resource
    IUserService userService;

    @Resource
    private IDynamicService dynamicService;

    @Resource
    IPraiseService praiseService;

    @Resource
    ICollectService collectService;

    @Resource
    ICommentService commentService;

    @PostMapping
    //@SaCheckPermission("dynamic.add")
    public Result save(@RequestBody Dynamic dynamic) {
        User user = (User) StpUtil.getSession().get(Constants.LOGIN_USER_KEY);//在后台获取用户信息
        dynamic.setUid(user.getUid());
        dynamicService.save(dynamic);
        return Result.success();
    }

    @PutMapping
    @SaCheckPermission("dynamic.edit")
    public Result update(@RequestBody Dynamic dynamic) {
        dynamicService.updateById(dynamic);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    //@SaCheckPermission("dynamic.delete")
    public Result delete(@PathVariable Integer id) {
        dynamicService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    //@SaCheckPermission("dynamic.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        dynamicService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    //@SaCheckPermission("dynamic.list")
    public Result findAll() {
        return Result.success(dynamicService.list());
    }

    //热门动态
    @GetMapping("/hot")
    //@SaCheckPermission("dynamic.list.hot")
    public Result hot() {
        List<Dynamic> list = dynamicService.list();
        List<Praise> praiseList = praiseService.list();
        List<Collect> collectList = collectService.list();
        List<Comment> commentList = commentService.list();
        for (Dynamic dynamic : list){
            int praiseCount =(int) praiseList.stream().filter(p -> p.getFid().equals(dynamic.getId())).count();//点赞的个数
            int collectCount =(int) collectList.stream().filter(p -> p.getDynamicId().equals(dynamic.getId())).count();//点赞的个数
            int commentCount =(int) commentList.stream().filter(p -> p.getDynamicId().equals(dynamic.getId())).count();//点赞的个数
            dynamic.setHot(praiseCount * 2 + collectCount * 2 + commentCount * 2 + dynamic.getView());
        }
        return Result.success(list.stream().sorted((d1,d2) -> d2.getHot().compareTo(d1.getHot())).limit(8));
    }

    @GetMapping("/{id}")
    @SaIgnore//不登陆可查询
    //@SaCheckPermission("dynamic.list")
    public Result findOne(@PathVariable Integer id) {
        dynamicService.updateView(id);
        Dynamic dynamic = dynamicService.getById(id);
        String uid = dynamic.getUid();
        User uid1 = userService.getOne(new QueryWrapper<User>().eq("uid",uid));
        Optional.of(uid1).ifPresent(dynamic::setUser);
        //查到点赞的数据
        List<Praise> list = praiseService.list();
        User user = (User) StpUtil.getSession().get(Constants.LOGIN_USER_KEY);
        //当前用户是否点赞
        dynamic.setHasPraise(list.stream().anyMatch(praise -> praise.getUserId().equals(user.getId())&&praise.getFid().equals(dynamic.getId())));
        dynamic.setPraiseCount((int) list.stream().filter(praise -> praise.getFid().equals(dynamic.getId())).count());

        //查询收藏的数据
        List<Collect> collectList = collectService.list();
        //当前用户是否收藏
        dynamic.setHasCollect(collectList.stream().anyMatch(collect -> collect.getUserId().equals(user.getId())&&collect.getDynamicId().equals(dynamic.getId())));
        dynamic.setCollectCount((int) collectList.stream().filter(collect -> collect.getDynamicId().equals(dynamic.getId())).count());

        return Result.success(dynamic);
    }

    @GetMapping("/page")
    @SaIgnore//不登陆就可以查询
    //@SaCheckPermission("dynamic.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "") String type,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Dynamic> queryWrapper = new QueryWrapper<Dynamic>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        User currentUser = (User) StpUtil.getSession().get(Constants.LOGIN_USER_KEY);
        String role = currentUser.getRole();
        if("1".equals(role)||"3".equals(role)){
            if ("user".equals(type)){
                queryWrapper.eq("uid",currentUser.getUid());
            }
        }
        Page<Dynamic> page = dynamicService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Praise> praiseList = praiseService.list();
        List<Collect> collectList = collectService.list();
        List<Comment> commentList = commentService.list();
        List<Dynamic> records = page.getRecords();
        List<User> userList = userService.list();
        for (Dynamic record : records){
            //查出用户信息
            userList.stream().filter(user -> record.getUid().equals(user.getUid())).findFirst().ifPresent(record::setUser);
            //查出多少点赞
            int praiseCount = (int) praiseList.stream().filter(praise -> praise.getFid().equals(record.getId())).count();//点赞数
            record.setPraiseCount(praiseCount);
            //查出多少收藏
            int collectCount = (int) collectList.stream().filter(collect -> collect.getDynamicId().equals(record.getId())).count();//收藏数
            record.setCollectCount(collectCount);
            //查出多少评论
            int commentCount = (int) commentList.stream().filter(comment -> comment.getDynamicId().equals(record.getId())).count();//收藏数
            record.setCommentCount(commentCount);
        }
        return Result.success(page);
    }

    /**
     * 导出接口
     */
    @GetMapping("/export")
    //@SaCheckPermission("dynamic.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Dynamic> list = dynamicService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Dynamic信息表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();

    }

    /**
     * excel 导入
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    //@SaCheckPermission("dynamic.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Dynamic> list = reader.readAll(Dynamic.class);

        dynamicService.saveBatch(list);
        return Result.success();
    }

}
