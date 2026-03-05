import java.io.Serializable;

public class Customer implements Serializable {
    private String id, name, phone, email;

    public Customer(String id, String name, String phone, String email) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    @Override
    public String toString() {
        return "ID: " + id + " | Name: " + name;
    }
}

interface ICustomerFactory {
    Customer createCustomer(String id, String name, String phone, String email);
}

class CustomerFactory implements ICustomerFactory {
    @Override
    public Customer createCustomer(String id, String name, String phone, String email) {
        return new Customer(id, name, phone, email);
    }
}
