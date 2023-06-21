/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package com.manticore.h2;

/**
 * @author are
 */
public class Recommendation {
    public Type type;
    public Object object;
    public String issue;
    public String recommendation;
    public Recommendation(Type type, Object object, String issue, String recommendation) {
        this.type = type;
        this.object = object;
        this.issue = issue;
        this.recommendation = recommendation;
    }

    public enum Type {
        DECIMAL_PRECISION
    }
}
