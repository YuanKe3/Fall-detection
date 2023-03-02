/**
  * Copyright 2023 bejson.com 
  */
package com.example.arr_pose1.HealthNews.bean;
import java.util.List;

/**
 * Auto-generated: 2023-03-02 9:55:29
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Result {

    private int curpage;
    private int allnum;
    private List<HealthNews> newslist;
    public void setCurpage(int curpage) {
         this.curpage = curpage;
     }
     public int getCurpage() {
         return curpage;
     }

    public void setAllnum(int allnum) {
         this.allnum = allnum;
     }
     public int getAllnum() {
         return allnum;
     }

    public void setNewslist(List<HealthNews> newslist) {
         this.newslist = newslist;
     }
     public List<HealthNews>  getNewslist() {
         return newslist;
     }

}