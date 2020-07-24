package com.lazy.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.lazy.mapper.TbGoodsDescMapper;
import com.lazy.mapper.TbGoodsMapper;
import com.lazy.mapper.TbItemCatMapper;
import com.lazy.mapper.TbItemMapper;
import com.lazy.page.servcie.ItemPageService;
import com.lazy.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {
        Writer out = null;
        try {
            System.out.println("生成页面中");
            //获取工具类
            Configuration configuration = freeMarkerConfig.getConfiguration();
            //获取模板
            Template template = configuration.getTemplate("item.ftl");
            Map dataModel = new HashMap<>();
            // 1.加载商品表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);

            // 2.加载商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);
            //3.获取1,2,3级分类
            String category1 =   itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String category2 =   itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String category3  =   itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1", category1);
            dataModel.put("itemCat2", category2);
            dataModel.put("itemCat3", category3);

            //4.SKU列表
            TbItemExample example=new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");//状态为上架
            criteria.andGoodsIdEqualTo(goodsId);//指定SPU ID
            example.setOrderByClause("is_default desc");//按照状态降序，保证第一个为默认
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList", itemList);


            out = new FileWriter(pagedir + goodsId + ".html");
            template.process(dataModel, out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if(out != null){
                    System.out.println("流关闭");
                    out.close();
                }
            } catch (IOException ex) {
                    ex.printStackTrace();
                return false;
            }
            return false;
        }finally {
            try {
                if(out != null){
                    System.out.println("流关闭");
                    out.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try{
            for (Long goodsId : goodsIds) {
                System.out.println("删除页面: "+goodsId);
                File f = new File(pagedir + goodsId + ".html");
                if (f.exists()){
                    System.out.println("页面村存在，删除");
                    f.delete();
                }
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}