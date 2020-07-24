package com.lazy.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lazy.entity.PageResult;
import com.lazy.group.Goods;
import com.lazy.mapper.*;
import com.lazy.pojo.*;
import com.lazy.pojo.TbGoodsExample.Criteria;
import com.lazy.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * goods服务实现层
 *
 * @author lazy
 */
@Service(timeout = 3000)
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbSellerMapper sellerMapper;

    @Override
    public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdIn(Arrays.asList(goodsIds));
        criteria.andStatusEqualTo(status);
        return itemMapper.selectByExample(example);
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        System.out.println("add------添加方法执行:-----------");
        goods.getGoods().setAuditStatus("0");
        goodsMapper.insert(goods.getGoods());    //插入商品表
        System.out.println("goodsMapper------添加方法执行:-----------");
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展数据
        insertItem(goods);
    }

    private void insertItem(Goods goods) {
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            for (TbItem item : goods.getItemList()) {
                //标题
                String title = goods.getGoods().getGoodsName();
                Map<String, Object> specMap = JSON.parseObject(item.getSpec());
                for (String key : specMap.keySet()) {
                    title += " " + specMap.get(key);
                }
                item.setTitle(title);
                setItemValus(goods, item);

                System.out.println("itemMapper------添加方法执行:-----------");
                itemMapper.insert(item);
            }
        } else {
            TbItem item = new TbItem();
            item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
            item.setPrice(goods.getGoods().getPrice());//价格
            item.setStatus("1");//状态
            item.setIsDefault("1");//是否默认
            item.setNum(9999);//库存数量
            item.setSpec("{}");
            setItemValus(goods, item);
            itemMapper.insert(item);
        }
    }

    //抽取一个公用的方法
    private void setItemValus(Goods goods, TbItem item) {
        item.setGoodsId(goods.getGoods().getId());//商品SPU编号
        item.setSellerId(goods.getGoods().getSellerId());//商家编号
        item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号（3级）
        item.setCreateTime(new Date());//创建日期
        item.setUpdateTime(new Date());//修改日期

        //品牌名称
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());
        //分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());

        //商家名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getNickName());

        //图片地址（取spu的第一个图片）
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList.size() > 0) {
            item.setImage((String) imageList.get(0).get("url"));
        }
    }


    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

//    /**
//     * 分页
//     */
//    @Override
//    public PageResult findPage(int pageNum, int pageSize) {
//        PageHelper.startPage(pageNum, pageSize);
//        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
//        return new PageResult(page.getTotal(), page.getResult());
//    }


    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        if(!"2".equals(goods.getGoods().getAuditStatus())){
            goods.getGoods().setAuditStatus("1");
        }
        goods.getGoods().setAuditStatus("0");
        goodsMapper.updateByPrimaryKey(goods.getGoods());
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
        //查询商品SKU数据信息 库存信息
        TbItemExample ex = new TbItemExample();
        TbItemExample.Criteria criteria = ex.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        itemMapper.deleteByExample(ex);
        insertItem(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        //查询商品信息
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        //查询商品描述信息
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(tbGoodsDesc);

        //查询商品SKU数据信息 库存信息
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);//查询条件：商品ID
        List<TbItem> itemList = itemMapper.selectByExample(example);
        goods.setItemList(itemList);

        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setIsDelete("1");
            goodsMapper.updateByPrimaryKey(goods);
        }//修改审核通过的商品的sku状态为删除
		 List<TbItem> listitem = findItemListByGoodsIdandStatus(ids, "1");
        for (TbItem tbItem : listitem) {
            tbItem.setStatus("3");
            itemMapper.updateByPrimaryKey(tbItem);
        }

    }

    /**
     * 分页+查询
     */
    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        System.out.println("goodsService>>>findPage");
        PageHelper.startPage(pageNum, pageSize);
        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();

        if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
            criteria.andSellerIdEqualTo(goods.getSellerId());
        }
        if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
            criteria.andAuditStatusEqualTo(goods.getAuditStatus());
        }
        if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
            criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
        }
        criteria.andIsDeleteIsNull();//非删除状态
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(goods);
        }
    }

}
