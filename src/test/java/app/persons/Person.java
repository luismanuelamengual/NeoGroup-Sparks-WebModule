
package app.persons;

import org.neogroup.sparks.model.Entity;
import org.neogroup.sparks.model.annotations.Column;
import org.neogroup.sparks.model.annotations.GeneratedValue;
import org.neogroup.sparks.model.annotations.Id;
import org.neogroup.sparks.model.annotations.Table;

@Table(name = "person")
public class Person extends Entity<Integer> {

    @Id
    @Column(name = "personid")
    @GeneratedValue
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "age")
    private Integer age;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                '}';
    }
}
