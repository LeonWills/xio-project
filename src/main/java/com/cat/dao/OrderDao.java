package com.cat.dao;

import com.cat.entity.bean.WorkOrder;
import com.cat.enums.OrderModule;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * @author CAT
 */
@Component
public class OrderDao extends BaseDao {
    RowMapper<WorkOrder> orderM = new BeanPropertyRowMapper<>(WorkOrder.class);

    /**
     * 更新工单运行状态
     *
     * @param order 工单
     */
    public void updateOrderState(WorkOrder order) {
        this.jdbcTemplate.update("UPDATE tb_local_work_order SET ZT = ? WHERE bid = ?", order.getOperationState(), order.getId());
    }

    /**
     * 更新工单已完工数量
     *
     * @param order 工单
     */
    public void updateOrderCompletedAmount(WorkOrder order) {
        // 由于工单原生表设计的不完善，该字段可能为空，需要对该字段进行判空处理，因为 update() 方法后续的参数数组不支持非空对象
        if (order.getCompletedAmount() == null) {
            this.jdbcTemplate.update("UPDATE tb_local_work_order SET YWGSL = null WHERE bid = ?", order.getId());
        } else {
            this.jdbcTemplate.update("UPDATE tb_local_work_order SET YWGSL = ? WHERE bid = ?", order.getCompletedAmount(), order.getId());
        }
    }

    /**
     * 根据工单 ID 获取工单
     *
     * @param id 工单 ID
     * @return 工单
     */
    public WorkOrder getOrderById(Integer id) {
        return this.jdbcTemplate.queryForObject("SELECT * FROM vi_local_work_order WHERE id = ?", this.orderM, id);
    }

    /**
     * 根据计划完工日期获取按顺序号升序的对重直梁工单集合
     *
     * @param date 计划完工日期
     * @return 对重直梁工单集合
     */
    public List<WorkOrder> getNotBottomOrders(LocalDate date) {
        return this.jdbcTemplate.query("SELECT * FROM vi_local_work_order " +
                "WHERE site_module != ? AND CAST(completion_date AS DATE) = ? " +
                "ORDER BY CAST(sequence_number AS INT), id", this.orderM, OrderModule.BOTTOM.value, date);
    }

    /**
     * 根据计划完工日期获取按顺序号升序的轿底工单集合
     *
     * @param date 计划完工日期
     * @return 轿底工单集合
     */
    public List<WorkOrder> getBottomOrders(LocalDate date) {
        return this.jdbcTemplate.query("SELECT * FROM vi_local_work_order " +
                "WHERE site_module = ? AND CAST(completion_date AS DATE) = ? " +
                "ORDER BY CAST(sequence_number AS INT), id", this.orderM, OrderModule.BOTTOM.value, date);
    }
}
