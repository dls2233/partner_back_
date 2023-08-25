package com.partner.boot.service.impl;

import com.partner.boot.entity.Permission;
import com.partner.boot.mapper.PermissionMapper;
import com.partner.boot.service.IPermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dalaoshi
 * @since 2023-08-13
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

    @Override
    public List<Permission> tree() {
        List<Permission> allData = list();

        return childrenTree(null,allData);
    }
    //递归生成树
    private List<Permission> childrenTree(Integer pid,List<Permission> allData) {
        List<Permission> list = new ArrayList<>();
        //if(pid==null){
        //    allData.stream().filter(p -> p.getPid() == null).collect(Collectors.toList());
        //}
        //return allData.stream().filter(p -> Objects.equals(p.getId(),pid)).collect(Collectors.toList());
        for (Permission permission : allData){
            if (Objects.equals(permission.getPid(),pid)){//null为一级
                list.add(permission);
                List<Permission> childrenTree = childrenTree(permission.getId(),allData);//递归调用，摘取二三四...级节点
                permission.setChildren(childrenTree);
            }
        }
        return list;
    }
}
