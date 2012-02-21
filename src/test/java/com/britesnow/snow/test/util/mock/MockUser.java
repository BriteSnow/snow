package com.britesnow.snow.test.util.mock;

public class MockUser {
    public enum Level {
        manager, employee, exec;
    }

    private Long        id;
    private String      firstName;
    private String      lastName;
    private MockAddress address;
    private Level       level;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;

    }

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

    public MockAddress getAddress() {
        return address;
    }

    public void setAddress(MockAddress address) {
        this.address = address;

    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

}
