package com.lazy.sellergoods.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.lazy.entity.PageResult;
import com.lazy.entity.Result;
import com.lazy.pojo.TbBrand;
import com.lazy.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbBrand> findAll() {
        return brandService.findAll();
    }

    @RequestMapping("/findPage")
    public PageResult findPage(int page,int size){
        return brandService.findPage(page,size);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){

        try {
            brandService.add(brand);
            return new Result(true,"增加品牌成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"网络异常请重试");
        }
    }

    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try{
            brandService.update(brand);
            return new Result(true,"修改成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"网络异常请重试");
        }
    }
    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
       return brandService.findOne(id);
    }
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try{
            brandService.delete(ids);
            return new Result(true,"删除成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"网络异常，请重试");
        }
    }
    /**
     * 查询+分页
     * @param brand
     * @param page
     * @param size
     * @return
     */
    @RequestMapping("/search")
    @ResponseBody
    public PageResult search(@RequestBody TbBrand brand, int page, int size){
        return brandService.findPage(brand, page, size);
    }
    @RequestMapping("/selectOptionList")
    @ResponseBody
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }


}
