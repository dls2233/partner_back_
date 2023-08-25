package com.partner.boot.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.simple.SimpleDataSource;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import sun.security.util.AuthResources_it;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * 代码生成器
 * v1.0
 * 作者：dalaoshi
 */
@Slf4j
public class CodeGenerator {
    private static final String PACKAGE_NAME="com.partner.boot";
    private static final String AUTHOR="dalaoshi";

    private static final String TABLE="comment";
    private static final String VUE_CODEPATH = "D:\\partner前台\\partner-manager\\src\\views\\";
    private static final String PROJECT_PATH = System.getProperty("user.dir");
    public static final String MAPPER_XML_PATH = "/src/main/resources/mapper/";
    public static final String JAVA_CODE_PATH = "/src/main/java/";

    private static final String SPACE14 = "              ";
    private static final String SPACE16 = "                ";

    //防止二次使用重制数据
    public static void main(String[] args) throws SQLException {
        //generateJava(TABLE);   // 生成Java后台代码

        //generateVue(TABLE);

    }

    private static void generateVue (String tableName) throws SQLException {

        List<TableColumn> tableColumns = getTableColumns(tableName);
        //读取模板，生成代码
        String vueTemplate = ResourceUtil.readUtf8Str("templates/vue.template");
        //封装模板
        Map<String, String> map = new HashMap<>();

        map.put("lowerEntity",getLowerEntity(tableName));//接口前缀

        String vueTableBody = getVueTableBody(tableColumns);
        map.put("tableBody",vueTableBody);

        String vueFormBody = getVueFormBody(tableColumns);
        map.put("formBody",vueFormBody);

        //生成页面代码
        String vuePage = StrUtil.format(vueTemplate, map);//VuePage是替换字符串模板后的内容
        //写文件:D:\partner前台\partner-manager\src\view写入出的文件目录
        String entity = getEntity(tableName);
        FileUtil.writeUtf8String(vuePage,VUE_CODEPATH+entity+".vue");
        log.debug("============="+entity+".Vue文件生成完成==============");
    }
    private static List<TableColumn> getTableColumns(String tableName) {
        //获取数据库连接的信息
        DBProp dbProp = getDBProp();
        //连接数据库
        DataSource dataSource = new SimpleDataSource("jdbc:mysql://localhost:3306/information_schema",dbProp.getUsername(),dbProp.getPassword());
        Db db = DbUtil.use(dataSource);
        //拿到实际要生成代码数据库的名称
        String url = dbProp.getUrl();
        String schema = url.substring(url.indexOf("3306/")+5,url.indexOf("?"));
        List<TableColumn> tableColumnList = new ArrayList<>();
        try {
            List<Entity> columns = db.findAll(Entity.create("COLUMNS").set("TABLE_SCHEMA",schema).set("TABLE_NAME",tableName));
            //封装结构化表数据的信息
            for (Entity entity : columns) {
                String columnName = entity.getStr("COLUMN_NAME");//字段名称
                String dataType = entity.getStr("DATA_TYPE");//数据类型
                String columnComment = entity.getStr("COLUMN_COMMENT");//字段名称
                TableColumn tableColumn = TableColumn.builder().columnName(columnName).dataType(dataType).columnComment(columnComment).build();
                tableColumnList.add(tableColumn);

            }
            //封装结构化的表数据信息
            System.out.println(tableColumnList);
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return tableColumnList;
    }
    private static String getVueTableBody(List<TableColumn> tableColumnList){
        StringBuilder builder = new StringBuilder();
        for (TableColumn tableColumn :tableColumnList){
            if(tableColumn.getColumnName().equalsIgnoreCase("id") && StrUtil.isBlank(tableColumn.getColumnComment())){
                tableColumn.setColumnComment("编号");
            }
            if(tableColumn.getColumnName().equalsIgnoreCase("deleted") || tableColumn.getColumnName()
                    .equalsIgnoreCase("create_time") || tableColumn.getColumnName().equalsIgnoreCase("update_time")){//排除deleted无需关注字段
            }
            String column = SPACE16 + "<el-table-column prop=\""+tableColumn.getColumnName()+
                    "\" label=\"" + tableColumn.getColumnComment()+"\"></el-table-column>\n";
            builder.append(column);
        }
        return builder.toString();
    }
    private static String getVueFormBody(List<TableColumn> tableColumnList){
        StringBuilder builder = new StringBuilder();
        for (TableColumn tableColumn :tableColumnList){
            if(tableColumn.getColumnName().equalsIgnoreCase("id")){
                continue;
            }
            if(tableColumn.getColumnName().equalsIgnoreCase("deleted") || tableColumn.getColumnName()
                    .equalsIgnoreCase("create_time") || tableColumn.getColumnName().equalsIgnoreCase("update_time")){//排除deleted无需关注字段
                continue;
            }
            String column = SPACE14+"<el-form-item prop=\""+tableColumn.getColumnName()+"\" label=\""+tableColumn.getColumnComment()+"\">\n" +
                    SPACE14+"  <el-input v-model=\"state.form."+tableColumn.getColumnName()+"\" autocomplete=\"off\" />\n" +
                    SPACE14+"</el-form-item>\n";
            builder.append(column);
        }
        return builder.toString();
    }
    private static String getLowerEntity(String tableName){
        tableName.replace("t_","").replace("sys_","");
        return StrUtil.toCamelCase(tableName);
    }
    private static String getEntity(String tableName){
        String lowerEntity = getLowerEntity(tableName);
        return lowerEntity.substring(0,1).toUpperCase() + lowerEntity.substring(1);
    }

    private static DBProp getDBProp() {
        ClassPathResource resource = new ClassPathResource("application.yml");
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(resource)  ;
        Properties dbProp = yamlPropertiesFactoryBean.getObject();
        return DBProp.builder().url(dbProp.getProperty("spring.datasource.url"))
                .username(dbProp.getProperty("spring.datasource.username"))
                .password(dbProp.getProperty("spring.datasource.password")).build();
    }
    private static void generateJava(String tableName) {
        DBProp dbProp = getDBProp();
        FastAutoGenerator.create(dbProp.getUrl(), dbProp.getUsername(), dbProp.getPassword())
                .globalConfig(builder -> {
                    builder.author(AUTHOR) // 设置作者
                            .enableSwagger()
                            .disableOpenDir()
                            .outputDir(PROJECT_PATH + JAVA_CODE_PATH); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent(PACKAGE_NAME) // 设置父包名
                            .moduleName("") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, PROJECT_PATH + MAPPER_XML_PATH)); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.controllerBuilder().fileOverride().enableRestStyle().enableHyphenStyle()
                            .serviceBuilder().fileOverride()
                            .mapperBuilder().fileOverride()
                            .entityBuilder().fileOverride().enableLombok()
                            .logicDeleteColumnName("deleted")
                            .addTableFills(new Column("update_time", FieldFill.INSERT_UPDATE));
                    builder.addInclude(tableName) // 设置需要生成的表名
                            .addTablePrefix("t_", "sys_"); // 设置过滤表前缀
                })
                .templateConfig(builder -> {
                    builder.controller("/templates/controller.java");
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }

}
