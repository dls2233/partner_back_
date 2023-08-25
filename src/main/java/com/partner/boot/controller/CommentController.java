package com.partner.boot.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.partner.boot.common.Constants;
import com.partner.boot.entity.User;
import com.partner.boot.service.IUserService;
import lombok.val;
import org.apache.poi.xssf.model.Comments;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.partner.boot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.partner.boot.service.ICommentService;
import com.partner.boot.entity.Comment;

import org.springframework.web.bind.annotation.RestController;

/**
* <p>
*  前端控制器
* </p>
*
* @author dalaoshi
* @since 2023-08-24
*/
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private ICommentService commentService;
    @Resource
    private IUserService userService;

    @PostMapping
    //@SaCheckPermission("comment.add")
    public Result save(@RequestBody Comment comment) {
        User user = (User) StpUtil.getSession().get(Constants.LOGIN_USER_KEY);
        comment.setUserId(user.getId());
        comment.setLocation("重庆");
        commentService.save(comment);
        return Result.success();
    }

    @PutMapping
    //@SaCheckPermission("comment.edit")
    public Result update(@RequestBody Comment comment) {
        commentService.updateById(comment);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    //@SaCheckPermission("comment.delete")
    public Result delete(@PathVariable Integer id) {
        List<Comment> children = commentService.list(new QueryWrapper<Comment>().eq("pid",id));
        for (Comment child : children){
            //删除子评论
            commentService.removeById(child.getId());
        }
        commentService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    //@SaCheckPermission("comment.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        commentService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping("/tree")
    //@SaCheckPermission("comment.list")
    public Result tree(@RequestParam Integer dynamicId) {
        List<User> userList = userService.list();
        List<Comment> list = commentService.list(new QueryWrapper<Comment>().eq("dynamic_id",dynamicId));
        //给comment里面的每个对象设置一个属性
        list = list.stream().peek(comment -> userList.stream().filter(user -> user.getId()
                .equals(comment.getUserId())).findFirst().ifPresent(comment::setUser))
                .collect(Collectors.toList());
        List<Comment> first = list.stream().filter(comment -> comment.getPid() == null).collect(Collectors.toList());//一级评论
        for (Comment comment : first){
            Integer pid = comment.getId();
            List<Comment> second = list.stream().filter(comment1 -> Objects.equals(pid,comment1.getPid())).collect(Collectors.toList());//二级评论
            //给second里的每一个对象设置一个pUser属性
            second.stream().peek(comment1 -> userList.stream().filter(user -> user.getId()
                    .equals(comment1.getPuserId())).findFirst().
                    ifPresent(comment1::setPUser)).collect(Collectors.toList());
            comment.setChildren(second);//一级评论设置二级评论
        }
        return Result.success(first);
    }

    @GetMapping
    //@SaCheckPermission("comment.list")
    public Result findAll() {
        return Result.success(commentService.list());
    }

    @GetMapping("/{id}")
    //@SaCheckPermission("comment.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(commentService.getById(id));
    }

    @GetMapping("/page")
    //@SaCheckPermission("comment.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<Comment>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(commentService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
    //@SaCheckPermission("comment.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Comment> list = commentService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Comment信息表", "UTF-8");
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
    //@SaCheckPermission("comment.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Comment> list = reader.readAll(Comment.class);

        commentService.saveBatch(list);
        return Result.success();
    }

}
