package com.nowcoder.community.entity;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 封装分页相关的信息
 */


public class Page {
    // 当前页码
    private int current = 1;
    // 页面大小
    private int limit = 10;
    // 数据总数（用于计算总页数）
    private int rows;
    // 查询路径（用于复用分页链接）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) {
            // 当前页码有效
            this.current = current;
        } else{
            this.current = 1;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            // 认为limit有效
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的行
     * @return
     */
    public int getOffset() {
        // (当前页码-1)*limit
        return (current-1)*limit;
    }

    /**
     * 获取总页数
     * @return
     */
    public int getTotal() {
        if (rows % limit == 0) {
            return rows/limit;
        } else {
            return (rows/limit)+1;
        }
    }

    /**
     * 获取起始页码，因为数据太多的话，不可能将所有页码都显示出来
     * @return
     */
    public int getFrom() {
        int from = current-2;
        return from < 1 ? 1:from;
    }

    /**
     * 获取结束页码
     * @return
     */
    public int getTo() {
        int to = current+2;
        int total = getTotal();
        return to > total ? total:to;
    }
}
