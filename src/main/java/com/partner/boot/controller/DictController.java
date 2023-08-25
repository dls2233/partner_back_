package com.partner.boot.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.partner.boot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.partner.boot.service.IDictService;
import com.partner.boot.entity.Dict;

import org.springframework.web.bind.annotation.RestController;

/**
* <p>
*  前端控制器
* </p>
*
* @author dalaoshi
* @since 2023-08-16
*/
@RestController
@RequestMapping("/dict")
public class DictController {

    @Resource
    private IDictService dictService;

    @PostMapping
    @CacheEvict(value = "findIcons",allEntries = true)//在存入数据时清楚缓存当前数据
    //@SaCheckPermission("dict.add")
    public Result save(@RequestBody Dict dict) {
        dictService.save(dict);
        return Result.success();
    }

    @PutMapping
    @CacheEvict(value = "findIcons",allEntries = true)//删除所有缓存数据
    //@SaCheckPermission("dict.edit")
    public Result update(@RequestBody Dict dict) {
        dictService.updateById(dict);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "findIcons", allEntries = true)
    //@SaCheckPermission("dict.delete")
    public Result delete(@PathVariable Integer id) {
        dictService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    @CacheEvict(value = "findIcons",allEntries = true)
    //@SaCheckPermission("dict.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        dictService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    //@SaCheckPermission("dict.list")
    public Result findAll() {
        return Result.success(dictService.list());
    }

    @GetMapping("/icons")
    //@SaCheckPermission("dict.list")
    public Result findByType() {
         return Result.success(dictService.findIcons());
    }

    @GetMapping("/{id}")
    //@SaCheckPermission("dict.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(dictService.getById(id));
    }

    @GetMapping("/page")
    //@SaCheckPermission("dict.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<Dict>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(dictService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
    //@SaCheckPermission("dict.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Dict> list = dictService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Dict信息表", "UTF-8");
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
    //@SaCheckPermission("dict.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Dict> list = reader.readAll(Dict.class);

        dictService.saveBatch(list);
        return Result.success();
    }

}
