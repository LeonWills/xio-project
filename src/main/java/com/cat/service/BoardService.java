package com.cat.service;

import com.cat.entity.Board;
import com.cat.entity.BoardList;
import com.cat.entity.bean.MachineAction;
import com.cat.entity.bean.WorkOrder;
import com.cat.entity.board.CutBoard;
import com.cat.entity.board.NormalBoard;
import com.cat.entity.param.StockSpecification;
import com.cat.enums.ActionCategory;
import com.cat.enums.BoardCategory;
import com.cat.enums.ForwardEdge;
import com.cat.mapper.ActionMapper;
import com.cat.utils.Arith;
import com.cat.utils.BoardUtils;
import com.cat.utils.ParamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author CAT
 */
@Service
public class BoardService {
    @Autowired
    ActionMapper actionMapper;

    /**
     * 板材裁剪函数，负责下料板材的实际旋转和裁剪操作。
     *
     * @param cutBoard    下料板
     * @param forwardEdge 朝向
     * @param targetBoard 目标板材
     * @param orderId     工单 ID
     */
    public void cuttingBoard(CutBoard cutBoard, ForwardEdge forwardEdge, NormalBoard targetBoard, Integer orderId) {
        if (targetBoard.getCutTimes() > 0) {
            if (cutBoard.getForwardEdge() != forwardEdge) {
                cutBoard.setForwardEdge(forwardEdge);
                this.actionMapper.insertMachineAction(MachineAction.of(ActionCategory.ROTATE, BigDecimal.ZERO, cutBoard, orderId));
            }

            BigDecimal dis = targetBoard.getWidth();
            if (cutBoard.getForwardEdge() == ForwardEdge.LONG) {
                cutBoard.setWidth(Arith.sub(cutBoard.getWidth(), dis));
            } else {
                cutBoard.setLength(Arith.sub(cutBoard.getLength(), dis));
            }

            if (cutBoard.getWidth().compareTo(BigDecimal.ZERO) > 0) {
                this.actionMapper.insertMachineAction(MachineAction.of(ActionCategory.CUT, dis, targetBoard, orderId));
            } else {
                this.actionMapper.insertMachineAction(MachineAction.of(ActionCategory.SEND, BigDecimal.ZERO, targetBoard, orderId));
            }
        }
    }

    /**
     * 板材裁剪流程，负责定义下料板整个裁剪流程。
     *
     * @param cutBoard       下料板
     * @param boardList      目标板材列表
     * @param wasteThreshold 废料阈值
     * @param currOrderId    当前工单 ID
     */
    public void newCutting(CutBoard cutBoard, BoardList boardList, BigDecimal wasteThreshold, Integer currOrderId) {
        List<Board> boards = boardList.getBoards();
        NormalBoard lastNormalBoard = boards.get(boards.size() - 1).getNormalBoard();
        if (BoardUtils.isAllowBackToFront(lastNormalBoard.getNormalBoardAllWidth(), lastNormalBoard.getWidth())) {
            NormalBoard extraBoard = this.getExtraBoard(cutBoard, ForwardEdge.LONG, boardList.getBoardAllWidth(), wasteThreshold);
            this.cuttingBoard(cutBoard, ForwardEdge.LONG, extraBoard, currOrderId);
            for (Board board : boards) {
                Integer orderId = board.getOrderId();
                NormalBoard normalBoard = board.getNormalBoard();
                if (normalBoard.getCutTimes() > 0) {
                    extraBoard = this.getExtraBoard(cutBoard, ForwardEdge.SHORT, normalBoard.getLength(), wasteThreshold);
                    this.cuttingBoard(cutBoard, ForwardEdge.SHORT, extraBoard, orderId);
                }
                for (int i = 0; i < normalBoard.getCutTimes(); i++) {
                    this.cuttingBoard(cutBoard, ForwardEdge.LONG, normalBoard, orderId);
                }
            }
        } else {
            this.newestCutting(cutBoard, boardList, wasteThreshold, currOrderId);
        }
    }

