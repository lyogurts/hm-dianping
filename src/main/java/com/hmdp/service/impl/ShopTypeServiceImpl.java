package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public Result queryTypeList() {
       //1.去redis中查询商户类型
      String key= RedisConstants.CACHE_SHOP_TYPE_KEY;
        String typeShopJson = redisTemplate.opsForValue().get(key);
        //2.存在，则返回
        if (StringUtils.hasText(typeShopJson)){
            List<ShopType> shopType = JSONUtil.toList(typeShopJson, ShopType.class);
            return Result.ok(shopType);
        }
        //3.不存在，查询数据
        LambdaQueryWrapper<ShopType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ShopType::getSort);
        List<ShopType> list = list(queryWrapper);
        //4.sql不存在，返回错误
        if (list==null){
            return Result.fail("类型不存在");
        }
        //5.存在，存入redis 返回
        redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(list));
        return Result.ok(list);
    }
}
