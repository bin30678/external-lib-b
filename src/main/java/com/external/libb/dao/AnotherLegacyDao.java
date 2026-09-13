package com.external.libb.dao;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 外部 JAR B 的 DAO
 * 所有方法都是傳 Connection 進來，自己不拿也不關
 */
@Repository("anotherLegacyDao")
public class AnotherLegacyDao {

    public int queryInactiveCustomerCount(Connection conn) throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT COUNT(*) FROM customers WHERE status = 'INACTIVE'");
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            if (ps != null) try { ps.close(); } catch (Exception e) {}
        }
    }

    public int updateCustomerStatus(Connection conn, String customerName, String newStatus) throws Exception {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("UPDATE customers SET status = ? WHERE customer_name = ?");
            ps.setString(1, newStatus);
            ps.setString(2, customerName);
            return ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (Exception e) {}
        }
    }
}
