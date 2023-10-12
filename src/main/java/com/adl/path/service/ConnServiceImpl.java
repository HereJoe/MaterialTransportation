package com.adl.path.service;

import com.adl.path.bean.Connection;
import com.adl.path.dao.ConnDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ConnServiceImpl implements ConnService {
    @Resource
    private ConnDao connDao;
    @Override
    public Connection getConn(int id) {
        return connDao.selectConn(id);
    }
}