    /**
     * 板材裁剪流程，负责定义下料板整个裁剪流程。
     *
     * @param cutBoard       下料板
     * @param boardList      目标板材列表
     * @param wasteThreshold 废料阈值
     * @param currOrderId    当前工单 ID
     */
    public void newestCutting(CutBoard cutBoard, BoardList boardList, BigDecimal wasteThreshold, Integer currOrderId) {
        for (Board board : boardList.getBoards()) {
            Integer orderId = board.getOrderId();
            NormalBoard normalBoard = board.getNormalBoard();
            if (normalBoard.getCutTimes() > 0) {
                NormalBoard extraBoard = this.getExtraBoard(cutBoard, ForwardEdge.SHORT, normalBoard.getLength(), wasteThreshold);
                this.cuttingBoard(cutBoard, ForwardEdge.SHORT, extraBoard, orderId);
            }
            for (int i = 0; i < normalBoard.getCutTimes(); i++) {
                BigDecimal remainingWidth = Arith.sub(cutBoard.getWidth(), normalBoard.getWidth());
                if (remainingWidth.compareTo(BoardUtils.CLAMP_DEPTH) <= 0) {
                    NormalBoard extraBoard = this.getExtraBoard(cutBoard, ForwardEdge.LONG, normalBoard.getWidth(), wasteThreshold);
                    this.cuttingBoard(cutBoard, ForwardEdge.LONG, extraBoard, orderId);
                }
                this.cuttingBoard(cutBoard, ForwardEdge.LONG, normalBoard, orderId);
            }
        }
        if (cutBoard.getWidth().compareTo(BigDecimal.ZERO) > 0) {
            NormalBoard extraBoard = this.getExtraBoard(cutBoard, ForwardEdge.LONG, BigDecimal.ZERO, wasteThreshold);
            this.cuttingBoard(cutBoard, ForwardEdge.LONG, extraBoard, currOrderId);
        }
    }

    /**
     * 获取下料板。
     *
     * @param cuttingSize 规格
     * @param material    材质
     * @param forwardEdge 朝向
     * @return 下料板
     */
    public CutBoard getCutBoard(String cuttingSize, String material, Integer forwardEdge) {
        return new CutBoard(cuttingSize, material, forwardEdge == 1 ? ForwardEdge.LONG : ForwardEdge.SHORT);
    }

    /**
     * 获取成品板。
     *
     * @param specification           规格
     * @param material                材质
     * @param cutBoardWidth           下料板宽度
     * @param orderIncompleteQuantity 工单未完成数目
     * @return 成品板
     */
    public NormalBoard getStandardProduct(String specification, String material, BigDecimal cutBoardWidth, Integer orderIncompleteQuantity) {
        NormalBoard product = new NormalBoard(specification, material, BoardCategory.PRODUCT);
        if (product.getWidth().compareTo(cutBoardWidth) > 0) {
            // 如果成品板宽度大于下料板宽度，则需要交换成品板的宽度和长度，不然会导致后续裁剪逻辑出错:
            BigDecimal tmp = product.getWidth();
            product.setWidth(product.getLength());
            product.setLength(tmp);
        }
        // 成品板的裁剪次数取决于最大裁剪次数以及工单未完成数目中的最小值:
        product.setCutTimes(Math.min(Arith.div(BoardUtils.getAvailableWidth(cutBoardWidth, product.getWidth()), product.getWidth()), orderIncompleteQuantity));
        return product;
    }

    /**
     * 获取半成品。
     *
     * @param cutBoard   下料板
     * @param fixedWidth 固定宽度
     * @param product    成品板
     * @return 半成品
     */
    public NormalBoard getSemiProduct(CutBoard cutBoard, BigDecimal fixedWidth, NormalBoard product) {
        NormalBoard semiProduct = new NormalBoard(cutBoard.getHeight(), fixedWidth, cutBoard.getLength(), cutBoard.getMaterial(), BoardCategory.SEMI_PRODUCT);
        if (semiProduct.getWidth().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal productAllWidth = Arith.mul(product.getWidth(), product.getCutTimes());
            productAllWidth = Arith.cmp(product.getWidth(), BoardUtils.CLAMP_DEPTH) >= 0 ? productAllWidth : productAllWidth.add(BoardUtils.CLAMP_DEPTH);
            productAllWidth = BoardUtils.processPostProductAllWidth(productAllWidth, product.getLength(), semiProduct.getLength());
            BigDecimal remainingWidth = Arith.sub(cutBoard.getWidth(), productAllWidth);
            // 半成品的裁剪次数取决于下料板裁剪成品后的剩余宽度以及半成品自身宽度:
            semiProduct.setCutTimes(Arith.div(remainingWidth, semiProduct.getWidth()));
        }
        return semiProduct;
    }

