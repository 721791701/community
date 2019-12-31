package com.bbgu.zmz.community.mapper;

import com.bbgu.zmz.community.model.Collect;
import com.bbgu.zmz.community.model.CollectExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface CollectMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table collect
     *
     * @mbg.generated Sun Dec 29 23:32:05 GMT+08:00 2019
     */
    long countByExample(CollectExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table collect
     *
     * @mbg.generated Sun Dec 29 23:32:05 GMT+08:00 2019
     */
    int deleteByExample(CollectExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table collect
     *
     * @mbg.generated Sun Dec 29 23:32:05 GMT+08:00 2019
     */
    int deleteByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table collect
     *
     * @mbg.generated Sun Dec 29 23:32:05 GMT+08:00 2019
     */
    int insert(Collect record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table collect
     *
     * @mbg.generated Sun Dec 29 23:32:05 GMT+08:00 2019
     */
    int insertSelective(Collect record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table collect
     *
     * @mbg.generated Sun Dec 29 23:32:05 GMT+08:00 2019
     */
    List<Collect> selectByExampleWithRowbounds(CollectExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table collect
     *
     * @mbg.generated Sun Dec 29 23:32:05 GMT+08:00 2019
     */
    List<Collect> selectByExample(CollectExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table collect
     *
     * @mbg.generated Sun Dec 29 23:32:05 GMT+08:00 2019
     */
    Collect selectByPrimaryKey(Long id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table collect
     *
     * @mbg.generated Sun Dec 29 23:32:05 GMT+08:00 2019
     */
    int updateByExampleSelective(@Param("record") Collect record, @Param("example") CollectExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table collect
     *
     * @mbg.generated Sun Dec 29 23:32:05 GMT+08:00 2019
     */
    int updateByExample(@Param("record") Collect record, @Param("example") CollectExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table collect
     *
     * @mbg.generated Sun Dec 29 23:32:05 GMT+08:00 2019
     */
    int updateByPrimaryKeySelective(Collect record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table collect
     *
     * @mbg.generated Sun Dec 29 23:32:05 GMT+08:00 2019
     */
    int updateByPrimaryKey(Collect record);
}