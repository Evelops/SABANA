package com.example.sabana;

public class ItemInfo {

    public int id;
    public String franchise;
    public String product;
    public int saleType;
    public int price;

    public ItemInfo(int id, String franchise, String product, int saleType, int price){
        this.id = id;
        this.franchise = franchise;
        this.product = product;
        this.saleType = saleType;
        this.price = price;
    }

    public String getSaleTypeToString() {
        if(this.saleType == 1) {
            return "1+1";
        } else if(this.saleType == 2) {
            return "2+1";
        }

        return "";
    }

    public int getId() {
        return id;
    }
    public String getFranchise() {
        return franchise;
    }
    public String getProduct() {
        return product;
    }
    public int getSaleType() {
        return saleType;
    }
    public int getPrice() {
        return price;
    }

}
