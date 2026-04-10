package com.demo.pdf.models;

public class PageAngle {

    private int page;
    private int angle;

    public PageAngle() {
    }

    public PageAngle(int page, int angle) {
        this.page = page;
        this.angle = angle;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }
}
