package com.britesnow.snow.test.app.simpleapp.entity;

public class Employee {

    private String firstName;
    private String lastName;
    private Long id;
    
    
    
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public Long getId(){
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
        
    }
    
    
}
