package com.chainquery.examples;

import com.chainquery.Row;

import java.util.List;
import java.util.Set;

public interface Person extends Row {

    public String getName();
    public void setName(String name);

    public Integer getAge();
    public void setAge(Integer age);

    public Person getSpouse();
    public void setSpouse(Person spouse);

    public List<Person> getChildren();
    public void setChildren(List<Person> children);

    public Set<Person> getFriends();
    public void setFriends(Set<Person> friends);
}
