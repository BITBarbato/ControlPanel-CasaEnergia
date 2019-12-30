package com.leviponti.tourcasaenergia.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;

public class SiteStack implements Serializable {

    private Stack<String> sites;
    private int pos;

    public SiteStack() {

        this.sites=new Stack<>();
        this.pos=0;


    }

    public SiteStack(ArrayList<String> list){
        this.sites=new Stack<>();
        this.sites.addAll(list);
    }

    public void addSite(String url){

        this.sites.push(url);
        this.pos=this.sites.size()-1;

    }

    public String getPrev(){
        if(this.pos>0) {
            this.pos--;
            return this.sites.get(this.pos);
        }

        return null;
    }

    public String getNext(){
        if(this.pos<this.sites.size()-1) {
            this.pos++;
            return this.sites.get(this.pos);
        }
        return null;
    }

    public Stack<String> getSites() {
        return sites;
    }

    public void setSites(Stack<String> sites) {
        this.sites = sites;
    }
}
