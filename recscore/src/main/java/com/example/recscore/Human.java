package com.example.recscore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.max;

public class Human{

    public float[][] pairs = new float[999][11];
    public Set uidx_list = new HashSet();
    public Map<Float,BodyPart> body_parts = new HashMap();
    public void init(final float[][] pairs){
        this.pairs = pairs;
        for (int i=0;i<pairs.length;i++){
            if (pairs[i] != null)
                add_pair(pairs[i]);
            else
                break;

        }
    }
    Human(float[][] pairs){
        this.pairs = pairs;
        for (int i=0;i<pairs.length;i++){
            if (pairs[i][0] > 0)
                add_pair(pairs[i]);
            else
                break;

        }

    }

    public String _get_unix(int part_idx, int idx){
        String s = ""+part_idx;
        String s1 = ""+idx;
        return s+'-'+s1;
    }


    public   void add_pair(float[] pair){
        this.pairs = pairs;


        this.body_parts.put(pair[1],new BodyPart(_get_unix((int)pair[1],(int)pair[3]),(int)pair[1],pair[5],pair[6],pair[0]));
        this.body_parts.put(pair[2],new BodyPart(_get_unix((int)pair[2],(int)pair[4]),(int)pair[2],pair[7],pair[8],pair[0]));
        this.uidx_list.add(_get_unix((int)pair[1],(int)pair[3]));
        this.uidx_list.add(_get_unix((int)pair[2],(int)pair[4]));





    }
    public boolean is_connected(Human other){
        Set result = new HashSet();
        result.clear();
        result.addAll(this.uidx_list);
        result.retainAll(other.uidx_list);
        int k = result.size();
        if (k > 0)
            return true;
        else
            return false;


    }
    public void merge(Human other){
        for (int i=0;i<other.pairs.length;i++){
            if (other.pairs[i][0] > 0)
                this.add_pair(other.pairs[i]);
            else
                break;

        }
    }
    public int part_count(){
        return this.body_parts.keySet().size();
    }
    public float get_max_score(){
        List<Float> list1 = new ArrayList<Float>();
        Iterator iter = this.body_parts.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry entry = (Map.Entry) iter.next();
            //Object key = entry.getKey();
            BodyPart val = (BodyPart) entry.getValue();
            list1.add(val.getScore());




        }
        return max(list1);


    }
    public String __str__(){
        String s = new String();
        Iterator iter = this.body_parts.entrySet().iterator();
        while (iter.hasNext()){
            Map.Entry entry = (Map.Entry) iter.next();
            //Object key = entry.getKey();
            BodyPart val = (BodyPart) entry.getValue();
            s+=" ";
            s+=val.__str__();




        }

        return s;
    }


}