package com.itmd.user.service;

import com.itmd.pojo.User;
import com.itmd.pojo.UserAddress;

import java.util.List;

public interface UserService {


    Boolean checkUserInfo(String data, int type);

    void sendUserPhoneSms(String phone);

    Boolean register(User user, String code);

    User queryUser(String username, String password);

    User queryUserByPhone(String phone);

    void addAddress(UserAddress userAddress);

    List<UserAddress> queryUserAddress();

    void updateUserDefaultAddress(Long id);

    UserAddress queryAddressById(Long id);
}
