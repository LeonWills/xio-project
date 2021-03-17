package com.cat;

import com.cat.pojo.WorkOrder;
import com.cat.service.ActionService;
import com.cat.service.MainService;
import com.cat.service.OrderService;
import com.cat.service.ParameterService;
import com.cat.utils.SignalUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BottomProcessTest extends BaseTest {
    @Autowired
    MainService mainService;
    @Autowired
    OrderService orderService;
    @Autowired
    ActionService actionService;
    @Autowired
    ParameterService parameterService;

    @Test
    void test1() {
        assertTrue(true);
        WorkOrder order = orderService.getOrderById(3099510);
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    @Test
    void test2() {
        assertTrue(true);
        WorkOrder order = orderService.getOrderById(3099510);
        order.setProductSpecification("2.5×121×2504");
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    @Test
    void test3() {
        assertTrue(true);
        WorkOrder order = orderService.getOrderById(3099510);
        order.setProductQuantity("8");
        order.setCuttingSize("2.5×1000×2504");
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
    }

    @Test
    void test4() {
        assertTrue(true);
        WorkOrder order = orderService.getOrderById(3099510);
        order.setProductQuantity("8");
        order.setProductSpecification("2.5×240×2185");
        mainService.processingBottomOrder(order, parameterService.getLatestOperatingParameter(), SignalUtil.getDefaultCuttingSignal(order));
        actionService.getAllMachineActions().forEach(System.out::println);
    }
}
