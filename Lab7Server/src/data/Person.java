package data;
/**
 *@author Azamat Tadeev
 */

public class Person implements Comparable<Person> {
    private String name;
    private int courage;

    public Person(String name, int courage){
        this.name = name;
        this.courage = courage;
    }

    @Override
    public int compareTo(Person person2) {
        if (this.courage>person2.getCourage()) return 1;
        if (this.courage<person2.getCourage()) return -1;
        return this.name.compareTo(person2.getName());
    }

    public boolean equals(Object ob) {
        if (this.getClass() != ob.getClass()) return false;
        Person person2 = (Person) ob;
        return (this.name.equals(person2.getName())) && (courage == person2.getCourage());
    }

    public String getName() {
        return name;
    }

    public int getCourage() {
        return courage;
    }

}
