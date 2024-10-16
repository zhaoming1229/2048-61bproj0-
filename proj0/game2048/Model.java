package game2048;

import java.util.Formatter;
import java.util.Observable;


/** 2048游戏的状态.
 *  @author TODO: 请在此处填写您的姓名
 */
public class Model extends Observable {
    /** 当前棋盘的内容。 */
    private Board board;
    /** 当前得分。 */
    private int score;
    /** 当前最高得分。当游戏结束时更新。 */
    private int maxScore;
    /** 如果游戏结束则为真。 */
    private boolean gameOver;

    /* 坐标系统：棋盘的列 C, 行 R （其中行 0, 列 0 是棋盘的左下角）
     * 将对应 board.tile(c, r)。要小心！它的工作方式类似 (x, y) 坐标系。
     */

    /** 最大的棋子数值。 */
    public static final int MAX_PIECE = 2048;

    /** 创建一个新的2048游戏，棋盘大小为SIZE，没有棋子，得分为0。 */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** 创建一个新的2048游戏，其中 RAWVALUES 包含棋子的数值（如果为空则为0）。
     * VALUES 的索引为 (row, col)，其中 (0, 0) 对应棋盘的左下角。
     * 用于测试目的。
     */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** 返回位于 (COL, ROW) 的当前棋子，其中 0 <= ROW < size(),
     *  0 <= COL < size()。如果没有棋子，则返回 null。
     *  用于测试。应该被弃用并删除。
     */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** 返回棋盘一边的方格数。
     *  用于测试。应该被弃用并删除。
     */
    public int size() {
        return board.size();
    }

    /** 返回游戏是否结束（棋盘上没有可移动的棋子，或者
     *  有一个数值为 2048 的棋子）。
     */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** 返回当前得分。 */
    public int score() {
        return score;
    }

    /** 返回当前最高游戏得分（在游戏结束时更新）。 */
    public int maxScore() {
        return maxScore;
    }

    /** 清空棋盘并重置得分。 */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** 在棋盘上添加 TILE。添加位置不能已有棋子。 */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** 向 SIDE 方向倾斜棋盘。若棋盘发生变化，返回 true。
     *
     * 1. 如果两个相邻的 Tile 对象在移动方向上数值相同，
     *    它们将合并成一个数值为原来两倍的 Tile，该数值会被加入到得分中。
     * 2. 合并产生的 Tile 在本次倾斜中不会再次合并。所以每个移动，
     *    每个棋子最多只能参与一次合并（也可能不参与）。
     * 3. 当三个相邻的棋子数值相同时，在移动方向上，前两个棋子会合并，
     *    而最后一个棋子不会合并。
     */
    public boolean tilt(Side side) {
        boolean changed = false;

        // 将棋盘旋转到指定的视角，方便统一处理向北（上）倾斜的情况
        board.setViewingPerspective(side);

        int size = board.size();

        // 对每一列进行处理
        for (int col = 0; col < size; col++) {
            // 记录当前列中已合并的行
            boolean[] merged = new boolean[size];
            // 从最上面的行开始，目标位置
            int targetRow = size - 1;

            // 从次顶行开始向下遍历
            for (int row = size - 2; row >= 0; row--) {
                Tile t = board.tile(col, row);
                if (t != null) {
                    int destRow = row;

                    // 向上寻找可以移动或合并的位置
                    while (destRow < targetRow) {
                        Tile aboveTile = board.tile(col, destRow + 1);
                        if (aboveTile == null) {
                            // 如果上方格子为空，移动上去
                            destRow++;
                        } else if (aboveTile.value() == t.value() && !merged[destRow + 1]) {
                            // 如果上方格子的值相同且未合并过，进行合并
                            destRow++;
                            merged[destRow] = true; // 标记该位置已经合并过
                            score += t.value() * 2; // 更新得分
                            break;
                        } else {
                            // 无法继续移动或合并，停止
                            break;
                        }
                    }

                    if (destRow != row) {
                        // 执行移动或合并操作
                        board.move(col, destRow, t);
                        changed = true;
                    }
                }
            }
        }

        // 恢复棋盘视角
        board.setViewingPerspective(Side.NORTH);

        // 检查游戏是否结束
        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }


    /** 检查游戏是否结束，并相应地设置 gameOver 变量。 */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** 确定游戏是否结束。 */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** 如果棋盘上至少有一个空格，返回 true。
     *  空格在棋盘上以 null 表示。
     */
    public static boolean emptySpaceExists(Board b) {
        // TODO: 完成此函数。
        for(int row = 0; row < b.size(); row++){
            for(int col = 0; col < b.size() ; col++){
                if(b.tile(col,row) == null){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 如果有任意一个棋子的值等于最大有效值，返回 true。
     * 最大有效值由 MAX_PIECE 给定。注意，给定一个 Tile 对象 t，
     * 我们可以通过 t.value() 获得它的数值。
     */
    public static boolean maxTileExists(Board b) {
        // TODO: 完成此函数。
        for(int row = 0; row < b.size(); row++){
            for(int col = 0; col < b.size(); col++){
                Tile t = b.tile(col,row);
                if(t != null && t.value() == MAX_PIECE){
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * 如果棋盘上有任何有效移动，返回 true。
     * 有两种情况会存在有效移动：
     * 1. 棋盘上至少有一个空格。
     * 2. 有两个相邻的棋子数值相同。
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: 完成此函数
        if (emptySpaceExists(b)){
            return true;
        }
        for(int row = 0; row < b.size(); row++){
            for(int col = 0; col < b.size(); col++){
                Tile t = b.tile(col,row);
                if(t != null) {
                    if (col < b.size() - 1) {
                        Tile right = b.tile(col + 1, row);
                        if (right != null && t.value() == right.value()) {
                            return true;
                        }
                    }
                    if(row < b.size() -1 ) {
                        Tile above = b.tile(col, row + 1);
                        if (above != null && t.value() == above.value()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    @Override
    /** 返回模型的字符串表示，用于调试。 */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "游戏结束" : "游戏未结束";
        out.format("] %d (最高分: %d) (游戏状态: %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** 返回两个模型是否相等。 */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** 返回 Model 字符串的哈希码。 */
    public int hashCode() {
        return toString().hashCode();
    }
}