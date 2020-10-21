package com.cat.entity;

import com.cat.entity.enums.BoardCategory;
import com.cat.util.BoardUtil;

import java.math.BigDecimal;
import java.util.List;

public class Board implements Comparable<Board> {
    private BigDecimal height;
    private BigDecimal width;
    private BigDecimal length;
    private String material;
    private BoardCategory category;

    public String getSpecification() {
        return BoardUtil.getStandardSpecStr(this.height, this.width, this.length);
    }

    public Board() {
    }

    public Board(BigDecimal height, BigDecimal width, BigDecimal length, String material, BoardCategory category) {
        this.height = height;
        this.width = width;
        this.length = length;
        this.material = material;
        this.category = category;
    }

    public Board(String specification, String material, BoardCategory category) {
        List<BigDecimal> list = BoardUtil.specStrToDecList(specification);
        this.height = list.get(0);
        this.width = list.get(1);
        this.length = list.get(2);
        this.material = material;
        this.category = category;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getLength() {
        return length;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public BoardCategory getCategory() {
        return category;
    }

    public void setCategory(BoardCategory category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Board{" +
                "height=" + height +
                ", width=" + width +
                ", length=" + length +
                ", material='" + material + '\'' +
                ", category=" + category +
                '}';
    }

    @Override
    public int compareTo(Board other) {
        if (other == null) {
            return -1;
        }
        if (this.material.equals(other.material) && this.height.compareTo(other.height) == 0) {
            // 在材质相同和厚度相等的前提下，去比较宽度和长度:
            if (this.width.compareTo(other.width) < 0 || this.length.compareTo(other.length) < 0) {
                return -1;
            } else if (this.width.compareTo(other.width) == 0 && this.length.compareTo(other.length) == 0) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return -1;
        }
    }
}
