package com.chainquery.examples;

import java.util.List;
import java.util.Set;

public interface Person {

    public String getName();
    public void setName(String name);

    public Integer getAge();
    public void setAge(Integer age);

    public List<Person> getChildren();
    public void setChildren(List<Person> children);

    public Set<Person> getFriends();
    public void setFriends(Set<Person> friends);

}