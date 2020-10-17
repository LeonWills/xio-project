package com.cat.util;

import com.cat.entity.Board;
import com.cat.entity.enums.BoardCategory;

import java.math.BigDecimal;
import java.util.List;

public class BoardUtil {
    public static final String SPEC_SEP = "×";

    public static List<BigDecimal> specStrToList(String specification) {
        String[] specifications = specification.split(SPEC_SEP);
        return List.of(new BigDecimal(specifications[0]),
                new BigDecimal(specifications[1]),
                new BigDecimal(specifications[2]));
    }

    public static int compareTwoSpecification(String sp1, String sp2) {
        List<BigDecimal> o1List = BoardUtil.specStrToList(sp1);
        List<BigDecimal> o2List = BoardUtil.specStrToList(sp2);
        if (o1List.get(0).compareTo(o2List.get(0)) != 0) {
            return o2List.get(0).compareTo(o1List.get(0));
        } else if (o1List.get(1).compareTo(o2List.get(1)) != 0) {
            return o2List.get(1).compareTo(o1List.get(1));
        } else {
            return o2List.get(2).compareTo(o1List.get(2));
        }
    }

    public static String getSpecStr(BigDecimal height, BigDecimal width, BigDecimal length) {
        return String.join(SPEC_SEP, height.toString(), width.toString(), length.toString());
    }

    public static BoardCategory calBoardCategory(BigDecimal boardWidth, BigDecimal boardLength, BigDecimal wasteThreshold) {
        if (boardWidth.compareTo(wasteThreshold) >= 0 && boardLength.compareTo(wasteThreshold) >= 0) {
            return BoardCategory.REMAINING;
        } else {
            return BoardCategory.WASTED;
        }
    }

    public static void standardizingBoard(Board board) {
        if (board.getWidth().compareTo(board.getLength()) > 0) {
            BigDecimal tmp = board.getWidth();
            board.setWidth(board.getLength());
            board.setLength(tmp);
        }
    }
}
