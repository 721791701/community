package com.bbgu.zmz.community.service;

import com.bbgu.zmz.community.dto.Result;
import com.bbgu.zmz.community.enums.MsgEnum;
import com.bbgu.zmz.community.mapper.*;
import com.bbgu.zmz.community.mapper.TopicinfoExtMapper;
import com.bbgu.zmz.community.model.*;
import com.bbgu.zmz.community.model.TopicinfoExt;
import com.bbgu.zmz.community.util.RedisUtil;
import com.bbgu.zmz.community.util.StringDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class TopicService {
    @Autowired
    private TopicinfoMapper topicinfoMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private KindMapper kindMapper;
    @Autowired
    private TopicinfoExtMapper topicinfoExtMapper;
    @Autowired
    private CollectMapper collectMapper;
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private CommentExtMapper commentExtMapper;
    @Autowired
    private RedisUtil redisUtil;

    /*
    获取置顶帖子信息 || 获取最新综合帖子信息
     */
    public List<TopicinfoExt> topicTop(Integer istop, Integer offset, Integer size) {
        List<TopicinfoExt> topicinfoExtList = topicinfoExtMapper.getTopTopic(istop, offset, size);
        for (TopicinfoExt topicinfoExt : topicinfoExtList) {
            topicinfoExt.setTime(StringDate.getStringDate(new Date(topicinfoExt.getTopicCreate())));
        }
        return topicinfoExtList;
    }

    /*
    查询一级分类
     */
    public List<Category> findCate() {
        List<Category> categoryList = categoryMapper.selectAll();
        return categoryList;
    }

    /*
    查询二级分类
     */
    public List<Kind> findKind() {
        List<Kind> kindList = kindMapper.selectAll();
        return kindList;
    }

    /*
    插入帖子信息
     */
    public int addTopic(Topicinfo topicinfo) {
        if (topicinfo.getId() != null) {
            topicinfoMapper.updateByPrimaryKeySelective(topicinfo);
            return 10;
        } else {
            int result = topicinfoMapper.insertSelective(topicinfo);
            return result;
        }
    }

    /*
    查看帖子详情
     */
    public TopicinfoExt showDetail(Long id) {
        TopicinfoExt topicinfoExt = topicinfoExtMapper.getTopicDetails(id);
        topicinfoExt.setTime(StringDate.getStringDate(new Date(topicinfoExt.getTopicCreate())));
        return topicinfoExt;

    }
    /*
    查看相关分类帖子
     */
    public List<Topicinfo> classifyTopic(Long catrgoryId,Long kindId){
        Example example = new Example(Topicinfo.class);
        example.setOrderByClause("topic_create desc");
        example.createCriteria().andEqualTo("categoryId",catrgoryId).andEqualTo("kindId",kindId).andEqualTo("status",1);
        return  topicinfoMapper.selectByExample(example);
    }

    /*
    更新浏览数
     */
    public void incView(Topicinfo topicinfo) {
        topicinfoExtMapper.incView(topicinfo);
    }

    /*
    插入评论
     */
    public Long insertComment(Comment comment) {
        commentMapper.insertSelective(comment);
        Long id = comment.getId();
        Map map = new HashMap();
        map.put("kissNum",5);
        map.put("accountId",comment.getUserId());
        topicinfoExtMapper.incExperience(map);
        return id;
    }

    /*
      查询评论
       */
    public List<CommentExt>  findComment(Long id,Integer page,Integer size,User userinfo){
        Integer offset = (page -1)*size;
        List<CommentExt> commentExtList = commentExtMapper.getCommentByTid(id,offset,size);
        Map map = new HashMap();
        if (!redisUtil.exists("agreeCount")){
            List<Comment> commentList = commentMapper.selectAll();
            for(Comment comment:commentList){
                map.put(comment.getId().toString(),comment.getAgreeNum().toString());
            }
            System.out.println("点赞数从数据库中取");

        }
        redisUtil.setHashMap("agreeCount",map);
        for(CommentExt commentExt:commentExtList){
            //查询是否点赞
            if(userinfo != null){
                Boolean zan = redisUtil.getSetOneExit("c"+commentExt.getId().toString(),userinfo.getAccountId().toString());
                if(zan){
                    commentExt.setZan(true);
                }else{
                    commentExt.setZan(false);
                }
                commentExt.setAgreeNum(Long.parseLong(redisUtil.getMapOneValue("agreeCount",commentExt.getId().toString())));
            }
            //转换时间
            commentExt.setTime(StringDate.getStringDate(new Date(commentExt.getCommentCreate())));
        }
        return commentExtList;
    }

    /*
    审核帖子
     */
    public void shenHe(Long id) {
        Topicinfo topicinfo = new Topicinfo();
        topicinfo.setId(id);
        topicinfo.setStatus(1);
        topicinfoMapper.updateByPrimaryKeySelective(topicinfo);
    }

    /*
    设置置顶、加精状态
     */
    public void setTopAndGood(Long id, Integer rank, String field) {
        Topicinfo topicinfo = topicinfoMapper.selectByPrimaryKey(id);
        if (field.equals("stick")) {
            topicinfo.setIsTop(rank);
        } else if (field.equals("status")) {
            topicinfo.setIsGood(rank);
        }
        topicinfoMapper.updateByPrimaryKeySelective(topicinfo);
    }

    /*
    删除帖子
     */
    public void delTopicAndComment(Long id) {
        //删除帖子
        topicinfoMapper.deleteByPrimaryKey(id);
        //删除相关评论
        Example example = new Example(Comment.class);
        example.createCriteria().andEqualTo("topicId", id);
        //List<Comment> commentList = commentMapper.selectByExample(example);
        commentMapper.deleteByExample(example);
        //删除相关收藏信息
        Example exampleCollect = new Example(Collect.class);
        exampleCollect.createCriteria().andEqualTo("topicId", id);
        collectMapper.deleteByExample(exampleCollect);
        //删除相关通知信息
        Example exampleMessage = new Example(Message.class);
        exampleMessage.createCriteria().andEqualTo("topicId", id);
        messageMapper.deleteByExample(exampleMessage);
    }

    /*
    点赞
     */
    public Result jiedaZan(User user, Long id, Boolean ok) {
        Long SUCEESS = 1L;
        if (ok == false)//点赞
        {
                Long status = redisUtil.setSet("c" + id.toString(), user.getAccountId().toString());
                if(SUCEESS.equals(status)){
                    redisUtil.setHashMapOneValueInc("agreeCount",id.toString(),+1);
                }
                return new Result().ok(MsgEnum.ZAN_SUCCESS);
        } else {  //取消点赞
               Long status =  redisUtil.delSet("c" + id.toString(), user.getAccountId().toString());
                if(SUCEESS.equals(status)){
                    redisUtil.setHashMapOneValueInc("agreeCount",id.toString(),-1);
                }
            return new Result().ok(MsgEnum.ZAN_CANCEL);
        }
    }
    /*
    采纳评论
     */

    public Result acceptComment(Long id){
        //更新评论状态
        Comment comment = new Comment();
        comment.setId(id);
        comment.setIsAccept(1);
        commentMapper.updateByPrimaryKeySelective(comment);
        //获得评论信息
        comment = commentMapper.selectByPrimaryKey(id);
        //设置帖子已结
        Topicinfo topicinfo = new Topicinfo();
        topicinfo.setId(comment.getTopicId());
        topicinfo.setIsEnd(1);
        topicinfoMapper.updateByPrimaryKeySelective(topicinfo);
        //获得帖子信息
        Topicinfo topicinfo1 = topicinfoMapper.selectByPrimaryKey(comment.getTopicId());
        //获得评论用户信息
        Example example = new Example(User.class);
        example.createCriteria().andEqualTo("accountId",comment.getUserId());
        List<User> userList = userMapper.selectByExample(example);
        //更新用户经验
        User user = new User();
        user.setKissNum(userList.get(0).getKissNum() + topicinfo1.getExperience());
        userMapper.updateByExampleSelective(user,example);
        return new Result().ok(MsgEnum.OK);
    }

    /*
    获得编辑评论内容
     */
    public String getCommentContentById(Long id){
        Comment comment = commentMapper.selectByPrimaryKey(id);
        return comment.getContent();
    }

    /*
    提交编辑后的新评论
     */
    public Result editCommentSub(Long id,String content){
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent(content);
        comment.setCommentModified(System.currentTimeMillis());
        commentMapper.updateByPrimaryKeySelective(comment);
        Map map = new HashMap();
        map.put("content",content);
        return new Result().ok(MsgEnum.UPDATE_COMMENT,map);
    }

    /*
    删除评论
     */
    public Result delComment(Long id) {
        commentMapper.deleteByPrimaryKey(id);
        //删除通知消息
        Example example = new Example(Message.class);
        example.createCriteria().andEqualTo("commentId",id);
        messageMapper.deleteByExample(example);
        return new Result().ok(MsgEnum.DELETE_SUCCESS);
    }
    /*
    修改帖子信息
     */
    public Topicinfo findTopicById(Long id){
        Topicinfo topicinfo = topicinfoMapper.selectByPrimaryKey(id);
        return topicinfo;
    }

    /*
    查询板块下的帖子
     */
    public List<TopicinfoExt> findCateTopic(Long categoryId,String status){
        if(categoryId == 0){
            if(status.equals("all")){
                return topicinfoExtMapper.getAllTopic();
            }else if(status.equals("isend")){
                return topicinfoExtMapper.getAllTopicIsEnd();
            }else if(status.equals("notend")){
                return topicinfoExtMapper.getAllTopicNotEnd();
            }else if(status.equals("isgood")){
                return topicinfoExtMapper.getAllTopicIsGood();
            }else{
                return topicinfoExtMapper.getAllTopic();
            }

        }else {
            if(status.equals("all")){
                return topicinfoExtMapper.getCateTopic(categoryId);
            }else if(status.equals("isend")){
                return topicinfoExtMapper.getCateTopicIsEnd(categoryId);
            }else if(status.equals("notend")){
                return topicinfoExtMapper.getCateTopicNotEnd(categoryId);
            }else if(status.equals("isgood")){
                return topicinfoExtMapper.getCateTopicIsGood(categoryId);
            }else{
                return topicinfoExtMapper.getCateTopic(categoryId);
            }

        }
    }

    /*
    查询二级分类的帖子
     */
    public List<TopicinfoExt> findKindTopic(Long categoryId,Long kindId,String status){
        if(categoryId == 0){
            if(status.equals("all")){
                return topicinfoExtMapper.getAllTopicByKindId(kindId);
            }else if(status.equals("isend")){
                return topicinfoExtMapper.getAllTopicByKindIdIsEnd(kindId);
            }else if(status.equals("notend")){
                return topicinfoExtMapper.getAllTopicByKindIdNotEnd(kindId);
            }else if(status.equals("isgood")){
                return topicinfoExtMapper.getAllTopicByKindIdIsGood(kindId);
            }else{
                return topicinfoExtMapper.getAllTopicByKindId(kindId);
            }

        }else {
            if(status.equals("all")){
                return topicinfoExtMapper.getKindTopic(categoryId,kindId);
            }else if(status.equals("isend")){
                return topicinfoExtMapper.getKindTopicIsEnd(categoryId,kindId);
            }else if(status.equals("notend")){
                return topicinfoExtMapper.getKindTopicNotEnd(categoryId,kindId);
            }else if(status.equals("isgood")){
                return topicinfoExtMapper.getKindTopicIsGood(categoryId,kindId);
            }else{
                return topicinfoExtMapper.getKindTopic(categoryId,kindId);
            }
        }

    }

    /*
    搜索
     */
    public List<TopicinfoExt> searchTopic(String q){
        List<TopicinfoExt> topicinfoExtList = topicinfoExtMapper.searchTopic(q);
       for(TopicinfoExt topicinfoExt:topicinfoExtList){
           topicinfoExt.setTime(StringDate.getStringDate(new Date(topicinfoExt.getTopicCreate())));
       }
        return topicinfoExtList;
    }

    /*
    查询未审帖子
     */
    public List<Topicinfo> findTopicStatus(){
        Example example = new Example(Topicinfo.class);
        example.setOrderByClause("topic_create desc");
        example.createCriteria().andEqualTo("status",0);
        return  topicinfoMapper.selectByExample(example);
    }
}
