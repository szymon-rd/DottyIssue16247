package testorg.impl;

import testorg.*;
import testorg.factory.*;

public class SimpleNodeFactory implements MkNodes {
    int counter = 0;
    private static final SimpleNodeFactory shared = new SimpleNodeFactory();

    protected SimpleNodeFactory() {
    }

    public static SimpleNodeFactory getInstance() {
        return shared;
    }

    public BNode mkBNode() {
        String name = "bn_" + counter;
        counter = counter + 1;
        return new BNode() {
            public String value() {
                return name;
            }
        };
    }

    public Uri mkUri(String u) {
        return new Uri() {
            public String value() {
                return u;
            }
        };
    };

    public Lit mkLit(String u) {
        return new Lit() {
            public String value() {
                return u;
            }
        };
    }
}
