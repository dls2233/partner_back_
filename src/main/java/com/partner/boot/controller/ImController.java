package com.partner.boot.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.partner.boot.common.Result;
import org.springframework.web.multipart.MultipartFile;
import com.partner.boot.service.IImService;
import com.partner.boot.entity.Im;

import org.springframework.web.bind.annotation.RestController;

/**
* <p>
*  前端控制器
* </p>
*
* @author dalaoshi
* @since 2023-08-18
*/
@RestController
@RequestMapping("/im")
public class ImController {

    @Resource
    private IImService imService;

    @PostMapping
    //@SaCheckPermission("im.add")
    public Result save(@RequestBody Im im) {
        imService.save(im);
        return Result.success();
    }

    @PutMapping
    //@SaCheckPermission("im.edit")
    public Result update(@RequestBody Im im) {
        imService.updateById(im);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    //@SaCheckPermission("im.delete")
    public Result delete(@PathVariable Integer id) {
        imService.removeById(id);
        return Result.success();
    }

    @PostMapping("/del/batch")
    //@SaCheckPermission("im.deleteBatch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        imService.removeByIds(ids);
        return Result.success();
    }

    @GetMapping
    //@SaCheckPermission("im.list")
    public Result findAll() {
        return Result.success(imService.list());
    }

    @GetMapping("/init{limit}")
    //@SaCheckPermission("im.list")
    public Result findAllInit(@PathVariable Integer limit) {
        List<Im> ims = imService.list(new QueryWrapper<Im>().orderByDesc("id").last("limit"+limit));
        return Result.success(ims.stream().sorted(Comparator.comparing(Im::getId)));
    }

    @GetMapping("/{id}")
    //@SaCheckPermission("im.list")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(imService.getById(id));
    }

    @GetMapping("/page")
    //@SaCheckPermission("im.list")
    public Result findPage(@RequestParam(defaultValue = "") String name,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize) {
        QueryWrapper<Im> queryWrapper = new QueryWrapper<Im>().orderByDesc("id");
        queryWrapper.like(!"".equals(name), "name", name);
        return Result.success(imService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
    * 导出接口
    */
    @GetMapping("/export")
    //@SaCheckPermission("im.export")
    public void export(HttpServletResponse response) throws Exception {
        // 从数据库查询出所有的数据
        List<Im> list = imService.list();
        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("Im信息表", "UTF-8");
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
    //@SaCheckPermission("im.import")
    public Result imp(MultipartFile file) throws Exception {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        // 通过 javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
        List<Im> list = reader.readAll(Im.class);

        imService.saveBatch(list);
        return Result.success();
    }

}
