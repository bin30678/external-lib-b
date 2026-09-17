package com.external.libb.dao;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 外部 JAR B 的 DAO (純 DAO 架構，無 Service 層)
 * 專門操作 H2 資料庫之 customers 表。
 * 依據規範：所有方法皆必須傳入 Connection 與對應參數，連線生命週期由外部呼叫端統一管理，DAO 絕不主動關閉連線。
 */
@Repository("anotherLegacyDao")
public class AnotherLegacyDao {

    /**
     * 查詢 INACTIVE 狀態客戶數量 (相容既有主專案呼叫)
     */
    public int queryInactiveCustomerCount(Connection conn) throws Exception {
        return queryCustomerCountByStatus(conn, "INACTIVE");
    }

    /**
     * 依狀態查詢 H2 customers 表的客戶數量
     */
    public int queryCustomerCountByStatus(Connection conn, String status) throws Exception {
        if (conn == null) {
            throw new IllegalArgumentException("Connection 不能為 null");
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT COUNT(*) FROM customers WHERE status = ?");
            ps.setString(1, status);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * 依狀態查詢 H2 customers 表的客戶名稱清單
     */
    public List<String> queryCustomerNamesByStatus(Connection conn, String status) throws Exception {
        if (conn == null) {
            throw new IllegalArgumentException("Connection 不能為 null");
        }
        List<String> list = new ArrayList<String>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement("SELECT customer_name FROM customers WHERE status = ?");
            ps.setString(1, status);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("customer_name"));
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
        }
        return list;
    }

    /**
     * 新增客戶紀錄至 H2 customers 表
     */
    public int insertCustomer(Connection conn, String customerName, String status) throws Exception {
        if (conn == null) {
            throw new IllegalArgumentException("Connection 不能為 null");
        }
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("INSERT INTO customers (customer_name, status) VALUES (?, ?)");
            ps.setString(1, customerName);
            ps.setString(2, status != null ? status : "ACTIVE");
            return ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * 更新 H2 customers 表中指定客戶的狀態
     */
    public int updateCustomerStatus(Connection conn, String customerName, String newStatus) throws Exception {
        if (conn == null) {
            throw new IllegalArgumentException("Connection 不能為 null");
        }
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("UPDATE customers SET status = ? WHERE customer_name = ?");
            ps.setString(1, newStatus);
            ps.setString(2, customerName);
            return ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * 刪除 H2 customers 表中指定客戶
     */
    public int deleteCustomerByName(Connection conn, String customerName) throws Exception {
        if (conn == null) {
            throw new IllegalArgumentException("Connection 不能為 null");
        }
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("DELETE FROM customers WHERE customer_name = ?");
            ps.setString(1, customerName);
            return ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
        }
    }
}
