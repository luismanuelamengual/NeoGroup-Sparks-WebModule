
package app.users;

import org.neogroup.sparks.model.Entity;
import org.neogroup.sparks.model.annotations.Id;

public class User extends Entity<Integer> {

    @Id
    private int id;

    private String name;

    private String lastName;

    private int age;

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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
