package io.authid.core.containers.user.services;

import io.authid.core.containers.user.contracts.UserCommonService;
import io.authid.core.containers.user.entities.UserEntity;
import io.authid.core.shared.rest.services.RestServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Qualifier("userCommonServiceImpl")
public class UserCommonServiceImpl extends RestServiceImpl<UserEntity, UUID, Object, Object> implements UserCommonService {
    public void hello(){
        System.out.println("hello");
    }
    public void sayHello(){
        System.out.println("hello");
    }
}
