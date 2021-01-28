package com.example.recscore;

public  class BodyPart{

    private String uidx ;
    private int part_idx;
    private float x;
    private float y;
    public float score;
    BodyPart(){

    }
    BodyPart(String uidx,int part_idx, float x,float y,float score){
        this.uidx =uidx;
        this.part_idx = part_idx;
        this.x = x;
        this.y = y;
        this.score = score;

    }
    public float getX(){
        return this.x;
    }
    public float getY(){
        return this.y;
    }
    public String getUidx(){
        return this.uidx;
    }
    public Integer getPart_idx(){
        return this.part_idx;
    }
    public float getScore(){
        return this.score;
    }
    public String get_part_name(int part_idx){
        this.part_idx = part_idx;
        switch (part_idx)
        {
            case 0:
                return "Nose";
            case 1:
                return "Neck";
            case 2:
                return "RShoulder";
            case 3:
                return "RElbow";
            case 4:
                return "RWrist";
            case 5:
                return "LShoulder";
            case 6:
                return "LElbow";
            case 7:
                return "LWrist";
            case 8:
                return "RHip";
            case 9:
                return "RKnee";
            case 10:
                return "RAnkle";
            case 11:
                return "LHip";
            case 12:
                return "LKnee";
            case 13:
                return "LAnkle";
            case 14:
                return "REye";
            case 15:
                return "LEye";
            case 16:
                return "REar";

            case 17:
                return "LEar";
            case 18:
                return "Background";
            default:
                return "NOT IN";


        }
    }
    public String __str__(){
        return "BodyPart:"+this.part_idx+"-("+this.x+","+this.y+") score="+this.score;
    }




}




