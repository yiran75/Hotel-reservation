import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler
{
    public static <T> void saveToFile(List<T> list, String filename)
    {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename)))
        {
            oos.writeObject(list);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> loadFromFile(String filename)
    {
        File file = new File(filename);
        if (!file.exists())
        {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename)))
        {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e)
        {
            return new ArrayList<>();
        }
    }
}