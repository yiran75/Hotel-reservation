import java.util.*;

interface Observer {
    void update();
}

class Subject {
    private List<Observer> observers = new ArrayList<>();
    public void addObserver(Observer o) { observers.add(o); }
    public void notifyObservers() {
        for (Observer o : observers) o.update();
    }
}