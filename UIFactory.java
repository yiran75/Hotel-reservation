interface ICustomerFactory {
    Customer createCustomer(String id, String name, String phone, String email);
}

class CustomerFactory implements ICustomerFactory {
    @Override
    public Customer createCustomer(String id, String name, String phone, String email) {
        return new Customer(id, name, phone, email);
    }
}
