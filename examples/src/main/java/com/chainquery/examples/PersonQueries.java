package com.chainquery.examples;

import com.chainquery.*;

import java.util.List;

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

        databaseTest1();
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

    public static void databaseTest1() {
        Database db = new InMemoryDatabase();
        Person person = db.alias(Person.class);
        listPersonsWhere(db, all());

        Person john = Record.create(Person.class);
        john.setName("John Connor");
        db.save(john);
        listPersonsWhere(db, all());

        Person sara = Record.create(Person.class);
        sara.setName("Sarah Connor");
        db.save(sara);
        listPersonsWhere(db, all());
        listPersonsWhere(db, has(person.getName()).equalTo("Sarah Connor"));
    }

    private static void listPersonsWhere(Database db, Constraint constraint) {
        System.out.println("|--------- Persons ---------|");
        Person person = db.alias(Person.class);
        final List<Person> list = db.select(person).where(constraint).list();
        for (Person p: list) {
            System.out.println(p.getName());
        }
    }
}
