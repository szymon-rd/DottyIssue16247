package testorg.factory;

import testorg.*;

public interface MkNodes {
    public BNode mkBNode();
    public Uri mkUri(String u);
    public Lit mkLit(String u);
}
