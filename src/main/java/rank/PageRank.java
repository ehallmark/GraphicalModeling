package rank;

import java.io.File;

/**
 * Created by ehallmark on 4/21/17.
 */
public class PageRank extends RankGraph<PageRank> {
    private static final File file = new File("page_rank_file.jobj");
    public PageRank() {
        super(file);
    }
    @Override
    protected void init() {

    }

    @Override
    public void solve() {

    }
}
