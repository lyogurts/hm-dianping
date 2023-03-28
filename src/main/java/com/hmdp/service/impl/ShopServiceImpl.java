package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public Result queryShopById(Long id) {
        //1.从redis查询缓存
        String key = "cache:shop"+id;
        //2.存在，返回
        String shopJson = redisTemplate.opsForValue().get(key);
        //有值的情况
        if (StringUtils.hasText(shopJson)){
            //json转换为对象，然后返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
 //String不存在的情况有两种，一种是null，一种是""
        //上面判断部位空的条件了，String不存在的情况有两种，一种是null，一种是""
        //当命中缓存为““，是空缓存返回  Result.fail("店铺不存在");
        //当为null是去下面设置”“空缓存
        if (shopJson!=null){
            return Result.fail("店铺不存在");
        }
        //3.不存在，根据id查询数据库
        Shop shop = getById(id);
        //4.不存在，返回错误
        if (shop==null){
            redisTemplate.opsForValue().set(key,"",RedisConstants.CACHE_NULL_TTL,TimeUnit.MINUTES);
            return Result.fail("店铺不存在");
        }
        //5.存在，写入redis
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //6.返回
        return Result.ok(shop);
    }
    @Transactional
    @Override
    public Result updateShop(Shop shop) {
        if (shop.getId()==null){
            return Result.fail("id不能为空");
        }
        //更新数据库
        updateById(shop);
        //删除缓存
        Long id = shop.getId();
        String key = "cache:shop"+id;
        redisTemplate.delete(key);
        return Result.ok();
    }
}
