package com.chainquery.examples;

import java.util.List;
import java.util.Set;

public class PersonP {

    public final Property<String> name = new Property<String>();

    public final Property<Integer> age = new Property<Integer>();

    public final Property<List<PersonP>> children = new Property<List<PersonP>>();

    public final Property<Set<PersonP>> friends= new Property<Set<PersonP>>();

/*
    public List<PersonP> getChildless() {
        PersonP person = database.alias(PersonP.class);
        return database.select(person).where(
                has(count(person.children), EQUAL, 0)
        );
    }
*/
}

class Property<T> {
    public T get() { return null;}
    public void set(T value) {}
}
