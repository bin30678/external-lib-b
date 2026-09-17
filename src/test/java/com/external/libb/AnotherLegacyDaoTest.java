package com.external.libb;

import com.external.libb.dao.AnotherLegacyDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * 外部 JAR B 的 Spring 整合測試 (JUnit 4 / Spring 3.2 相容)
 * 驗證 JAR B 僅有 DAO 層（無 Service 層），且所有資料庫方法皆必須傳入 Connection 與參數。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-libb.xml")
public class AnotherLegacyDaoTest {

    @Resource
    private ApplicationContext applicationContext;

    @Resource(name = "anotherLegacyDao")
    private AnotherLegacyDao dao;

    @Test
    public void testSpringContextLoaded_daoOnly() {
        Assert.assertNotNull("Spring ApplicationContext 必須成功載入", applicationContext);
        Assert.assertNotNull("Spring 必須透過 component-scan 成功建立並注入 AnotherLegacyDao", dao);
        Assert.assertTrue(applicationContext.containsBean("anotherLegacyDao"));
        // 驗證 JAR B 不包含任何 Service 層 Bean
        Assert.assertFalse("JAR B 不應包含 Service 層 Bean", applicationContext.containsBean("anotherLegacyService"));
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
        Assert.assertEquals(42, count);

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
        Assert.assertEquals(1, updated);

        Mockito.verify(mockPs, Mockito.times(1)).setString(1, "SUSPENDED");
        Mockito.verify(mockPs, Mockito.times(1)).setString(2, "Alice");
        Mockito.verify(mockPs, Mockito.times(1)).close();
        Mockito.verify(mockConn, Mockito.never()).close();
    }

    @Test
    public void testInsertCustomer() throws Exception {
        Connection mockConn = Mockito.mock(Connection.class);
        PreparedStatement mockPs = Mockito.mock(PreparedStatement.class);

        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockPs);
        Mockito.when(mockPs.executeUpdate()).thenReturn(1);

        int inserted = dao.insertCustomer(mockConn, "Bob", "ACTIVE");
        Assert.assertEquals(1, inserted);

        Mockito.verify(mockPs, Mockito.times(1)).setString(1, "Bob");
        Mockito.verify(mockPs, Mockito.times(1)).setString(2, "ACTIVE");
        Mockito.verify(mockPs, Mockito.times(1)).close();
        Mockito.verify(mockConn, Mockito.never()).close();
    }

    @Test
    public void testQueryCustomerNamesByStatus() throws Exception {
        Connection mockConn = Mockito.mock(Connection.class);
        PreparedStatement mockPs = Mockito.mock(PreparedStatement.class);
        ResultSet mockRs = Mockito.mock(ResultSet.class);

        Mockito.when(mockConn.prepareStatement(Mockito.anyString())).thenReturn(mockPs);
        Mockito.when(mockPs.executeQuery()).thenReturn(mockRs);
        Mockito.when(mockRs.next()).thenReturn(true, false);
        Mockito.when(mockRs.getString("customer_name")).thenReturn("Bob");

        List<String> list = dao.queryCustomerNamesByStatus(mockConn, "ACTIVE");
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("Bob", list.get(0));

        Mockito.verify(mockRs, Mockito.times(1)).close();
        Mockito.verify(mockPs, Mockito.times(1)).close();
        Mockito.verify(mockConn, Mockito.never()).close();
    }
}
