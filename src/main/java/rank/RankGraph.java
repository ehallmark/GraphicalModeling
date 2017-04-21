package rank;

import java.io.*;

/**
 * Created by ehallmark on 4/21/17.
 */
public abstract class RankGraph<T> implements Serializable {
    private static final long serialVersionUID = 1l;

    protected File file;
    protected RankGraph(File file) {
        this.file=file;
    }

    protected abstract void init();

    public abstract void solve();

    protected void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            oos.writeObject(this);
            oos.flush();
            oos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public T load() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            return (T) ois.readObject();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
