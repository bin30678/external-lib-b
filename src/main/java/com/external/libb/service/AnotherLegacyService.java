package com.external.libb.service;

import com.external.libb.dao.AnotherLegacyDao;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.sql.Connection;

/**
 * 外部 JAR B 的 Service
 * DAO 由 Spring 注入，Connection 由外部主專案傳入
 */
@Service("anotherLegacyService")
public class AnotherLegacyService {

    @Resource(name = "anotherLegacyDao")
    private AnotherLegacyDao anotherLegacyDao;

    public void setAnotherLegacyDao(AnotherLegacyDao anotherLegacyDao) {
        this.anotherLegacyDao = anotherLegacyDao;
    }

    public AnotherLegacyDao getAnotherLegacyDao() {
        return anotherLegacyDao;
    }

    public int getInactiveCustomerCount(Connection conn) throws Exception {
        System.out.println("=== [External JAR B] getInactiveCustomerCount called ===");
        return anotherLegacyDao.queryInactiveCustomerCount(conn);
    }

    public boolean disableCustomer(Connection conn, String customerName) throws Exception {
        System.out.println("=== [External JAR B] disableCustomer called: " + customerName + " ===");
        int rows = anotherLegacyDao.updateCustomerStatus(conn, customerName, "INACTIVE");
        return rows > 0;
    }
}