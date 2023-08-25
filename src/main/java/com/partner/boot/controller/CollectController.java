package com.partner.boot.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.partner.boot.common.Constants;
import com.partner.boot.entity.User;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.swing.text.AbstractDocument;
import java.io.InputStream;
import java.util.Currency;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.partner.boot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.partner.boot.service.ICollectService;
import com.partner.boot.entity.Collect;

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
@RequestMapping("/collect")
public class CollectController {

    @Resource
    private ICollectService collectService;

    @PostMapping
    //@SaCheckPermission("collect.add")
    public Result save(@RequestBody Collect collect) {
        User user = (User) StpUtil.getSession().get(Constants.LOGIN_USER_KEY);
        collect.setUserId(user.getId());
        try {
            collectService.save(collect);
        }catch (Exception e){
            //唯一冲突就删除
            collectService.remove(new UpdateWrapper<Collect>().eq("user_id",user.
                    getId()).eq("dynamic_id",collect.getDynamicId()));
        }
        return Result.success();
    }

    @PutMapping
    //@SaCheckPermission("collect.edit")
    public Result update(@RequestBody Collect collect) {
        collectService.updateById(collect);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    //@SaCheckPermission("collect.delete")
    public Result delete(@PathVariable Integer id) {
        collectService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    //@SaCheckPermission("collect.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        collectService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    //@SaCheckPermission("collect.list")
    public Result findAll() {
        return Result.success(collectService.list());
    }

    @GetMapping("/{id}")
    //@SaCheckPermission("collect.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(collectService.getById(id));
    }

    @GetMapping("/page")
    //@SaCheckPermission("collect.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Collect> queryWrapper = new QueryWrapper<Collect>().orderByDesc("id");
        User currentUser = (User) StpUtil.getSession().get(Constants.LOGIN_USER_KEY);
        String role = currentUser.getRole();
        if("1".equals(role)||"3".equals(role)){
            queryWrapper.eq("user_id",currentUser.getId());
        }
        return Result.success(collectService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
    //@SaCheckPermission("collect.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Collect> list = collectService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Collect信息表", "UTF-8");
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
    //@SaCheckPermission("collect.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Collect> list = reader.readAll(Collect.class);

        collectService.saveBatch(list);
        return Result.success();
    }

}