    /**
     * 获取库存件。
     *
     * @param specs    库存件规格集合
     * @param cutBoard 下料板
     * @param product  成品板
     * @return 库存件
     */
    public NormalBoard getMatchStock(List<StockSpecification> specs, CutBoard cutBoard, NormalBoard product) {
        StockSpecification ss = specs.stream()
                .filter(spec -> spec.getHeight().compareTo(cutBoard.getHeight()) == 0)
                .findFirst()
                .orElse(ParamUtils.getDefaultStockSpec());
        NormalBoard stock = new NormalBoard(ss.getHeight(), ss.getWidth(), ss.getLength(), cutBoard.getMaterial(), BoardCategory.STOCK);
        if (stock.getWidth().compareTo(BigDecimal.ZERO) > 0 && cutBoard.getLength().compareTo(stock.getLength()) > 0) {
            BigDecimal productAllWidth = Arith.mul(product.getWidth(), product.getCutTimes());
            BigDecimal remainingWidth = Arith.sub(cutBoard.getWidth(), productAllWidth);
            if (product.getLength().compareTo(stock.getLength()) >= 0) {
                if (BoardUtils.isAllowCutting(remainingWidth, product.getLength(), stock.getLength())) {
                    stock.setCutTimes(Arith.div(BoardUtils.getAvailableWidth(remainingWidth, stock.getWidth()), stock.getWidth()));
                }
            } else {
                productAllWidth = Arith.cmp(product.getWidth(), BoardUtils.CLAMP_DEPTH) >= 0 ? productAllWidth : productAllWidth.add(BoardUtils.CLAMP_DEPTH);
                productAllWidth = BoardUtils.processPostProductAllWidth(productAllWidth, product.getLength(), stock.getLength());
                remainingWidth = Arith.sub(cutBoard.getWidth(), productAllWidth);
                stock.setCutTimes(Arith.div(remainingWidth, stock.getWidth()));
            }
        }
        return stock;
    }

    /**
     * 获取额外板材。
     *
     * @param cutBoard       下料板
     * @param forwardEdge    裁剪方向
     * @param targetMeasure  目标度量
     * @param wasteThreshold 废料阈值
     * @return 额外板材
     */
    public NormalBoard getExtraBoard(CutBoard cutBoard, ForwardEdge forwardEdge, BigDecimal targetMeasure, BigDecimal wasteThreshold) {
        NormalBoard extraBoard = new NormalBoard();
        extraBoard.setHeight(cutBoard.getHeight());
        // 以进刀出去的边作为较长边:
        if (forwardEdge == ForwardEdge.LONG) {
            extraBoard.setLength(cutBoard.getLength());
            extraBoard.setWidth(Arith.sub(cutBoard.getWidth(), targetMeasure));
        } else {
            extraBoard.setLength(cutBoard.getWidth());
            extraBoard.setWidth(Arith.sub(cutBoard.getLength(), targetMeasure));
        }
        extraBoard.setMaterial(cutBoard.getMaterial());
        extraBoard.setCategory(BoardUtils.calBoardCategory(extraBoard.getWidth(), extraBoard.getLength(), wasteThreshold));
        // 额外板材裁剪次数取决于目标度量和下料板对应度量的差值:
        extraBoard.setCutTimes(extraBoard.getWidth().compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
        return extraBoard;
    }

    /**
     * 获取后续成品。
     *
     * @param nextOrder    后续工单
     * @param currCutBoard 当前下料板
     * @param currProduct  当前成品板
     * @return 后续成品
     */
    public NormalBoard getNextProduct(WorkOrder nextOrder, CutBoard currCutBoard, NormalBoard currProduct) {
        NormalBoard nextProduct = new NormalBoard(nextOrder.getProductSpecification(), nextOrder.getMaterial(), BoardCategory.PRODUCT);
        if (currProduct.getMaterial().equals(nextProduct.getMaterial())) {
            BigDecimal remainingWidth = Arith.sub(currCutBoard.getWidth(), Arith.mul(currProduct.getWidth(), currProduct.getCutTimes()));
            if (BoardUtils.isAllowCutting(remainingWidth, currProduct.getLength(), nextProduct.getLength())) {
                nextProduct.setCutTimes(Math.min(Arith.div(BoardUtils.getAvailableWidth(remainingWidth, nextProduct.getWidth()), nextProduct.getWidth()), nextOrder.getIncompleteQuantity()));
            }
        }
        return nextProduct;
    }
}
