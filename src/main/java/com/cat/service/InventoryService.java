package com.cat.service;

import com.cat.dao.InventoryDao;
import com.cat.entity.bean.Inventory;
import com.cat.utils.BoardUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author CAT
 */
@Component
public class InventoryService {
    @Autowired
    InventoryDao inventoryDao;

    /**
     * 根据存货规格、材质、类型获取唯一的存货记录，不存在则返回 null
     *
     * @param specification 规格
     * @param material      材质
     * @param category      类型
     * @return 存货
     */
    public Inventory getInventory(String specification, String material, String category) {
        return this.inventoryDao.getInventories(material, category)
                .stream()
                .filter(inventory -> BoardUtils.compareTwoSpecStr(inventory.getSpecStr(), specification) == 0)
                .findFirst()
                .orElse(null);
    }

    /**
     * 更新存货数量，如果不存在相应存货，则新增该存货记录
     *
     * @param inventory 存货
     */
    public void updateInventoryAmount(Inventory inventory) {
        Inventory existedInventory = this.getInventory(inventory.getSpecStr(), inventory.getMaterial(), inventory.getCategory());
        if (existedInventory != null) {
            existedInventory.setAmount(existedInventory.getAmount() + inventory.getAmount());
            this.inventoryDao.updateInventoryAmount(existedInventory);
        } else {
            this.insertInventory(inventory.getSpecStr(), inventory.getMaterial(), inventory.getAmount(), inventory.getCategory());
        }
    }

    /**
     * 新增存货记录
     *
     * @param specification 规格
     * @param material      材质
     * @param amount        数量
     * @param category      类型
     */
    public void insertInventory(String specification, String material, Integer amount, String category) {
        this.inventoryDao.insertInventory(specification, material, amount, category);
    }

    /**
     * 获取所有的存货记录数量，包括数量为零的记录
     *
     * @return 数量
     */
    public Integer getInventoryCount() {
        return this.inventoryDao.getInventoryCount();
    }
}
