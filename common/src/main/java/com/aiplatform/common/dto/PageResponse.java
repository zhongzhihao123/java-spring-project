package com.aiplatform.common.dto;

import java.util.List;

/**
 * 分页响应体
 */
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long total;
    private int totalPages;

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long total) {
        var r = new PageResponse<T>();
        r.content = content;
        r.page = page;
        r.size = size;
        r.total = total;
        r.totalPages = (int) Math.ceil((double) total / size);
        return r;
    }
}
