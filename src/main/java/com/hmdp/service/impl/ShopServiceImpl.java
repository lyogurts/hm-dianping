package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
        if (StringUtils.hasText(shopJson)){
            //json转换为对象，然后返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        //3.不存在，根据id查询数据库
        Shop shop = getById(id);
        //4.不存在，返回错误
        if (shop==null){
            return Result.fail("店铺不存在");
        }
        //5.存在，写入redis
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop));
        //6.返回
        return Result.ok(shop);
    }
}
