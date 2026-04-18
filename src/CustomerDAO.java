package dao;

import model.Customer;
import observer.DataObservable;
import observer.DataObserver;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer entity.
 * Handles sequential-access file I/O using Java Serialization.
 * Implements DataObservable to notify the UI (JTable) when data changes.
 */
public class CustomerDAO implements DataObservable {
    private static final String FILE_PATH = "data/customers.dat";
    private List<DataObserver> observers = new ArrayList<>();

    public CustomerDAO() {
        // Ensure data directory exists
        new File("data").mkdirs();
    }

    // ── Observer Pattern ──────────────────────────────────────────────────────

    @Override
    public void addObserver(DataObserver observer) {
        if (!observers.contains(observer)) observers.add(observer);
    }

    @Override
    public void removeObserver(DataObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String eventType) {
        for (DataObserver observer : observers) {
            observer.onDataChanged(eventType);
        }
    }

    // ── CRUD Operations ───────────────────────────────────────────────────────

    /** Save a new customer (append to sequential file). */
    public boolean save(Customer customer) {
        try {
            List<Customer> all = findAll();
            all.add(customer);
            writeAll(all);
            notifyObservers("ADDED");
            return true;
        } catch (IOException e) {
            System.err.println("Error saving customer: " + e.getMessage());
            return false;
        }
    }

    /** Load all customers from the sequential file. */
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return customers;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            while (true) {
                try {
                    Customer c = (Customer) ois.readObject();
                    customers.add(c);
                } catch (EOFException eof) {
                    break; // End of sequential file
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading customers: " + e.getMessage());
        }
        return customers;
    }

    /** Find a single customer by ID. */
    public Customer findById(int customerId) {
        return findAll().stream()
                .filter(c -> c.getCustomerId() == customerId)
                .findFirst()
                .orElse(null);
    }

    /** Update an existing customer record. */
    public boolean update(Customer updated) {
        List<Customer> all = findAll();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getCustomerId() == updated.getCustomerId()) {
                all.set(i, updated);
                found = true;
                break;
            }
        }
        if (!found) return false;
        try {
            writeAll(all);
            notifyObservers("UPDATED");
            return true;
        } catch (IOException e) {
            System.err.println("Error updating customer: " + e.getMessage());
            return false;
        }
    }

    /** Delete a customer by ID. */
    public boolean delete(int customerId) {
        List<Customer> all = findAll();
        boolean removed = all.removeIf(c -> c.getCustomerId() == customerId);
        if (!removed) return false;
        try {
            writeAll(all);
            notifyObservers("DELETED");
            return true;
        } catch (IOException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            return false;
        }
    }

    /** Overwrite the entire sequential file with the given list. */
    private void writeAll(List<Customer> customers) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            for (Customer c : customers) {
                oos.writeObject(c);
            }
        }
    }
}
