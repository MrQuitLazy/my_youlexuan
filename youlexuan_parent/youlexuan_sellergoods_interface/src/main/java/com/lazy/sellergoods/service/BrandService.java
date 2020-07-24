package com.lazy.sellergoods.service;

import com.lazy.entity.PageResult;
import com.lazy.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    public List<TbBrand> findAll();
    public PageResult findPage(int pageNum,int pageSize);
    public void add(TbBrand brand);
    public void update(TbBrand brand);
    TbBrand findOne(Long id);
    public void delete(Long[] ids);

    //查询的分页
    public PageResult findPage(TbBrand brand,int pageNum,int pageSize);
    /**
     * 品牌下拉框数据
     */
    List<Map> selectOptionList();

}
