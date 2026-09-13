package com.external.libb;

import com.external.libb.dao.AnotherLegacyDao;
import com.external.libb.service.AnotherLegacyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import jakarta.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 外部 JAR B 的 Spring 整合測試
 * 透過 Spring 讀取 applicationContext-libb.xml，將所有 Bean 註冊並委派給 Spring 容器管理與注入
 */
@SpringJUnitConfig(locations = "classpath:applicationContext-libb.xml")
public class AnotherLegacyDaoTest {

    @Resource
    private ApplicationContext applicationContext;

    @Resource(name = "anotherLegacyDao")
    private AnotherLegacyDao dao;

    @Resource(name = "anotherLegacyService")
    private AnotherLegacyService service;

    @Test
    public void testSpringContextLoaded() {
        Assertions.assertNotNull(applicationContext, "Spring ApplicationContext 必須成功載入");
        Assertions.assertNotNull(dao, "Spring 必須透過 component-scan 成功建立並注入 AnotherLegacyDao");
        Assertions.assertNotNull(service, "Spring 必須透過 component-scan 成功建立並注入 AnotherLegacyService");
        Assertions.assertTrue(applicationContext.containsBean("anotherLegacyDao"));
        Assertions.assertTrue(applicationContext.containsBean("anotherLegacyService"));
        // 驗證 Service 內部的 DAO 是由 Spring 注入的
        Assertions.assertNotNull(service.getAnotherLegacyDao(), "Service 中的 DAO 必須由 Spring 注入");
        Assertions.assertSame(dao, service.getAnotherLegacyDao());
    }

    @Test
    public void testQueryInactiveCustomerCount() throws Exception {
        Connection mockConn = Mockito.mock(Connection.class);
        PreparedStatement mockPs = Mockito.mock(PreparedStatement.class);
        ResultSet mockRs = Mockito.mock(ResultSet.class);

        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockPs);
        Mockito.when(mockPs.executeQuery()).thenReturn(mockRs);
        Mockito.when(mockRs.next()).thenReturn(true);
        Mockito.when(mockRs.getInt(1)).thenReturn(42);

        int count = dao.queryInactiveCustomerCount(mockConn);
        Assertions.assertEquals(42, count);

        // 驗證 PreparedStatement 和 ResultSet 有被關閉，但 Connection 不能被關閉
        Mockito.verify(mockRs, Mockito.times(1)).close();
        Mockito.verify(mockPs, Mockito.times(1)).close();
        Mockito.verify(mockConn, Mockito.never()).close();
    }

    @Test
    public void testUpdateCustomerStatus() throws Exception {
        Connection mockConn = Mockito.mock(Connection.class);
        PreparedStatement mockPs = Mockito.mock(PreparedStatement.class);

        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockPs);
        Mockito.when(mockPs.executeUpdate()).thenReturn(1);

        int updated = dao.updateCustomerStatus(mockConn, "Alice", "SUSPENDED");
        Assertions.assertEquals(1, updated);

        Mockito.verify(mockPs, Mockito.times(1)).setString(1, "SUSPENDED");
        Mockito.verify(mockPs, Mockito.times(1)).setString(2, "Alice");
        Mockito.verify(mockPs, Mockito.times(1)).close();
        Mockito.verify(mockConn, Mockito.never()).close();
    }

    @Test
    public void testServiceIntegrationWithDao() throws Exception {
        // 驗證 Spring 管理的 Service 呼叫注入的 DAO
        Connection mockConn = Mockito.mock(Connection.class);
        PreparedStatement mockPs = Mockito.mock(PreparedStatement.class);
        ResultSet mockRs = Mockito.mock(ResultSet.class);

        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockPs);
        Mockito.when(mockPs.executeQuery()).thenReturn(mockRs);
        Mockito.when(mockRs.next()).thenReturn(true);
        Mockito.when(mockRs.getInt(1)).thenReturn(10);

        int count = service.getInactiveCustomerCount(mockConn);
        Assertions.assertEquals(10, count);

        Mockito.verify(mockRs, Mockito.times(1)).close();
        Mockito.verify(mockPs, Mockito.times(1)).close();
        Mockito.verify(mockConn, Mockito.never()).close();
    }
}
