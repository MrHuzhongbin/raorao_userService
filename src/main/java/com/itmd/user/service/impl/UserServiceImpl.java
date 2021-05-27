package com.itmd.user.service.impl;

import com.itmd.auth.entiy.UserInfo;
import com.itmd.enums.ExceptionEnum;
import com.itmd.exception.RaoraoBookShopException;
import com.itmd.pojo.User;
import com.itmd.pojo.UserAddress;
import com.itmd.user.filter.LoginInterceptor;
import com.itmd.user.mapper.UserAddressMapper;
import com.itmd.user.mapper.UserMapper;
import com.itmd.user.service.UserService;
import com.itmd.user.utils.CodecUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private UserAddressMapper userAddressMapper;

    static final String KEY_PREFIX = "user:code:phone:";

    @Override
    public Boolean checkUserInfo(String data, int type) {
        User user=  new User();
        switch (type){
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            case 3:
                user.setEmail(data);
            default:
                throw new RaoraoBookShopException(ExceptionEnum.PATH_ERROR);
        }
        return userMapper.selectCount(user)==0;
    }

    @Override
    public void sendUserPhoneSms(String phone) {
        //生成验证码
        StringBuffer code = new StringBuffer();
        int random;
        for(int i = 1;i<=6;i++) {
             code.append(new Random().nextInt(9));
        }
        Map<String,String> map=new HashMap<>();
        map.put("phone", "+86"+phone);
        map.put("code", code.toString());

        try {
            //发送验证码
            amqpTemplate.convertAndSend("raorao.book.exchange","sms.verity.code",  map);
            //保存验证码到redis
            redisTemplate.opsForValue().set(KEY_PREFIX+phone, code.toString(),5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.info("短信发送失败"+phone);
        }
    }

    @Override
    @Transactional
    public Boolean register(User user, String code) {
        // 校验验证码
        String key = KEY_PREFIX+user.getPhone();
        String codeRedis = redisTemplate.opsForValue().get(key);
        if(!code.equals(codeRedis)){
           return false;
        }
        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //加密密码
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));
        user.setCreateTime(new Date());
        user.setImage("https://upload.hytree.link/group1/M00/00/01/rBE112CBepSAXFxCAAASBve_fOQ893.png");
        //存储到数据库
        int status = userMapper.insert(user);
        if(status != 1){
            throw new RaoraoBookShopException(ExceptionEnum.USER_SAVE_ERROR);
        }
        return true;
    }

    @Override
    public User queryUser(String username, String password) {
        Example example = new Example(User.class);
        example.createCriteria().orEqualTo("username", username)
                .orEqualTo("phone", username)
                .orEqualTo("email", username);
        User u = userMapper.selectOneByExample(example);
        if(u == null ){
            throw new RaoraoBookShopException(ExceptionEnum.USER_NOT_FOUND_ERROR);
        }
        if(!u.getPassword().equals(CodecUtils.md5Hex(password, u.getSalt()))){
            throw new RaoraoBookShopException(ExceptionEnum.USER_NOT_FOUND_ERROR);
        }
        return u;
    }

    @Override
    public User queryUserByPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        User u = userMapper.selectOne(user);
        if(u == null){
            throw new RaoraoBookShopException(ExceptionEnum.USER_NOT_FOUND_ERROR);
        }
        return u;
    }

    @Override
    @Transactional
    public void addAddress(UserAddress userAddress) {
        UserInfo user = LoginInterceptor.getUser();
        userAddress.setUserId(user.getId());
        if(userAddress.getIsDefault()){
            //查询是否存在默认地址
            UserAddress address = new UserAddress();
            address.setUserId(user.getId());
            address.setIsDefault(true);

            UserAddress result = userAddressMapper.selectOne(address);
            if(result != null){
                result.setIsDefault(false);
                userAddressMapper.updateByPrimaryKey(result);
            }
        }
        int pos = userAddressMapper.insert(userAddress);
        if(pos != 1){
            throw new RaoraoBookShopException(ExceptionEnum.ADDRESS_SAVE_FOUND);
        }
    }

    @Override
    public List<UserAddress> queryUserAddress() {
        UserAddress userAddress = new UserAddress();
        UserInfo user = LoginInterceptor.getUser();
        userAddress.setUserId(user.getId());
        List<UserAddress> result = userAddressMapper.select(userAddress);
        if(CollectionUtils.isEmpty(result)){
            throw new RaoraoBookShopException(ExceptionEnum.ADDRESS_NOT_FOUND);
        }
        return result;
    }

    @Override
    @Transactional
    public void updateUserDefaultAddress(Long id) {
        UserAddress userAddress = new UserAddress();
        UserInfo user = LoginInterceptor.getUser();
        userAddress.setUserId(user.getId());
        userAddress.setIsDefault(true);
        //查询是否存在默认地址
        UserAddress address = userAddressMapper.selectOne(userAddress);
        if(address != null){
            address.setIsDefault(false);
            userAddressMapper.updateByPrimaryKey(address);
        }
        //修改地址为默认地址
        userAddress.setId(id);
        int pos = userAddressMapper.updateByPrimaryKeySelective(userAddress);
        if(pos != 1){
            throw new RaoraoBookShopException(ExceptionEnum.ADDRESS_UPDATE_FOUND);
        }
    }

    @Override
    public UserAddress queryAddressById(Long id) {
        UserAddress userAddress = userAddressMapper.selectByPrimaryKey(id);
        if(userAddress == null){
            throw new RaoraoBookShopException(ExceptionEnum.ADDRESS_NOT_FOUND);
        }
        return userAddress;
    }
}
