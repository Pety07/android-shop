package com.example.product_ordering;

import java.util.List;

public class Order {

    private String id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String description;
    private String orderDate;
    private List<Product> orderList;
    private String fullPrice;
    private String deliveryFee;

    public Order() { }

    public Order(String fullName, String email, String phoneNumber, String address, String description, String orderDate, List<Product> orderList, String fullPrice, String deliveryFee) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.description = description;
        this.orderDate = orderDate;
        this.orderList = orderList;
        this.fullPrice = fullPrice;
        this.deliveryFee = deliveryFee;
    }

    public String getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(String deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public String _getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public List<Product> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<Product> orderList) {
        this.orderList = orderList;
    }

    public String getFullPrice() {
        return fullPrice;
    }

    public void setFullPrice(String fullPrice) {
        this.fullPrice = fullPrice;
    }
}
