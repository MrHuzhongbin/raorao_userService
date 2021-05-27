package com.itmd.user.web;

import com.itmd.pojo.User;
import com.itmd.pojo.UserAddress;
import com.itmd.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户名，手机号，邮箱校验
     * @param data
     * @param type
     * @return
     */
    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserInfo(@PathVariable("data")String data,@PathVariable("type")int type){
        return ResponseEntity.ok(userService.checkUserInfo(data,type));
    }

    /**
     * 发送验证码
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendUserPhoneSms(@RequestParam("phone")String phone){
        userService.sendUserPhoneSms(phone);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void>register(@Valid User user, @RequestParam("code") String code){
        Boolean register = userService.register(user, code);
        if(register == null || !register){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("one")
    public ResponseEntity<User>queryUser(@RequestParam("username")String username,@RequestParam("password")String password){
        return ResponseEntity.ok(userService.queryUser(username,password));
    }

    @GetMapping("one/phone")
    public ResponseEntity<User>queryUserByPhone(@RequestParam("phone")String phone){
        return ResponseEntity.ok(userService.queryUserByPhone(phone));
    }


     /**
     * 添加用户收货地址
     * @param userAddress
     * @return
     */
    @PostMapping("userCenter/one")
    public ResponseEntity<Void> addAddress(UserAddress userAddress) {

        userService.addAddress(userAddress);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询用户收获地址
     * @return
     */
    @GetMapping("userCenter/list")
    public ResponseEntity<List<UserAddress>> queryUserAddress(){
        return ResponseEntity.ok(userService.queryUserAddress());
    }

    /**
     * 查询用户地址，不拦截，供订单服务调用
     * @param id
     * @return
     */
    @GetMapping("addr/id")
    public ResponseEntity<UserAddress>queryAddressById(@RequestParam("id")Long id){
        return ResponseEntity.ok(userService.queryAddressById(id));
    }
    /**
     * 修改地址为默认地址
     * @param id
     * @return
     */
    @PutMapping("userCenter/default")
    public ResponseEntity<Void> updateUserDefaultAddress(@RequestParam("id") Long id){
        userService.updateUserDefaultAddress(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
