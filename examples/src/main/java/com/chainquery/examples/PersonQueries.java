package com.chainquery.examples;

import com.chainquery.*;

import static com.chainquery.Constraint.*;

public class PersonQueries {

    public static void main(String[] _) {
        System.out.println(constraint1());

        Person person1 = Alias.create(Person.class);
        System.out.println(person1);
        System.out.println("person1.type() = " + person1.type());


        Person person = Record.create(Person.class);
        System.out.println(person);
        person.setName("John Corner");
        System.out.println("person.getName() = " + person.getName());
        System.out.println("person.type() = " + person.type());
    }

    public static Constraint constraint1() {
        Person person = Alias.create(Person.class);
        return all(
                has(person.getName()).equalTo("Peter"),
                has(person.getSpouse().getAge()).equalTo(21),
                has(person.getSpouse().getName()).equalTo("Susan")
        );
    }

    public static void query1() {
        Database db = new InMemoryDatabase();
        Person person = db.alias(Person.class);
        db.select(person).where(
            has(person.getName()).equalTo("Hansen"),
            has(person.getAge()).greaterOrEqualTo(21),
            has(person.getChildren().get(ANY).getAge()).greaterThan(21)
            ).list();
    }
}
