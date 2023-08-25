package com.partner.boot.service.impl;

import com.partner.boot.entity.Comment;
import com.partner.boot.mapper.CommentMapper;
import com.partner.boot.service.ICommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dalaoshi
 * @since 2023-08-24
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

}
